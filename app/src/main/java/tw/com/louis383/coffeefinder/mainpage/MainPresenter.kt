package tw.com.louis383.coffeefinder.mainpage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.libraries.maps.model.LatLng
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import tw.com.louis383.coffeefinder.BasePresenter
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager
import tw.com.louis383.coffeefinder.model.CurrentLocationCarrier
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop
import tw.com.louis383.coffeefinder.utils.ifNotNull
import java.util.*

/**
 * Created by louis383 on 2017/2/17.
 */

class MainPresenter(private val coffeeShopListManager: CoffeeShopListManager,
                    private val fusedLocationProviderClient: FusedLocationProviderClient,
                    private val currentLocationCarrier: CurrentLocationCarrier) : BasePresenter<MainView>(), CoffeeShopListManager.Callback, LifecycleObserver {
    private val googleMapPackage = "com.google.android.apps.maps"
    private val updateInterval = 10000    // 10 Sec
    private val fastestUpdateInterval = 5000 // 5 Sec
    private val range = 3000    // 3m
    private var anchorHeight = 200

    var currentLocation: Location? = null
        private set(value) {
            currentLocationCarrier.currentLocation = value
            field = value
        }

    private val locationRequest by lazy {
        LocationRequest().apply {
            interval = updateInterval.toLong()
            fastestInterval = fastestUpdateInterval.toLong()
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private var backgroundThread: HandlerThread? = null
    private var uiHandler: Handler? = null

    private var lastTappedCoffeeShop: CoffeeShop? = null

    private var isRequestingLocation = false
    private var isWaitingAccurateLocation = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            result?.run {
                if (locations.isNotEmpty()) {
                    // The last one in the List<> is the latest location result
                    val latestLocation = locations.last()
                    stopLocationUpdate()    // Only get one time accurate position.
                    currentLocation = latestLocation

                    if (isWaitingAccurateLocation) {
                        isWaitingAccurateLocation = false

                        uiHandler?.post {
                            val currentLatLng = LatLng(latestLocation.latitude, latestLocation.longitude)
                            view?.moveCameraToCurrentPosition(currentLatLng)
                            fetchCoffeeShops()
                        }
                    }
                }
            }
        }
    }

    init {
        this.coffeeShopListManager.callback = this
    }

    override fun attachView(view: MainView) {
        super.attachView(view)
        with(view) {
            setStatusBarDarkIndicator()

            if (!checkLocationPermission()) {
                requestLocationPermission()
            }

            if (!isInternetAvailable) {
                requestInternetConnection()
            }

            anchorHeight = getViewPagerBottomSheetBehavior().anchorOffset
        }
    }

    fun addLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun startPresenterWorks() {
        backgroundThread = HandlerThread("BackgroundThread").apply { start() }
        uiHandler = Handler(Looper.getMainLooper())
        requestUserLocation(false)

        view?.getViewPagerBottomSheetBehavior()?.addBottomSheetCallback(bottomSheetCallback)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun pausePresenterWorks() {
        coffeeShopListManager.stop()
        backgroundThread?.quitSafely()
        backgroundThread?.join()
        backgroundThread = null
        uiHandler = null
        stopLocationUpdate()

        view?.getViewPagerBottomSheetBehavior()?.removeBottomSheetCallback(bottomSheetCallback)
    }

    private val bottomSheetCallback = object : AnchorBottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset >= 0f) {
                // negative values to move view up
                val additionalDistances = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, bottomSheet.resources.displayMetrics)
                view?.moveMapView((anchorHeight * slideOffset) * (additionalDistances * -1))
            }
        }
    }

    fun requestUserLocation(force: Boolean) {
        // Prevent request twice current location
        if (currentLocation != null && !force) {
            return
        }

        if (view?.checkLocationPermission() == true) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = it
                    val lastLatLng = LatLng(it.latitude, it.longitude)
                    view?.moveCameraToCurrentPosition(lastLatLng)
                    fetchCoffeeShops()
                    Log.i("MainPresenter", "lastLocation latitude: " + lastLatLng.latitude + ", longitude: " + lastLatLng.longitude)
                } ?: run {
                    isWaitingAccurateLocation = true
                }

                tryToGetAccurateLocation()
            }.addOnFailureListener {
                isWaitingAccurateLocation = true
                tryToGetAccurateLocation()
            }
        }
    }

    fun fetchCoffeeShops() {
        currentLocation?.run {
            coffeeShopListManager.fetch(this, range)
        }
    }

    fun prepareNavigation() {
        val isApplicationInstalled = this.view?.isApplicationInstalled(googleMapPackage) ?: return
        if (!isApplicationInstalled) {
            view?.showNeedsGoogleMapMessage()
            return
        }

        ifNotNull(currentLocation, lastTappedCoffeeShop) { currentLocation: Location, lastTappedCoffeeShop: CoffeeShop ->
            val urlString = String.format(Locale.getDefault(), "http://maps.google.com/maps?daddr=%f,%f&saddr=%f,%f&mode=w",
                    lastTappedCoffeeShop.latitude, lastTappedCoffeeShop.longitude,
                    currentLocation.latitude, currentLocation.longitude)

            val intent = Intent()
            intent.`package` = googleMapPackage
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(urlString)
            view?.navigateToLocation(intent)
        }
    }

    fun share(context: Context) {
        lastTappedCoffeeShop?.run {
            val subject = context.resources.getString(R.string.share_subject)
            val message = context.resources.getString(R.string.share_message, viewModel.shopName, viewModel.address)

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message + "\n" + viewModel.detailUri)

            view?.shareCoffeeShop(intent)
        }
    }

    fun setLastTappedCoffeeShop(lastTappedCoffeeShop: CoffeeShop) {
        this.lastTappedCoffeeShop = lastTappedCoffeeShop
    }

    fun showDetailView() {
        lastTappedCoffeeShop?.let {
            view?.showBottomSheetDetailView(it)
        }
    }

    private fun tryToGetAccurateLocation() {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val context = view?.activityContext ?: return
        val result = LocationServices.getSettingsClient(context)
                .checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                task.getResult<ApiException>(ApiException::class.java)
                if (!isRequestingLocation) {
                    startLocationUpdate()
                }
                Log.i("MapsPresenter", "Succeed.")
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        val resolvable = exception as ResolvableApiException
                        view?.locationSettingNeedsResolution(resolvable)
                        Log.i("MapsPresenter", "Resolution Required")
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        view?.showServiceUnavailableMessage()
                        Log.i("MapsPresenter", "unavailable.")
                    }
                }
            }
        }
    }

    // Make sure call this function after checked permission.
    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        isRequestingLocation = true
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, backgroundThread?.looper)
    }

    private fun stopLocationUpdate() {
        isRequestingLocation = false
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    //region CoffeeShopListManager Callback
    override fun onCoffeeShopFetchedComplete(coffeeShops: List<CoffeeShop>) {
        view?.updateListPage(coffeeShops)
        view?.onCoffeeShopFetched(coffeeShops)
    }

    override fun onCoffeeShopFetchedFailed(message: String) {
        view?.makeSnackBar(message, false)
    }
    //endregion
}
