package tw.com.louis383.coffeefinder.mainpage

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.common.api.ResolvableApiException
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.louis383.coffeefinder.BasePresenter
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.model.ConnectivityChecker
import tw.com.louis383.coffeefinder.model.UserLocationListener
import tw.com.louis383.coffeefinder.model.data.entity.Shop
import tw.com.louis383.coffeefinder.model.domain.usecase.GetCoffeeShopsUseCase
import tw.com.louis383.coffeefinder.utils.ifNotNull
import tw.com.louis383.coffeefinder.utils.toLatLng
import java.util.*
import javax.inject.Inject

/**
 * Created by louis383 on 2017/2/17.
 */

@ActivityScoped
class MainPresenter @Inject constructor(
    private val getCoffeeShopsUseCase: GetCoffeeShopsUseCase,
    private val connectivityChecker: ConnectivityChecker,
    private val userLocationListener: UserLocationListener
) : BasePresenter<MainView>(),
    UserLocationListener.OnLocationRequestErrorListener {

    private val googleMapPackage = "com.google.android.apps.maps"
    private val range = 3000    // 3m
    private var anchorHeight = 200

    var currentLocation: Location? = null
        private set(value) {
            field = value
        }

    private var lastTappedCoffeeShop: Shop? = null

    override fun attachView(view: MainView) {
        super.attachView(view)
        collectUserLocationChange()
        with(view) {
            setStatusBarDarkIndicator()

            if (!checkLocationPermission()) {
                requestLocationPermission()
            }

            if (!isNetworkAvailable()) {
                requestInternetConnection()
            }

            anchorHeight = getViewPagerBottomSheetBehavior().anchorOffset
        }
    }

    private fun collectUserLocationChange() {
        uiScope.launch {
            userLocationListener.userLocationPublisher
                .filter {
                    it != null && !userLocationListener.isWaitingAccurateLocation
                }
                .filterNotNull()
                .collect { location ->
                    if (this@MainPresenter.currentLocation == null) {
                        fetchCoffeeShops(location)
                        moveCameraToPosition(location)
                    }
                    this@MainPresenter.currentLocation = location
                    Log.w("MainPresenter", "Location Update to $location")
                }
        }
    }

    private fun moveCameraToPosition(location: Location) {
        view?.moveCameraToCurrentPosition(location.toLatLng())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun startPresenterWorks() {
        userLocationListener.errorListener = this
        requestUserLocation()
        view?.getViewPagerBottomSheetBehavior()?.addBottomSheetCallback(bottomSheetCallback)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun pausePresenterWorks() {
        userLocationListener.errorListener = null
        userLocationListener.stopLocationUpdate()
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

    fun requestUserLocation() {
        if (view?.checkLocationPermission() == true) {
            userLocationListener.startLocationUpdate()
        }
    }

    private fun fetchCoffeeShops(location: Location) {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            view?.makeSnackBar(R.string.network_error_fetching_api)
            Log.d("MainPresenter", Log.getStackTraceString(throwable))
        }

        uiScope.launch(errorHandler) {
            val coffeeShops = withContext(Dispatchers.IO) {
                getCoffeeShopsUseCase(location, range)
            }

            coffeeShops.fold(
                success = { result ->
                    if (result.isNotEmpty()) {
                        val copiedShops = result.map { it.copy() }
                        view?.updateListPage(copiedShops)
                        view?.onCoffeeShopFetched(copiedShops)
                    }
                },
                failed = {
                    view?.makeSnackBar(R.string.generic_network_error)
                }
            )
        }
    }

    fun prepareNavigation() {
        val isApplicationInstalled = this.view?.isApplicationInstalled(googleMapPackage) ?: return
        if (!isApplicationInstalled) {
            view?.showNeedsGoogleMapMessage()
            return
        }

        ifNotNull(currentLocation, lastTappedCoffeeShop) { currentLocation: Location, lastTappedCoffeeShop: Shop ->
            val urlString = String.format(
                Locale.getDefault(), "http://maps.google.com/maps?daddr=%f,%f&saddr=%f,%f&mode=w",
                lastTappedCoffeeShop.latitude, lastTappedCoffeeShop.longitude,
                currentLocation.latitude, currentLocation.longitude
            )

            val intent = Intent()
            intent.`package` = googleMapPackage
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(urlString)
            view?.navigateToLocation(intent)
        }
    }

    fun share(context: Context) {
        lastTappedCoffeeShop?.getUiModel()?.run {
            val subject = context.resources.getString(R.string.share_subject)
            val message = context.resources.getString(R.string.share_message, shopName, address)

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message + "\n" + detailUri)

            view?.shareCoffeeShop(intent)
        }
    }

    fun setLastTappedCoffeeShop(lastTappedCoffeeShop: Shop) {
        this.lastTappedCoffeeShop = lastTappedCoffeeShop
    }

    fun showDetailView() {
        lastTappedCoffeeShop?.let {
            view?.showBottomSheetDetailView(it)
        }
    }

    fun isNetworkAvailable(): Boolean {
        return connectivityChecker.isNetworkAvailable()
    }

    //region UserLocationListener.OnLocationRequestErrorListener
    override fun onRequestLocationPermission() {
        view?.requestLocationPermission()
    }

    override fun onLocationSettingNeedsResolution(resolvable: ResolvableApiException) {
        view?.locationSettingNeedsResolution(resolvable)
    }

    override fun onLocationServiceUnavailable() {
        view?.showServiceUnavailableMessage()
    }
    //endregion
}
