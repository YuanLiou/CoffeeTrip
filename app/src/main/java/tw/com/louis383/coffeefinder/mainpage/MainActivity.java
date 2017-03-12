package tw.com.louis383.coffeefinder.mainpage;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.com.louis383.coffeefinder.CoffeeTripApplication;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.adapter.ViewPagerAdapter;
import tw.com.louis383.coffeefinder.list.ListFragment;
import tw.com.louis383.coffeefinder.maps.MapsClickHandler;
import tw.com.louis383.coffeefinder.maps.MapsFragment;
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;
import tw.com.louis383.coffeefinder.utils.ChromeCustomTabsHelper;
import tw.com.louis383.coffeefinder.utils.Utils;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;

/**
 * Created by louis383 on 2017/2/17.
 */

public class MainActivity extends AppCompatActivity implements MainPresenter.MainView, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MapsClickHandler, ListFragment.Callback {

    private static final int LOCATION_PERMISSION_REQUEST = 0;
    private static final int LOCATION_MANUAL_ENABLE = 1;
    private static final int LOCATION_SETTING_RESOLUTION = 2;

    private GoogleApiClient googleApiClient;
    private ChromeCustomTabsHelper customTabsHelper;

    private MainPresenter presenter;
    private ViewPagerAdapter adapter;
    private Snackbar snackbar;
    private BottomSheetBehavior bottomSheetBehavior;

    private boolean isAppbarVisible = false;

    // Main Content
    @BindView(R.id.main_rootview) CoordinatorLayout rootView;
    @BindView(R.id.main_toolbar) Toolbar toolbar;
    @BindView(R.id.main_appbar) AppBarLayout appbarLayout;
    @BindView(R.id.main_tabbar) TabLayout tabLayout;
    @BindView(R.id.main_viewpager) ViewPager viewPager;
    @BindView(R.id.main_bottom_sheet) NestedScrollView bottomSheet;

    // Bottom Sheet
    @BindView(R.id.detail_view_title) TextView bottomSheetTitle;
    @BindView(R.id.detail_view_distance) TextView bottomSheetDistance;
    @BindView(R.id.detail_view_wifi_score) TextView bottomSheetWifiScore;
    @BindView(R.id.detail_view_seat_score) TextView bottomSheetSeatScore;
    @BindView(R.id.detail_view_limited_time) TextView bottomSheetLimitedTime;
    @BindView(R.id.detail_view_socket) TextView bottomSheetSocket;
    @BindView(R.id.detail_view_standing_desk) TextView bottomSheetStandingDesk;
    @BindView(R.id.detail_view_opentime) TextView bottomSheetOpenTime;
    @BindView(R.id.detail_view_mrt) TextView bottomSheetMrt;
    @BindView(R.id.detail_view_expense) AppCompatRatingBar bottomSheetExpensebar;
    @BindView(R.id.detail_view_wifi_quality) ProgressBar bottomSheetWifiQuality;
    @BindView(R.id.detail_view_seat_quality) ProgressBar bottomSheetSeatQuality;
    @BindView(R.id.detail_view_button_navigate) Button bottomSheetNavigate;
    @BindView(R.id.detail_view_button_share) Button bottomSheetShare;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((CoffeeTripApplication) getApplication()).getAppComponent().inject(this);
        ButterKnife.bind(this);

        init();
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        presenter.attachView(this);
        presenter.setBottomSheetBehavior(bottomSheetBehavior);

        bottomSheetNavigate.setOnClickListener(v -> presenter.prepareNavigation());
        bottomSheetShare.setOnClickListener(v -> presenter.share(MainActivity.this));

