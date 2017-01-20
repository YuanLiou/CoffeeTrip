package tw.com.louis383.coffeefinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import tw.com.louis383.coffeefinder.model.entity.CoffeeTripAPI;
import tw.com.louis383.coffeefinder.utils.ChromeCustomTabsHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapsPresenter.MapView, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_PERMISSION_REQUEST = 0;
    private static final int LOCATION_MANUAL_ENABLE = 1;
    private static final int LOCATION_SETTING_RESOLUTION = 2;

    private GoogleMap googleMap;
    private MapsPresenter presenter;
    private GoogleApiClient googleApiClient;
    private ChromeCustomTabsHelper customTabsHelper;

    private CoordinatorLayout rootView;
    private Snackbar snackbar;

    private boolean mapInterfaceInitiated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        rootView = (CoordinatorLayout) findViewById(R.id.map_rootview);

        buildGoogleAPIClient();
        CoffeeTripAPI coffeeTripAPI = new CoffeeTripAPI();

        presenter = new MapsPresenter(googleApiClient, coffeeTripAPI);
        presenter.attachView(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        customTabsHelper = new ChromeCustomTabsHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabsHelper.bindCustomTabsServices(this);
        if (googleMap != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.activityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.activityPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabsHelper.unbindCustomTabsServices(this);
        googleApiClient.disconnect();
    }

    private synchronized void buildGoogleAPIClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleApiClient.connect();

        presenter.setGoogleMap(googleMap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCATION_MANUAL_ENABLE:
                if (isLocationPermissionGranted() && googleMap != null) {
                    if (googleApiClient.isConnected()) {
                        presenter.requestUserLocation();
                    } else {
                        googleApiClient.connect();
                    }

                    if (snackbar != null && snackbar.isShown()) {
                        snackbar.dismiss();
                    }

                } else {
                    showSnackBar();
                }
                break;
            case LOCATION_SETTING_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    presenter.requestUserLocation();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (googleApiClient.isConnected() && googleMap != null) {
                        presenter.requestUserLocation();
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        new AlertDialog.Builder(this)
                                .setMessage(Utils.getResourceString(this, R.string.request_location))
                                .setPositiveButton(Utils.getResourceString(this, R.string.dialog_auth),
                                        (dialog, which) -> requestLocationPermission())
                                .setNegativeButton(Utils.getResourceString(this, R.string.dialog_cancel),
                                        (dialog, which) -> showSnackBar())
                                .create().show();
                    } else {
                        showSnackBar();
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
    public boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestLocationPermission() {
        String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION };
        ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void addMakers(LatLng latLng, String title, String snippet, String id) {
        String distance = getResources().getString(R.string.unit_m, snippet);
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(distance));
        marker.setTag(id);
    }

    @Override
    public void moveCamera(LatLng latLng, float zoom) {
        if (!mapInterfaceInitiated) {
            mapInterfaceInitiated = true;
            setupDetailedMapInterface();
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        googleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void openWebsite(Uri uri) {
        CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabIntent = customTabBuilder.build();

        ChromeCustomTabsHelper.openCustomTab(this, customTabIntent, uri, (activity, uri1) -> {
            // TODO:: a webview page to open a link.
            Intent intent = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(intent);
        });
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void setupDetailedMapInterface() {
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);

        UiSettings mapUISettings = googleMap.getUiSettings();
        mapUISettings.setRotateGesturesEnabled(false);
        mapUISettings.setTiltGesturesEnabled(false);
    }

    @Override
    public void locationSettingNeedsResolution(Status status) {
        try {
            status.startResolutionForResult(this, LOCATION_SETTING_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e("MapsActivity", Log.getStackTraceString(e));
        }
    }

    @Override
    public void showServiceUnavailableMessage() {
        String message = getResources().getString(R.string.service_unavailable);
        makeCustomSnackbar(message);
    }

    @Override
    public void makeCustomSnackbar(String message) {
        snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    @Override
    public void showNoCoffeeShopDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.dialog_no_coffeeshop_title));
        alertDialogBuilder.setMessage(getResources().getString(R.string.dialog_no_coffeeshop_message));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.dialog_no_coffeeshop_ok), (dialog, which) -> {});
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    private void openApplicationSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, LOCATION_MANUAL_ENABLE);
    }

    private void showSnackBar() {
        if (snackbar == null) {
            snackbar = Snackbar.make(rootView, R.string.permission_needed, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.dialog_auth, v -> openApplicationSetting());
        }
        snackbar.show();
    }

    //region ConnectionCallback
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        presenter.requestUserLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("MapsActivity", "onConnectionSuspended");
    }
    //endregion

    //region OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("MapsActivity", "onConnectionFailed: " + connectionResult.getErrorMessage());
    }
    //endregion
}
