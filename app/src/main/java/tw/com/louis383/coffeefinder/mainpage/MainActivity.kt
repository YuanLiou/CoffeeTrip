package tw.com.louis383.coffeefinder.mainpage

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.view.animation.AnticipateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.adapter.ViewPagerAdapter
import tw.com.louis383.coffeefinder.core.domain.model.CoffeeShop
import tw.com.louis383.coffeefinder.details.DetailsFragment
import tw.com.louis383.coffeefinder.details.DetailsItemClickListener
import tw.com.louis383.coffeefinder.list.ListFragment
import tw.com.louis383.coffeefinder.maps.MapsClickHandler
import tw.com.louis383.coffeefinder.maps.MapsFragment
import tw.com.louis383.coffeefinder.uimodel.getUiModel
import tw.com.louis383.coffeefinder.utils.QuickCheckUtils
import tw.com.louis383.coffeefinder.utils.bindView
import javax.inject.Inject

/**
 * Created by louis383 on 2017/2/17.
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainView, MapsClickHandler, ListFragment.Callback,
    DetailsItemClickListener, View.OnClickListener {
    private val locationSettingResolution = 1001

    @Inject
    lateinit var presenter: MainPresenter

    private var snackbar: Snackbar? = null

    // Main Content
    private val rootView: CoordinatorLayout by bindView(R.id.main_rootview)
    private val mainContainer: FrameLayout by bindView(R.id.main_container)
    private val myLocationButton: ImageButton by bindView(R.id.main_my_location_button)

    // Bottom Sheet
    private lateinit var bottomSheetViewPager: ViewPager
    private lateinit var bottomSheetBehavior: AnchorBottomSheetBehavior<ViewPager>
    private val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

    // View States
    sealed class ViewState {
        data class EnterDetailInfoFromMap(val coffeeshop: CoffeeShop): ViewState()
        data class EnterDetailInfoFromList(val coffeeshop: CoffeeShop): ViewState()
        object Browsing: ViewState()
    }
    private var detailViewState: ViewState = ViewState.Browsing

    private val mapFragment: MapsFragment?
        get() {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.main_container)
            return mapFragment as? MapsFragment
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        handleSplashScreen(splashScreen)
        setContentView(R.layout.activity_main)

        initMapFragment()
        bottomSheetViewPager = findViewById(R.id.main_bottom_sheet)
        bottomSheetViewPager.adapter = viewPagerAdapter
        bottomSheetBehavior = getViewPagerBottomSheetBehavior()
        bottomSheetBehavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED

        if (QuickCheckUtils.canApplyDynamicColor()) {
            myLocationButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dynamic_light_primary))
        }

        presenter.attachView(this)
        myLocationButton.setOnClickListener(this)
    }

    private fun handleSplashScreen(splashScreen: SplashScreen) {
        splashScreen.setOnExitAnimationListener(object : SplashScreen.OnExitAnimationListener {
            override fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenViewProvider.view,
                    View.TRANSLATION_Y,
                    0f,
                    splashScreenViewProvider.view.height.toFloat() * -1f
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 200L

                slideUp.doOnEnd {
                    splashScreenViewProvider.remove()
                }
                slideUp.start()
            }
        })
    }

    private fun initMapFragment() {
        val mapsFragment = MapsFragment.newInstance()
        with(mapsFragment) {
            retainInstance = true
            setMapClickHandler(this@MainActivity)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, mapsFragment, MapsFragment.TAG)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            locationSettingResolution -> {
                if (resultCode == Activity.RESULT_OK) {
                    presenter.requestUserLocation()
                } else {
                    snackbar = Snackbar.make(rootView, R.string.high_accuracy_recommand, Snackbar.LENGTH_LONG)
                    snackbar?.show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun provideLifecycleOwner(): LifecycleOwner {
        return this
    }

    private fun forceRequestCoffeeShop() {
        presenter.requestUserLocation()
    }

    override fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                hasApproximateLocationPermission()
    }

    override fun hasApproximateLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private val locationPermissionCallback = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                presenter.requestUserLocation()
                hideSnackBar()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                presenter.requestUserLocation()
                hideSnackBar()
            }
            else -> {
                // No location access granted.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(getResourceString(R.string.request_location))
                        .setPositiveButton(getResourceString(R.string.dialog_auth))
                        { _, _ -> presenter.requestLocationPermission() }
                        .setNegativeButton(getResourceString(R.string.dialog_cancel))
                        { _, _ -> showPermissionNeedSnackBar() }
                        .create()
                        .show()
                } else {
                    showPermissionNeedSnackBar()
                    val appName = getResourceString(R.string.app_name)
                    val permissionName = getResourceString(R.string.auth_location)

                    MaterialAlertDialogBuilder(this)
                        .setTitle(getResourceString(R.string.dialog_auth))
                        .setMessage(resources.getString(R.string.auth_yourself, appName, permissionName))
                        .setPositiveButton(getResourceString(R.string.auto_go))
                        { _, _ -> openApplicationSetting() }
                        .setNegativeButton(getResourceString(R.string.dialog_cancel))
                        { _, _ -> }
                        .create()
                        .show()
                }
            }
        }
    }

    override fun requestLocationPermission() {
        locationPermissionCallback.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun locationSettingNeedsResolution(resolvable: ResolvableApiException) {
        resolvable.startResolutionForResult(this, locationSettingResolution)
    }

    override fun showServiceUnavailableMessage() {
        val message = resources.getString(R.string.service_unavailable)
        makeSnackBar(message, true)
    }

    override fun makeSnackBar(message: String, infinity: Boolean) {
        val duration = if (infinity) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
        val snackbar = Snackbar.make(rootView, message, duration)
        snackbar.show()
        this.snackbar = snackbar
    }

    override fun makeSnackBar(stringRes: Int) {
        if (stringRes != -1) {
            val message = getString(stringRes)
            makeSnackBar(message, false)
        }
    }

    override fun setStatusBarDarkIndicator() {
        translucentStatusBar()
    }

    private fun translucentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // replace View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.setDecorFitsSystemWindows(false)
            // replace View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.insetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private val internetRequestCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val isNetworkAvailable = presenter.isNetworkAvailable()
        if (isNetworkAvailable) {
            if (checkLocationPermission()) {
                forceRequestCoffeeShop()
            } else {
                showPermissionNeedSnackBar()
            }
        } else {
            requestInternetConnection()
        }
    }

    override fun requestInternetConnection() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getResourceString(R.string.internet_request_title))
            .setMessage(getResourceString(R.string.internet_request_message))
            .setPositiveButton(getResourceString(R.string.auto_go)) { _, _ ->
                internetRequestCallback.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
            }.create().show()
    }

    override fun moveCameraToCurrentPosition(latLng: LatLng) {
        mapFragment?.moveCamera(latLng, MapsFragment.ZOOM_RATE)
    }

    override fun onCoffeeShopFetched(coffeeShops: List<CoffeeShop>) {
        mapFragment?.prepareCoffeeShops(coffeeShops)
    }

    override fun updateListPage(coffeeShops: List<CoffeeShop>) {
        if (!viewPagerAdapter.isListPageInitiated) {
            val listFragment = ListFragment.newInstance(coffeeShops)
            listFragment.setCallback(this)
            // FIXME:: Below line cause crash
            viewPagerAdapter.setListFragment(listFragment)
        }
    }

    override fun isApplicationInstalled(packageName: String): Boolean {
        val packageManager = packageManager
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun navigateToLocation(intent: Intent) {
        startActivity(intent)
    }

    override fun showNeedsGoogleMapMessage() {
        val message = resources.getString(R.string.googlemap_not_install)
        makeSnackBar(message, false)
    }

    override fun shareCoffeeShop(shareIntent: Intent) {
        val title = getResourceString(R.string.share_title)
        startActivity(Intent.createChooser(shareIntent, title))
    }

    private fun showPermissionNeedSnackBar() {
        val snackbar = Snackbar.make(rootView, R.string.permission_needed, Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(R.string.dialog_auth) { openApplicationSetting() }
        snackbar.show()
        this.snackbar = snackbar
    }

    private val enableLocationManuallyCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkLocationPermission()) {
            forceRequestCoffeeShop()
            hideSnackBar()
        } else {
            showPermissionNeedSnackBar()
        }
    }

    private fun hideSnackBar() {
        snackbar?.run {
            if (isShown) {
                dismiss()
            }
        }
    }

    private fun openApplicationSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        enableLocationManuallyCallback.launch(intent)
    }

    private fun getResourceString(stringId: Int): String {
        return resources.getString(stringId)
    }

    override fun showBottomSheetDetailView(coffeeShop: CoffeeShop) {
        if (this::bottomSheetViewPager.isInitialized) {
            if (viewPagerAdapter.isDetailPageInitiated) {
                val detailsFragment = viewPagerAdapter.getItem(ViewPagerAdapter.DETAIL_FRAGMENT)
                (detailsFragment as DetailsFragment).setDetailInfo(coffeeShop.getUiModel())
            } else {
                val detailsFragment = DetailsFragment.newInstance(coffeeShop)
                detailsFragment.detailsItemClickListener = this
                viewPagerAdapter.setDetailFragment(detailsFragment)
            }

            if (bottomSheetBehavior.state != AnchorBottomSheetBehavior.STATE_ANCHORED) {
                bottomSheetBehavior.state = AnchorBottomSheetBehavior.STATE_ANCHORED
            }

            listScrollToItemPosition(coffeeShop)
            bottomSheetViewPager.setCurrentItem(ViewPagerAdapter.DETAIL_FRAGMENT, true)
        }
    }

    private fun listScrollToItemPosition(coffeeShop: CoffeeShop) {
        if (viewPagerAdapter.isListPageInitiated) {
            val listFragment = viewPagerAdapter.getItem(ViewPagerAdapter.LIST_FRAGMENT)
            (listFragment as ListFragment).scrollToItemPosition(coffeeShop)
        }
    }

    override fun moveMapView(offset: Float) {
        mainContainer.translationY = offset
    }

    override fun onBackPressed() {
        if (this::bottomSheetBehavior.isInitialized) {
            if (bottomSheetViewPager.currentItem != 0) {
                bottomSheetViewPager.currentItem = 0

                when (detailViewState) {
                    is ViewState.EnterDetailInfoFromList -> {
                        if (bottomSheetBehavior.state != AnchorBottomSheetBehavior.STATE_ANCHORED) {
                            bottomSheetBehavior.state = AnchorBottomSheetBehavior.STATE_ANCHORED
                        }
                    }
                    is ViewState.EnterDetailInfoFromMap -> {
                        if (bottomSheetBehavior.state != AnchorBottomSheetBehavior.STATE_COLLAPSED) {
                            bottomSheetBehavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                    else -> {}
                }
                detailViewState = ViewState.Browsing
                return
            }
        }
        super.onBackPressed()
    }

    override fun getViewPagerBottomSheetBehavior(): AnchorBottomSheetBehavior<ViewPager> = AnchorBottomSheetBehavior.from(bottomSheetViewPager)

    //region MapsClickHandler
    override fun onMapClicked() {
        if (this::bottomSheetBehavior.isInitialized) {
            if (bottomSheetBehavior.state != AnchorBottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    override fun onMarkerClicked(coffeeShop: CoffeeShop) {
        presenter.setLastTappedCoffeeShop(coffeeShop)
        presenter.showDetailView()
        detailViewState = ViewState.EnterDetailInfoFromMap(coffeeShop)
    }
    //endregion

    //region ListFragment.Callback
    override fun onItemTapped(coffeeShop: CoffeeShop) {
        presenter.setLastTappedCoffeeShop(coffeeShop)
        presenter.showDetailView()
        detailViewState = ViewState.EnterDetailInfoFromList(coffeeShop)
        mapFragment?.setMarkerActive(coffeeShop)
    }
    //endregion

    //region DetailsItemClickListener
    override fun onNavigationButtonClicked() {
        presenter.prepareNavigation()
    }

    override fun onShareButtonClicked() {
        presenter.share(this)
    }

    override fun onBackButtonClicked() {
        onBackPressed()
    }
    //endregion

    //region View.OnClickListener
    override fun onClick(view: View) {
        when (view.id) {
            R.id.main_my_location_button -> {
                presenter.let {
                    val myLocation = it.currentLocation
                    val mapFragment = supportFragmentManager.findFragmentByTag(MapsFragment.TAG)
                            as MapsFragment
                    mapFragment.moveToMyLocation(myLocation)
                }
            }
        }
    }
    //endregion
}
