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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.adapter.ViewPagerAdapter;
import tw.com.louis383.coffeefinder.list.ListFragment;
import tw.com.louis383.coffeefinder.maps.MapsFragment;
import tw.com.louis383.coffeefinder.model.CoffeeTripAPI;
import tw.com.louis383.coffeefinder.utils.ChromeCustomTabsHelper;
import tw.com.louis383.coffeefinder.utils.Utils;

/**
 * Created by louis383 on 2017/2/17.
 */

public class MainActivity extends AppCompatActivity implements MainPresenter.MainView, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int LOCATION_PERMISSION_REQUEST = 0;
    private static final int LOCATION_MANUAL_ENABLE = 1;
    private static final int LOCATION_SETTING_RESOLUTION = 2;

    private GoogleApiClient googleApiClient;
    private ChromeCustomTabsHelper customTabsHelper;

    private MainPresenter presenter;
    private ViewPagerAdapter adapter;

    private CoordinatorLayout rootView;
    private Toolbar toolbar;
    private Snackbar snackbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = (CoordinatorLayout) findViewById(R.id.main_rootview);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        tabLayout = (TabLayout) findViewById(R.id.main_tabbar);
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        init();

        buildGoogleAPIClient();
        CoffeeTripAPI coffeeTripAPI = new CoffeeTripAPI();

        presenter = new MainPresenter(googleApiClient, coffeeTripAPI);
        presenter.attachView(this);

        customTabsHelper = new ChromeCustomTabsHelper();
    }

    private void init() {
        List<Fragment> fragments = new ArrayList<>();

        MapsFragment mapsFragment = MapsFragment.newInstance();
        fragments.add(ViewPagerAdapter.MAP_FRAGMENT, mapsFragment);

        ListFragment listFragment = ListFragment.newInstance();
        fragments.add(ViewPagerAdapter.LIST_FRAGMENT, listFragment);

        tabLayout.addTab(tabLayout.newTab().setText(getResourceString(R.string.tab_title_map)));
        tabLayout.addTab(tabLayout.newTab().setText(getResourceString(R.string.tab_title_list)));

        adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        toolbar.setTitle(getResourceString(R.string.app_name));
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
                if (isLocationPermissionGranted()) {
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
    public boolean isLocationPermissionGranted() {
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
}
