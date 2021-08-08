package tw.com.louis383.coffeefinder.model

import android.annotation.SuppressLint
import android.location.Location
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

class UserLocationListener(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val settingsClient: SettingsClient,
    private val currentLocationCarrier: CurrentLocationCarrier,
    private val permissionChecker: PermissionChecker
) {

    private val _userLocationState = MutableStateFlow<Location?>(null)
    val userLocationPublisher: StateFlow<Location?>
        get() = _userLocationState

    private val coroutineJobs: Job = SupervisorJob()
    private val defaultErrorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("UserLocationListener", Log.getStackTraceString(throwable))
    }
    private val coroutineContext: CoroutineContext = Dispatchers.Main + coroutineJobs + defaultErrorHandler
    private val uiScope = CoroutineScope(coroutineContext)

    private val updateInterval = 10000    // 10 Sec
    private val fastestUpdateInterval = 5000 // 5 Sec
    var errorListener: OnLocationRequestErrorListener? = null

    var isListeningLocation = false
        private set
    var isWaitingAccurateLocation = false
        private set

    private val locationRequest by lazy {
        LocationRequest.create().apply {
            interval = updateInterval.toLong()
            fastestInterval = fastestUpdateInterval.toLong()
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult?.locations?.takeIf { it.isNotEmpty() }?.run {
                // The last one in the List<> is the latest location result
                val currentLocation = last()
                currentLocationCarrier.currentLocation = currentLocation
                _userLocationState.value = currentLocation

                if (isWaitingAccurateLocation) {
                    isWaitingAccurateLocation = false
                }
            }
        }
    }

    private var backgroundThread: HandlerThread? = null

    fun startLocationUpdate() {
        if (isListeningLocation) {
            return
        }
        isListeningLocation = true
        backgroundThread = HandlerThread("BackgroundThread").apply {
            start()
        }
        requestUserLocation()
    }

    fun stopLocationUpdate() {
        backgroundThread?.run {
            quitSafely()
            join()
        }
        backgroundThread = null
        stopListeningLocation()
        isListeningLocation = false
    }

    @SuppressLint("MissingPermission") // Checked at PermissionChecker
    private fun requestUserLocation() {
        if (!permissionChecker.isLocationPermissionGranted()) {
            errorListener?.onRequestLocationPermission()
            return
        }

        uiScope.launch {
            try {
                val lastLocation = withContext(Dispatchers.Default) {
                    fusedLocationProviderClient.lastLocation.await()
                }

                if (lastLocation != null) {
                    currentLocationCarrier.currentLocation = lastLocation
                    _userLocationState.value = lastLocation
                } else {
                    isWaitingAccurateLocation = true
                }
            } catch (e: Exception) {
                Log.e("UserLocationListener", Log.getStackTraceString(e))
                isWaitingAccurateLocation = true
            } finally {
                tryToGetAccurateLocation()
            }
        }
    }

    private suspend fun tryToGetAccurateLocation() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        try {
            val result = settingsClient.checkLocationSettings(builder.build()).await()
            Log.i("UserLocationListener", result.locationSettingsStates?.toString() ?: "checkLocationSettings states is null")
            startListeningLocationChange()
        } catch (exception: ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    val resolvable = exception as ResolvableApiException
                    errorListener?.onLocationSettingNeedsResolution(resolvable)
                    Log.i("UserLocationListener", "Resolution Required")
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    errorListener?.onLocationServiceUnavailable()
                    Log.i("UserLocationListener", "unavailable.")
                }
            }
        }
    }

    @SuppressLint("MissingPermission") // Checked at PermissionChecker
    private fun startListeningLocationChange() {
        Log.i("UserLocationListener", "start listening user location change")
        backgroundThread?.looper?.run {
            isListeningLocation = true
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, this)
        }
    }

    private fun stopListeningLocation() {
        Log.i("UserLocationListener", "stop listening user location change")
        isListeningLocation = false
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    interface OnLocationRequestErrorListener {
        fun onRequestLocationPermission()
        fun onLocationSettingNeedsResolution(resolvable: ResolvableApiException)
        fun onLocationServiceUnavailable()
    }
}