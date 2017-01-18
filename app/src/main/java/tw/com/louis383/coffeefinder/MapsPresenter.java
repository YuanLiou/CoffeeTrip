package tw.com.louis383.coffeefinder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.util.Log;

/**
 * Created by louis383 on 2017/1/13.
 */

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> implements LocationListener {

    private static final float ZOOM_RATE = 16f;
    private static final int UPDATE_INTERVAL = 10000;    // 10 Sec
    private static final int FASTEST_UPDATE_INTERVAL = 5000; // 5 Sec

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private Location currentLocation;
    private boolean isRequestingLocation;

    public MapsPresenter(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    @Override
    public void attachView(MapView view) {
        super.attachView(view);

        if (!view.isLocationPermissionGranted()) {
            view.requestLocationPermission();
        }
    }

    public void requestUserLocation() {
        if (view.isLocationPermissionGranted()) {
            currentLocation = getLastLocation();
            if (currentLocation != null) {
                LatLng lastLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                view.moveCamera(lastLatLng, ZOOM_RATE);

                Log.i("MapsPresenter", "lastLocation latitude: " + lastLatLng.latitude + ", longitude: " + lastLatLng.longitude);
            }

            tryToGetAccurateLocation();
        }
    }

    public void activityResume() {
        if (googleApiClient.isConnected() && isRequestingLocation) {
            requestUserLocation();
        }
    }

    public void activityPause() {
        if (googleApiClient.isConnected()) {
            stopLocationUpdate();
        }
    }

    // Doing permission checking at Activity. When the method is called, it must have granted location permission.
    @SuppressWarnings("MissingPermission")
    private Location getLastLocation() {
        if (googleApiClient != null) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            return location;
        }

        return null;
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void tryToGetAccurateLocation() {
        if (googleApiClient != null) {
            if (locationRequest == null) {
                buildLocationRequest();
            }

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

            result.setResultCallback(locationSettingsResult -> {
                Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (!isRequestingLocation) {
                            startLocationUpdate();
                        }
                        Log.i("MapsPresenter", "Succeed.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        view.locationSettingNeedsResolution(status);
                        Log.i("MapsPresenter", "Resolution Required");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        view.showServiceUnavaliableSnackBar();
                        Log.i("MapsPresenter", "unavailable.");
                        break;
                }
            });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdate() {
        isRequestingLocation = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void stopLocationUpdate() {
        isRequestingLocation = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    //region LocationListener
    @Override
    public void onLocationChanged(Location location) {
        this.currentLocation = location;
        stopLocationUpdate();    // Only get one time accurate position.

        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        Log.i("MapsPresenter", "Location Updated latitude: " + currentLatLng.latitude + ", longitude: " + currentLatLng.longitude);

        view.moveCamera(currentLatLng, ZOOM_RATE);
    }
    //endregion

    public interface MapView {
        boolean isLocationPermissionGranted();
        void requestLocationPermission();
        void addMakers(LatLng latLng, String title);
        void moveCamera(LatLng latLng, float zoom);
        void setupDetailedMapInterface();
        void locationSettingNeedsResolution(Status status);
        void showServiceUnavaliableSnackBar();
    }
}
