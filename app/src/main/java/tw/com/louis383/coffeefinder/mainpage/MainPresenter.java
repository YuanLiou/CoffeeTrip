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

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.Locale;

import tw.com.louis383.coffeefinder.BasePresenter;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;

/**
 * Created by louis383 on 2017/2/17.
 */

public class MainPresenter extends BasePresenter<MainPresenter.MainView> implements LocationListener, CoffeeShopListManager.Callback {

    private static final String GOOGLE_MAP_PACKAGE = "com.google.android.apps.maps";

    private static final int UPDATE_INTERVAL = 10000;    // 10 Sec
    private static final int FASTEST_UPDATE_INTERVAL = 5000; // 5 Sec
    private static final int RANGE = 3000;    // 3m

    private GoogleApiClient apiClient;
    private LocationRequest locationRequest;
    private CoffeeShopListManager coffeeShopListManager;

    private Location currentLocation;
    private CoffeeShop lastTappedCoffeeShop;
    private boolean isRequestingLocation, isWaitingAccurateLocation;

    public MainPresenter(GoogleApiClient apiClient, CoffeeShopListManager coffeeShopListManager) {
        this.apiClient = apiClient;
        this.coffeeShopListManager = coffeeShopListManager;
        this.coffeeShopListManager.setCallback(this);
    }

    @Override
    public void attachView(MainView view) {
        super.attachView(view);
        view.setStatusBarDarkIndicator();

        if (!view.checkLocationPermission()) {
            view.requestLocationPermission();
        }
    }

    public void requestUserLocation(boolean force) {
        // Prevent request twice current location
        if (currentLocation != null && !force) {
            return;
        }

        if (view.checkLocationPermission()) {
            currentLocation = getLastLocation();
            if (currentLocation != null) {
                LatLng lastLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                view.moveCameraToCurrentPosition(lastLatLng);
                fetchCoffeeShops();
                Log.i("MainPresenter", "lastLocation latitude: " + lastLatLng.latitude + ", longitude: " + lastLatLng.longitude);
            } else {
                isWaitingAccurateLocation = true;
            }

            tryToGetAccurateLocation();
        }
    }

    public void onActivityPause() {
        if (apiClient.isConnected()) {
            stopLocationUpdate();
        }
    }

    public void fetchCoffeeShops() {
        coffeeShopListManager.fetch(currentLocation, RANGE);
    }

    public void prepareNavigation() {
        if (!view.isApplicationInstalled(GOOGLE_MAP_PACKAGE)) {
            view.showNeedsGoogleMapMessage();
            return;
        }

        if (currentLocation != null && lastTappedCoffeeShop != null) {
            String urlString = String.format(Locale.getDefault(), "http://maps.google.com/maps?daddr=%f,%f&saddr=%f,%f&mode=w",
                    lastTappedCoffeeShop.getLatitude(), lastTappedCoffeeShop.getLongitude(),
                    currentLocation.getLatitude(), currentLocation.getLongitude());

            Intent intent = new Intent();
            intent.setPackage(GOOGLE_MAP_PACKAGE);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(urlString));
            view.navigateToLocation(intent);
        }
    }

    public void share(Context context) {
        if (lastTappedCoffeeShop != null) {
            CoffeeShopViewModel coffeeShopViewModel = lastTappedCoffeeShop.getViewModel();
            String subject = context.getResources().getString(R.string.share_subject);
            String message = context.getResources().getString(R.string.share_message, coffeeShopViewModel.getShopName(), coffeeShopViewModel.getAddress());

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message + "\n" + coffeeShopViewModel.getDetailUri());

            view.shareCoffeeShop(intent);
        }
    }

    public void setLastTappedCoffeeShop(CoffeeShop lastTappedCoffeeShop) {
        this.lastTappedCoffeeShop = lastTappedCoffeeShop;
    }

    public void showDetailView() {
        if (lastTappedCoffeeShop != null) {
            view.showBottomSheetDetailView(lastTappedCoffeeShop.getViewModel());
        }
    }

    public void setBottomSheetBehavior(BottomSheetBehavior bottomSheetBehavior) {
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        if (!view.isAppbarVisible()) {
                            view.hideAppbar(false);
                        }
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0.1f && view.isAppbarVisible()) {
                    view.hideAppbar(true);
                } else if (slideOffset <= 0.4f && !view.isAppbarVisible()) {
                    view.hideAppbar(false);
                }
//                Log.i("MainPresenter", "onSlide: " + slideOffset);
            }
        });
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

    public Location getCurrentLocation() {
        return currentLocation;
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
        stopLocationUpdate();    // Only get one time accurate position.

        if (isWaitingAccurateLocation) {
            isWaitingAccurateLocation = false;

            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            view.moveCameraToCurrentPosition(currentLatLng);
            fetchCoffeeShops();
        }
    }
    //endregion

    //region CoffeeShopListManager Callback
    @Override
    public void onCoffeeShopFetchedComplete(List<CoffeeShop> coffeeShops) {
        view.onCoffeeShopFetched(coffeeShops);
    }

    @Override
    public void onCoffeeShopFetchedFailed(String message) {
        view.makeSnackBar(message, false);
    }
    //endregion

    public interface MainView {
        boolean isApplicationInstalled(String packageName);
        boolean checkLocationPermission();
        boolean isAppbarVisible();
        void requestLocationPermission();
        void locationSettingNeedsResolution(Status status);
        void showServiceUnavailableMessage();
        void makeSnackBar(String message, boolean infinity);
        void setStatusBarDarkIndicator();
        void moveCameraToCurrentPosition(LatLng latLng);
        void onCoffeeShopFetched(List<CoffeeShop> coffeeShops);
        void navigateToLocation(Intent intent);
        void showNeedsGoogleMapMessage();
        void showBottomSheetDetailView(CoffeeShopViewModel viewModel);
        void shareCoffeeShop(Intent shareIntent);
        void hideAppbar(boolean hide);
    }
}
