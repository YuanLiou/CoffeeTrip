package tw.com.louis383.coffeefinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapsPresenter.MapView, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_PERMISSION_REQUEST = 0;
    private static final int LOCATION_MANUAL_ENABLE = 1;

    private GoogleMap googleMap;
    private MapsPresenter presenter;
    private GoogleApiClient googleApiClient;

    private CoordinatorLayout rootView;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        rootView = (CoordinatorLayout) findViewById(R.id.map_rootview);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        presenter = new MapsPresenter();
        presenter.attachView(this);
        presenter.setGoogleApiClient(googleApiClient);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleMap != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleApiClient.connect();
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
                } else {
                    showSnackBar();
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
                                            openApplicaionSetting();
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
    public void addMakers(LatLng latLng, String title) {
//        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    }

    @Override
    public void moveCamera(LatLng latLng, float zoom) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void setupDetailedMapInterface() {

    }

    private void openApplicaionSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, LOCATION_MANUAL_ENABLE);
    }

    private void showSnackBar() {
        if (snackbar == null) {
            snackbar = Snackbar.make(rootView, R.string.permission_needed, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.dialog_auth, v -> openApplicaionSetting());
        }
        snackbar.show();
    }

    //region ConnectionCallback
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (isLocationPermissionGranted()) {
            presenter.requestUserLocation();
        }
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
