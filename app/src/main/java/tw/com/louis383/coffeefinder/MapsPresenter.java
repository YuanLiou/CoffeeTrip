package tw.com.louis383.coffeefinder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
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

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> {

    private static final float ZOOM_RATE = 15f;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    @Override
    public void attachView(MapView view) {
        super.attachView(view);

        if (!view.isLocationPermissionGranted()) {
            view.requestLocationPermission();
        }
    }

    public void requestUserLocation() {
        if (view.isLocationPermissionGranted()) {
            Location lastLocation = getLastLocation();
            if (lastLocation != null) {
                LatLng lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                view.moveCamera(lastLatLng, ZOOM_RATE);
                view.setupDetailedMapInterface();

                Log.i("MapsPresenter", "lastLocation latitude: " + lastLatLng.latitude + ", longitude: " + lastLatLng.longitude);
            }

            checkAccurateLocationRequestAbility();
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

    private void checkAccurateLocationRequestAbility() {
        if (googleApiClient != null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);    // 10 Sec
            locationRequest.setFastestInterval(5000);    // 5 Sec
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

            result.setResultCallback(locationSettingsResult -> {
                Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // initialize Location Here
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        view.locationSettingNeedsResolution(status);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        view.showServiceUnavaliableSnackBar();
                        break;
                }
            });
        }
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

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
