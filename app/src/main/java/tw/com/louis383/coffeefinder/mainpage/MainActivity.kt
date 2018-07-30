package tw.com.louis383.coffeefinder.mainpage

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import tw.com.louis383.coffeefinder.CoffeeTripApplication
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.about.AboutActivity
import tw.com.louis383.coffeefinder.adapter.ViewPagerAdapter
import tw.com.louis383.coffeefinder.details.DetailsFragment
import tw.com.louis383.coffeefinder.details.DetailsItemClickListener
import tw.com.louis383.coffeefinder.list.ListFragment
import tw.com.louis383.coffeefinder.maps.MapsClickHandler
import tw.com.louis383.coffeefinder.maps.MapsFragment
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop
import tw.com.louis383.coffeefinder.utils.Utils
import tw.com.louis383.coffeefinder.utils.bindView
import javax.inject.Inject

/**
 * Created by louis383 on 2017/2/17.
 */

class MainActivity : AppCompatActivity(), MainView, MapsClickHandler, ListFragment.Callback,
        DetailsItemClickListener, View.OnClickListener {
    private val locationPermissionRequest = 0
    private val locationManualEnable = 1
    private val locationSettingResolution = 2
    private val internetRequest = 3

    private var presenter: MainPresenter? = null
    private var snackbar: Snackbar? = null

    // Main Content
    private val rootView: CoordinatorLayout by bindView(R.id.main_rootview)
    private val mainContainer: FrameLayout by bindView(R.id.main_container)
    private val myLocationButton: ImageButton by bindView(R.id.main_my_location_button)

    // Bottom Sheet
    private lateinit var bottomSheetViewPager: ViewPager
    private lateinit var bottomSheetBehavior: AnchorBottomSheetBehavior<ViewPager>
    private val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

    override val activityContext: Context
        get() = this

    override val isInternetAvailable: Boolean
        get() {
            val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo
            return networkInfo?.run {
                isConnected && isAvailable
            } ?: false
        }

    private val mapFragment: MapsFragment?
        get() {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.main_container)
            return mapFragment as? MapsFragment
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Translucent)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as CoffeeTripApplication).appComponent.inject(this)

        initMapFragment()
        bottomSheetViewPager = findViewById(R.id.main_bottom_sheet)
        bottomSheetViewPager.adapter = viewPagerAdapter
        bottomSheetBehavior = AnchorBottomSheetBehavior.from(bottomSheetViewPager)
        bottomSheetBehavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED

        presenter?.attachView(this)
        presenter?.addLifecycleOwner(this)
        presenter?.setBottomSheetBehavior(bottomSheetBehavior)

        myLocationButton.setOnClickListener(this)
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

    @Inject
    fun initPresenter(coffeeShopListManager: CoffeeShopListManager) {
        val fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this)
        presenter = MainPresenter(coffeeShopListManager, fusedLocationProviderClient)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            locationPermissionRequest -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter?.requestUserLocation(true)
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        AlertDialog.Builder(this)
                                .setMessage(Utils.getResourceString(this, R.string.request_location))
                                .setPositiveButton(Utils.getResourceString(this, R.string.dialog_auth))
                                    { _, _ -> requestLocationPermission() }
                                .setNegativeButton(Utils.getResourceString(this, R.string.dialog_cancel))
                                    { _, _ -> showPermissionNeedSnackBar() }
                                .create()
                                .show()
                    } else {
                        showPermissionNeedSnackBar()
                        val appName = Utils.getResourceString(this, R.string.app_name)
                        val permissionName = Utils.getResourceString(this, R.string.auth_location)

                        AlertDialog.Builder(this)
                                .setTitle(Utils.getResourceString(this, R.string.dialog_auth))
                                .setMessage(resources.getString(R.string.auth_yourself, appName, permissionName))
                                .setPositiveButton(Utils.getResourceString(this, R.string.auto_go))
                                    { _, _ -> openApplicationSetting() }
                                .setNegativeButton(Utils.getResourceString(this, R.string.dialog_cancel))
                                    { _, _ -> }
                                .create()
                                .show()
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            internetRequest -> {
                if (isInternetAvailable) {
                    if (checkLocationPermission()) {
                        forceRequestCoffeeShop()
                    } else {
                        showPermissionNeedSnackBar()
                    }
                } else {
                    requestInternetConnection()
                }
            }
            locationManualEnable -> {
                if (checkLocationPermission()) {
                    forceRequestCoffeeShop()
                    snackbar?.run {
                        if (isShown) {
                            dismiss()
                        }
                    }
                } else {
                    showPermissionNeedSnackBar()
                }
            }
            locationSettingResolution -> {
                if (resultCode == Activity.RESULT_OK) {
                    presenter?.requestUserLocation(true)
                } else {
                    snackbar = Snackbar.make(rootView, R.string.high_accuracy_recommand, Snackbar.LENGTH_LONG)
                    snackbar?.show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> {
                // Go to about page!
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun forceRequestCoffeeShop() {
        presenter?.requestUserLocation(true)
    }

    override fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestLocationPermission() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions, locationPermissionRequest)
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

    override fun setStatusBarDarkIndicator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            translucentStatusBar()
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.secondary_bluegray)
        }
    }

    private fun translucentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun requestInternetConnection() {
        AlertDialog.Builder(this)
                .setTitle(getResourceString(R.string.internet_request_title))
                .setMessage(getResourceString(R.string.internet_request_message))
                .setPositiveButton(getResourceString(R.string.auto_go)) { _, _ ->
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivityForResult(intent, internetRequest)
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

    private fun openApplicationSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, locationManualEnable)
    }

    private fun getResourceString(stringId: Int): String {
        return resources.getString(stringId)
    }

    override fun showBottomSheetDetailView(coffeeShop: CoffeeShop) {
        if (this::bottomSheetViewPager.isInitialized) {
            if (viewPagerAdapter.isDetailPageInitiated) {
                val detailsFragment = viewPagerAdapter.getItem(ViewPagerAdapter.DETAIL_FRAGMENT)
                (detailsFragment as DetailsFragment).setDetailInfo(coffeeShop.viewModel)
            } else {
                val detailsFragment = DetailsFragment.newInstance(coffeeShop)
                viewPagerAdapter.setDetailFragment(detailsFragment)
            }
            bottomSheetViewPager.setCurrentItem(ViewPagerAdapter.DETAIL_FRAGMENT, true)
        }
    }

    override fun moveMapView(offset: Float) {
        mainContainer.translationY = offset
    }

    override fun onBackPressed() {
        if (this::bottomSheetBehavior.isInitialized) {
            if (bottomSheetViewPager.currentItem != 0) {
                bottomSheetViewPager.currentItem = 0
                return
            }

            if (bottomSheetBehavior.state != AnchorBottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED
                return
            }
        }

        super.onBackPressed()
    }

    //region MapsClickHandler
    override fun onMapClicked() {
        if (this::bottomSheetBehavior.isInitialized) {
            if (bottomSheetBehavior.state != AnchorBottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = AnchorBottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    override fun onMarkerClicked(coffeeShop: CoffeeShop) {
        presenter?.setLastTappedCoffeeShop(coffeeShop)
        presenter?.showDetailView()
    }
    //endregion

    //region ListFragment.Callback
    override fun onItemTapped(coffeeShop: CoffeeShop) {
        presenter?.setLastTappedCoffeeShop(coffeeShop)
        presenter?.showDetailView()

        mapFragment?.setMarkerActive(coffeeShop)
    }
    //endregion

    //region DetailsItemClickListener
    override fun onNavigationButtonClicked() {
        presenter?.prepareNavigation()
    }

    override fun onShareButtonClicked() {
        presenter?.share(this)
    }
    //endregion

    //region View.OnClickListener
    override fun onClick(view: View) {
        when (view.id) {
            R.id.main_my_location_button -> {
                presenter?.let {
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
