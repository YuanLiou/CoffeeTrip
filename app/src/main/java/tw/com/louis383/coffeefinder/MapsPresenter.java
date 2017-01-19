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

import java.util.List;

import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;
import tw.com.louis383.coffeefinder.model.entity.CoffeeTripAPI;

/**
 * Created by louis383 on 2017/1/13.
 */

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> implements LocationListener {

    private static final float ZOOM_RATE = 16f;
    private static final int UPDATE_INTERVAL = 10000;    // 10 Sec
    private static final int FASTEST_UPDATE_INTERVAL = 5000; // 5 Sec

    private static final int RANGE = 2000;    // 2m

    private GoogleApiClient googleApiClient;
    private CoffeeTripAPI coffeeTripAPI;
    private LocationRequest locationRequest;

    private Location currentLocation;
    private boolean isRequestingLocation;

    public MapsPresenter(GoogleApiClient googleApiClient, CoffeeTripAPI coffeeTripAPI) {
        this.googleApiClient = googleApiClient;
        this.coffeeTripAPI = coffeeTripAPI;
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

    public void fetchCoffeeShop() {
        if (currentLocation != null) {
            coffeeTripAPI.getCoffeeShops(currentLocation.getLatitude(), currentLocation.getLongitude(), RANGE)
                    .subscribe(listResponse -> {
                        if (listResponse.isSuccessful()) {
                            List<CoffeeShop> coffeeShops = listResponse.body();

                            if (!coffeeShops.isEmpty()) {
                                for (CoffeeShop shop : coffeeShops) {
                                    LatLng latLng = new LatLng(shop.getLatitude(), shop.getLongitude());

                                    int distance = (int) shop.getDistance();
                                    view.addMakers(latLng, shop.getName(), String.valueOf(distance));
                                }
                            } else {
                                view.showNoCoffeeShopDialog();
                            }
                        }
                    }, throwable -> {
                        view.makeCustomSnackbar(throwable.getLocalizedMessage());
                        Log.e("fetchingCoffeeShop", Log.getStackTraceString(throwable));
                    });
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
                        view.showServiceUnavailableMessage();
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

        fetchCoffeeShop();

        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        Log.i("MapsPresenter", "Location Updated latitude: " + currentLatLng.latitude + ", longitude: " + currentLatLng.longitude);

        view.moveCamera(currentLatLng, ZOOM_RATE);
    }
    //endregion

    public interface MapView {
        boolean isLocationPermissionGranted();
        void requestLocationPermission();
        void addMakers(LatLng latLng, String title, String snippet);
        void moveCamera(LatLng latLng, float zoom);
        void setupDetailedMapInterface();
        void locationSettingNeedsResolution(Status status);
        void showServiceUnavailableMessage();
        void makeCustomSnackbar(String message);
        void showNoCoffeeShopDialog();
    }
}