        customTabsHelper = new ChromeCustomTabsHelper();
    }

    private void init() {
        List<Fragment> fragments = new ArrayList<>();

        MapsFragment mapsFragment = MapsFragment.newInstance();
        mapsFragment.setRetainInstance(true);
        mapsFragment.setMapClickHandler(this);
        fragments.add(ViewPagerAdapter.MAP_FRAGMENT, mapsFragment);

        ListFragment listFragment = ListFragment.newInstance();
        listFragment.setCallback(this);
        fragments.add(ViewPagerAdapter.LIST_FRAGMENT, listFragment);

        tabLayout.addTab(tabLayout.newTab().setText(getResourceString(R.string.tab_title_map)));
        tabLayout.addTab(tabLayout.newTab().setText(getResourceString(R.string.tab_title_list)));

        adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        toolbar.setTitle(getResourceString(R.string.app_name));
    }

    @Inject
    public void initPresenter(CoffeeShopListManager coffeeShopListManager) {
        buildGoogleAPIClient();
        presenter = new MainPresenter(googleApiClient, coffeeShopListManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        customTabsHelper.bindCustomTabsServices(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onActivityPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
        customTabsHelper.unbindCustomTabsServices(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (googleApiClient.isConnected()) {
                        presenter.requestUserLocation(true);
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        new AlertDialog.Builder(this)
                                .setMessage(Utils.getResourceString(this, R.string.request_location))
                                .setPositiveButton(Utils.getResourceString(this, R.string.dialog_auth),
                                        (dialog, which) -> requestLocationPermission())
                                .setNegativeButton(Utils.getResourceString(this, R.string.dialog_cancel),
                                        (dialog, which) -> showPermissionNeedSnackBar())
                                .create().show();
                    } else {
                        showPermissionNeedSnackBar();
                        String appName = Utils.getResourceString(this, R.string.app_name);
                        String permissionName = Utils.getResourceString(this, R.string.auth_location);

                        new AlertDialog.Builder(this)
                                .setTitle(Utils.getResourceString(this, R.string.dialog_auth))
                                .setMessage(getResources().getString(R.string.auth_yourself, appName, permissionName))
                                .setPositiveButton(Utils.getResourceString(this, R.string.auto_go),
                                        (dialog, which) -> {
                                            openApplicationSetting();
                                        })
                                .setNegativeButton(Utils.getResourceString(this, R.string.dialog_cancel), (dialog, which) -> {})
                                .create().show();
                    }
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
            case LOCATION_MANUAL_ENABLE:
                if (checkLocationPermission()) {
                    if (googleApiClient.isConnected()) {
                        presenter.requestUserLocation(true);
                    } else {
                        googleApiClient.connect();
                    }

                    if (snackbar != null && snackbar.isShown()) {
                        snackbar.dismiss();
                    }

                } else {
                    showPermissionNeedSnackBar();
                }
                break;
            case LOCATION_SETTING_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    presenter.requestUserLocation(true);
                } else {
                    snackbar = Snackbar.make(rootView, R.string.high_accuracy_recommand, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestLocationPermission() {
        String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION };
        ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void locationSettingNeedsResolution(Status status) {
        try {
            status.startResolutionForResult(this, LOCATION_SETTING_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e("MainActivity", Log.getStackTraceString(e));
        }
    }

    @Override
    public void showServiceUnavailableMessage() {
        String message = getResources().getString(R.string.service_unavailable);
        makeSnackBar(message, true);
    }

    @Override
    public void makeSnackBar(String message, boolean infinity) {
        int duration = infinity ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG;
        snackbar = Snackbar.make(rootView, message, duration);
        snackbar.show();
    }

    @Override
    public void setStatusBarDarkIndicator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public Location getCurrentLocation() {
        return presenter.getCurrentLocation();
    }

    @Override
    public void moveCameraToCurrentPosition(LatLng latLng) {
        MapsFragment mapsFragment = (MapsFragment) adapter.getItem(ViewPagerAdapter.MAP_FRAGMENT);
        mapsFragment.moveCamera(latLng, MapsFragment.ZOOM_RATE);
    }

    @Override
    public void onCoffeeShopFetched(List<CoffeeShop> coffeeShops) {
        MapsFragment mapsFragment = (MapsFragment) adapter.getItem(ViewPagerAdapter.MAP_FRAGMENT);
        mapsFragment.prepareCoffeeShops(coffeeShops);

        ListFragment listFragment = (ListFragment) adapter.getItem(ViewPagerAdapter.LIST_FRAGMENT);
        listFragment.prepareCoffeeShops(coffeeShops);
    }

    @Override
    public boolean isApplicationInstalled(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void navigateToLocation(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void showNeedsGoogleMapMessage() {
        String message = getResources().getString(R.string.googlemap_not_install);
        makeSnackBar(message, false);
    }

    @Override
    public void shareCoffeeShop(Intent shareIntent) {
        String title = getResourceString(R.string.share_title);
        startActivity(Intent.createChooser(shareIntent, title));
    }

    private synchronized void buildGoogleAPIClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void showPermissionNeedSnackBar() {
        if (snackbar == null) {
            snackbar = Snackbar.make(rootView, R.string.permission_needed, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.dialog_auth, v -> openApplicationSetting());
        }
        snackbar.show();
    }

    private void openApplicationSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, LOCATION_MANUAL_ENABLE);
    }

    private String getResourceString(int stringId) {
        return getResources().getString(stringId);
    }

    @Override
    public void showBottomSheetDetailView(CoffeeShopViewModel viewModel) {
        if (bottomSheet != null && bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            bottomSheetTitle.setText(viewModel.getShopName());
            bottomSheetDistance.setText(viewModel.getDistances());
            bottomSheetExpensebar.setRating(viewModel.getCheapPoints());

            bottomSheetWifiQuality.setProgress((int) viewModel.getWifiPoints() * 20);
            bottomSheetWifiScore.setText(String.valueOf(viewModel.getWifiPoints()));
            bottomSheetSeatQuality.setProgress((int) viewModel.getSeatPoints() * 20);
            bottomSheetSeatScore.setText(String.valueOf(viewModel.getSeatPoints()));

            bottomSheetOpenTime.setText(viewModel.getOpenTimes(this));
            bottomSheetMrt.setText(viewModel.getMrtInfo(this));

            bottomSheetLimitedTime.setText(viewModel.getLimitTimeString(this));
            bottomSheetSocket.setText(viewModel.getSocketString(this));
            bottomSheetStandingDesk.setText(viewModel.getStandingDeskString(this));
        }
    }

    @Override
    public void hideAppbar(boolean hide) {
        isAppbarVisible = !hide;
        if (hide) {
            appbarLayout.animate().translationY(-appbarLayout.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        } else {
            appbarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    @Override
    public boolean isAppbarVisible() {
        return isAppbarVisible;
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior != null) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return;
            } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }
        }

        super.onBackPressed();
    }

    //region GoogleAPIClient.ConnectionCallback
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        presenter.requestUserLocation(false);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("MainActivity", "onConnectionSuspended");
    }
    //endregion

    //region GoogleAPIClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("MapsFragment", "onConnectionFailed: " + connectionResult.getErrorMessage());
    }
    //endregion

    //region MapsClickHandler
    @Override
    public void onMapClicked() {
        if (!isAppbarVisible && bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void onMarkerClicked(CoffeeShop coffeeShop) {
        presenter.setLastTappedCoffeeShop(coffeeShop);
        presenter.showDetailView();
    }
    //endregion

    //region ListFragment.Callback
    @Override
    public void onItemTapped(CoffeeShop coffeeShop) {
        presenter.setLastTappedCoffeeShop(coffeeShop);
        presenter.showDetailView();
    }
    //endregion
}
