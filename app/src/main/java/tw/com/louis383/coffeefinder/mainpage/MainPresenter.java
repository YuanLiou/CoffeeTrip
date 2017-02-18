package tw.com.louis383.coffeefinder.mainpage;

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

import java.util.ArrayList;
import java.util.List;

import tw.com.louis383.coffeefinder.BasePresenter;
import tw.com.louis383.coffeefinder.model.CoffeeTripAPI;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/2/17.
 */

public class MainPresenter extends BasePresenter<MainPresenter.MainView> implements LocationListener {

    private static final int UPDATE_INTERVAL = 10000;    // 10 Sec
    private static final int FASTEST_UPDATE_INTERVAL = 5000; // 5 Sec
    private static final int RANGE = 2000;    // 2m

    private GoogleApiClient apiClient;
    private CoffeeTripAPI coffeeTripAPI;
    private LocationRequest locationRequest;

    private Location currentLocation;
    private boolean isRequestingLocation;

    private List<CoffeeShop> coffeeShops;

    public MainPresenter(GoogleApiClient apiClient, CoffeeTripAPI coffeeTripAPI) {
        this.apiClient = apiClient;
        this.coffeeTripAPI = coffeeTripAPI;
    }

    @Override
    public void attachView(MainView view) {
        super.attachView(view);

        coffeeShops = new ArrayList<>();
        if (!view.isLocationPermissionGranted()) {
            view.requestLocationPermission();
        }
    }

    public void requestUserLocation(boolean force) {
        // Prevent request twice current location
        if (currentLocation != null && !force) {
            return;
        }

        if (view.isLocationPermissionGranted()) {
            currentLocation = getLastLocation();
            if (currentLocation != null) {
                LatLng lastLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                // TODO:: Move Google Map Camera
                Log.i("MainPresenter", "lastLocation latitude: " + lastLatLng.latitude + ", longitude: " + lastLatLng.longitude);
            }

            tryToGetAccurateLocation();
        }
    }

    public void onActivityPause() {
        if (apiClient.isConnected()) {
            stopLocationUpdate();
        }
    }

    // Doing permission checking at Activity. When the method is called, it must have granted location permission.
    @SuppressWarnings("MissingPermission")
    private Location getLastLocation() {
        if (apiClient != null) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
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
        if (apiClient != null) {
            if (locationRequest == null) {
                buildLocationRequest();
            }

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build());

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

    public void fetchCoffeeShop() {
        if (currentLocation != null) {
            coffeeTripAPI.getCoffeeShops(currentLocation.getLatitude(), currentLocation.getLongitude(), RANGE)
                    .subscribe(listResponse -> {
                        if (listResponse.isSuccessful()) {
                            coffeeShops = new ArrayList<>();
                            coffeeShops.addAll(listResponse.body());

                        }
                    }, throwable -> {
                        Log.e("fetchingCoffeeShop", Log.getStackTraceString(throwable));
                    });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdate() {
        isRequestingLocation = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
    }

    private void stopLocationUpdate() {
        isRequestingLocation = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
    }

    //region LocationChanged
    @Override
    public void onLocationChanged(Location location) {
        this.currentLocation = location;
//        stopLocationUpdate();    // Only get one time accurate position.
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        Log.i("MainPresenter", "Location Updated latitude: " + currentLatLng.latitude + ", longitude: " + currentLatLng.longitude);
    }
    //endregion

    public interface MainView {
        boolean isLocationPermissionGranted();
        void requestLocationPermission();
        void locationSettingNeedsResolution(Status status);
        void showServiceUnavailableMessage();
        void makeSnackBar(String message, boolean infinity);
    }
}
