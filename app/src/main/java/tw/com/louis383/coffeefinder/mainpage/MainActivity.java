package tw.com.louis383.coffeefinder.mainpage;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import javax.inject.Inject;
import tw.com.louis383.coffeefinder.CoffeeTripApplication;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.about.AboutActivity;
import tw.com.louis383.coffeefinder.list.ListFragment;
import tw.com.louis383.coffeefinder.maps.MapsClickHandler;
import tw.com.louis383.coffeefinder.maps.MapsFragment;
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;
import tw.com.louis383.coffeefinder.utils.Utils;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;

/**
 * Created by louis383 on 2017/2/17.
 */

public class MainActivity extends AppCompatActivity implements MainPresenter.MainView,  MapsClickHandler, ListFragment.Callback {
    private static final int LOCATION_PERMISSION_REQUEST = 0;
    private static final int LOCATION_MANUAL_ENABLE = 1;
    private static final int LOCATION_SETTING_RESOLUTION = 2;
    private static final int INTERNET_REQUEST = 3;

    private MainPresenter presenter;
    private Snackbar snackbar;
    private BottomSheetBehavior bottomSheetBehavior;

    // Main Content
    private CoordinatorLayout rootView;
    private NestedScrollView bottomSheet;
    private View shadow;

    // Bottom Sheet
    private FloatingActionButton navigationFab;
    private TextView bottomSheetTitle;
    private TextView bottomSheetDistance;
    private TextView bottomSheetWifiScore;
    private TextView bottomSheetSeatScore;
    private TextView bottomSheetLimitedTime;
    private TextView bottomSheetSocket;
    private TextView bottomSheetStandingDesk;
    private TextView bottomSheetOpenTime;
    private TextView bottomSheetWebsite;
    private TextView bottomSheetMrt;
    private AppCompatRatingBar bottomSheetExpensebar;
    private ProgressBar bottomSheetWifiQuality;
    private ProgressBar bottomSheetSeatQuality;
    private Button bottomSheetNavigate;
    private Button bottomSheetShare;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((CoffeeTripApplication) getApplication()).getAppComponent().inject(this);

        // Main Content
        rootView = (CoordinatorLayout) findViewById(R.id.main_rootview);
        bottomSheet = (NestedScrollView) findViewById(R.id.main_bottom_sheet);
        shadow = findViewById(R.id.main_shadow);

        // Bottom Sheet
        navigationFab = (FloatingActionButton) findViewById(R.id.main_fab);
        bottomSheetTitle = (TextView) findViewById(R.id.detail_view_title);
        bottomSheetDistance = (TextView) findViewById(R.id.detail_view_distance);
        bottomSheetWifiScore = (TextView) findViewById(R.id.detail_view_wifi_score);
        bottomSheetSeatScore = (TextView) findViewById(R.id.detail_view_seat_score);
        bottomSheetLimitedTime = (TextView) findViewById(R.id.detail_view_limited_time);
        bottomSheetSocket = (TextView) findViewById(R.id.detail_view_socket);
        bottomSheetStandingDesk = (TextView) findViewById(R.id.detail_view_standing_desk);
        bottomSheetOpenTime = (TextView) findViewById(R.id.detail_view_opentime);
        bottomSheetWebsite = (TextView) findViewById(R.id.detail_view_website);
        bottomSheetMrt = (TextView) findViewById(R.id.detail_view_mrt);
        bottomSheetExpensebar = (AppCompatRatingBar) findViewById(R.id.detail_view_expense);
        bottomSheetWifiQuality = (ProgressBar) findViewById(R.id.detail_view_wifi_quality);
        bottomSheetSeatQuality = (ProgressBar) findViewById(R.id.detail_view_seat_quality);
        bottomSheetNavigate = (Button) findViewById(R.id.detail_view_button_navigate);
        bottomSheetShare = (Button) findViewById(R.id.detail_view_button_share);

        init();
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        presenter.attachView(this);
        presenter.addLifecycleOwner(this);
        presenter.setBottomSheetBehavior(bottomSheetBehavior);

        bottomSheetNavigate.setOnClickListener(v -> presenter.prepareNavigation());
        bottomSheetShare.setOnClickListener(v -> presenter.share(MainActivity.this));
    }

    private void init() {
        MapsFragment mapsFragment = MapsFragment.newInstance();
        mapsFragment.setRetainInstance(true);
        mapsFragment.setMapClickHandler(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, mapsFragment)
                .commit();
    }

    @Inject
    public void initPresenter(CoffeeShopListManager coffeeShopListManager) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this);
        presenter = new MainPresenter(coffeeShopListManager, fusedLocationProviderClient);
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.requestUserLocation(true);
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
            case INTERNET_REQUEST:
                if (isInternetAvailable()) {
                    if (checkLocationPermission()) {
                        forceRequestCoffeeShop();
                    } else {
                        showPermissionNeedSnackBar();
                    }
                } else {
                    requestInternetConnection();
                }
                break;
            case LOCATION_MANUAL_ENABLE:
                if (checkLocationPermission()) {
                    forceRequestCoffeeShop();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                // Go to about page!
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void forceRequestCoffeeShop() {
        presenter.requestUserLocation(true);
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
    public void locationSettingNeedsResolution(ResolvableApiException resolvable) {
        try {
            resolvable.startResolutionForResult(this, LOCATION_SETTING_RESOLUTION);
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

    @Override
    public void requestInternetConnection() {
        new AlertDialog.Builder(this)
            .setTitle(getResourceString(R.string.internet_request_title))
            .setMessage(getResourceString(R.string.internet_request_message))
            .setPositiveButton(getResourceString(R.string.auto_go), (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intent, INTERNET_REQUEST);
            }).create().show();
    }

    public Location getCurrentLocation() {
        return presenter.getCurrentLocation();
    }

    @Override
    public void moveCameraToCurrentPosition(LatLng latLng) {
        MapsFragment mapsFragment = getMapFragment();
        if (mapsFragment != null) {
            mapsFragment.moveCamera(latLng, MapsFragment.ZOOM_RATE);
        }
    }

    @Override
    public void onCoffeeShopFetched(List<CoffeeShop> coffeeShops) {
        MapsFragment mapsFragment = getMapFragment();
        if (mapsFragment != null) {
            mapsFragment.prepareCoffeeShops(coffeeShops);
        }
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

            bottomSheetWebsite.setText(viewModel.getWebsiteURL(this));
            bottomSheetOpenTime.setText(viewModel.getOpenTimes(this));
            bottomSheetMrt.setText(viewModel.getMrtInfo(this));

            bottomSheetLimitedTime.setText(viewModel.getLimitTimeString(this));
            bottomSheetSocket.setText(viewModel.getSocketString(this));
            bottomSheetStandingDesk.setText(viewModel.getStandingDeskString(this));
        }
    }

    @Override
    public void showFab(boolean show) {
        if (show) {
            navigationFab.animate().scaleX(1).scaleY(1).setDuration(300)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (!navigationFab.isShown()) {
                                navigationFab.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onAnimationEnd(Animator animation) {}
                        @Override
                        public void onAnimationCancel(Animator animation) {}
                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    })
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        } else {
            navigationFab.animate().scaleX(0).scaleY(0).setDuration(200).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    @Override
    public void setFloatingActionButtonEnable(boolean enable) {
        if (!enable) {
            navigationFab.setOnClickListener(null);
            navigationFab.setVisibility(View.GONE);
        } else {
            navigationFab.setOnClickListener(v -> presenter.prepareNavigation());
        }

        navigationFab.setEnabled(enable);
        navigationFab.setClickable(enable);
    }

    @Override
    public void setShadowAlpha(float offset) {
        if (offset > 0.0f) {
            float alpha = offset * 0.4f;
            shadow.setAlpha(alpha);
        }

        shadow.setVisibility(offset > 0.0f ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isInternetAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior != null) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheet.fullScroll(View.FOCUS_UP);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return;
            } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                showFab(false);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }
        }

        super.onBackPressed();
    }

    //region MapsClickHandler
    @Override
    public void onMapClicked() {
        if (bottomSheetBehavior != null) {
            showFab(false);
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

        MapsFragment mapsFragment = getMapFragment();
        if (mapsFragment != null) {
            mapsFragment.setMarkerActive(coffeeShop);
        }
    }
    //endregion

    @Nullable
    private MapsFragment getMapFragment() {
        Fragment mapFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (mapFragment instanceof MapsFragment) {
            return (MapsFragment) mapFragment;
        } else {
            return null;
        }
    }
}
