package tw.com.louis383.coffeefinder.mainpage;

import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
import android.view.View;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
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

public class MainPresenter extends BasePresenter<MainPresenter.MainView> implements CoffeeShopListManager.Callback,
        LifecycleObserver {

    private static final String GOOGLE_MAP_PACKAGE = "com.google.android.apps.maps";

    private static final int UPDATE_INTERVAL = 10000;    // 10 Sec
    private static final int FASTEST_UPDATE_INTERVAL = 5000; // 5 Sec
    private static final int RANGE = 3000;    // 3m

    private LocationRequest locationRequest;
    private CoffeeShopListManager coffeeShopListManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HandlerThread backgroundThread;
    private Handler uiHandler;

    private Location currentLocation;
    private CoffeeShop lastTappedCoffeeShop;
    private boolean isRequestingLocation, isWaitingAccurateLocation;

    public MainPresenter(CoffeeShopListManager coffeeShopListManager, FusedLocationProviderClient locationProviderClient) {
        this.fusedLocationProviderClient = locationProviderClient;
        this.coffeeShopListManager = coffeeShopListManager;
        this.coffeeShopListManager.setCallback(this);
    }

    @Override
    public void attachView(MainView view) {
        super.attachView(view);
        view.setStatusBarDarkIndicator();
        view.showFab(false);
        view.setFloatingActionButtonEnable(false);

        if (!view.checkLocationPermission()) {
            view.requestLocationPermission();
        }

        if (!view.isInternetAvailable()) {
            view.requestInternetConnection();
        }
    }

    public void addLifecycleOwner(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Event.ON_RESUME)
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("BackgroundThread");
        backgroundThread.start();
        uiHandler = new Handler(Looper.getMainLooper());
        requestUserLocation(false);
    }

    @OnLifecycleEvent(Event.ON_PAUSE)
    private void pauseLocationUpdate() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            uiHandler = null;
        } catch (InterruptedException e) {
            Log.e("MainPresenter", Log.getStackTraceString(e));
        }

        stopLocationUpdate();
    }

    public void requestUserLocation(boolean force) {
        // Prevent request twice current location
        if (currentLocation != null && !force) {
            return;
        }

        if (view.checkLocationPermission()) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    LatLng lastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    view.moveCameraToCurrentPosition(lastLatLng);
                    fetchCoffeeShops();
                    Log.i("MainPresenter",
                            "lastLocation latitude: " + lastLatLng.latitude + ", longitude: " + lastLatLng.longitude);
                } else {
                    isWaitingAccurateLocation = true;
                }
                tryToGetAccurateLocation();
            }).addOnFailureListener(e -> {
                isWaitingAccurateLocation = true;
                tryToGetAccurateLocation();
            });
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
                        view.showFab(false);
                        view.setFloatingActionButtonEnable(false);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        view.showFab(true);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        view.showFab(false);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                view.setShadowAlpha(slideOffset);
            }
        });
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void tryToGetAccurateLocation() {
        if (locationRequest == null) {
            buildLocationRequest();
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(view.getActivityContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                task.getResult(ApiException.class);
                if (!isRequestingLocation) {
                    startLocationUpdate();
                }
                Log.i("MapsPresenter", "Succeed.");
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                        view.locationSettingNeedsResolution(resolvable);
                        Log.i("MapsPresenter", "Resolution Required");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        view.showServiceUnavailableMessage();
                        Log.i("MapsPresenter", "unavailable.");
                        break;
                }
            }
        });
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdate() {
        isRequestingLocation = true;
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, backgroundThread.getLooper());
    }

    private void stopLocationUpdate() {
        isRequestingLocation = false;
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(final LocationResult result) {
            super.onLocationResult(result);
            List<Location> locations = result.getLocations();
            if (!locations.isEmpty()) {
                // The last location in the list is the newest
                Location newestLocation = locations.get(locations.size() - 1);
                stopLocationUpdate();    // Only get one time accurate position.
                MainPresenter.this.currentLocation = newestLocation;

                if (isWaitingAccurateLocation) {
                    isWaitingAccurateLocation = false;

                    uiHandler.post(() -> {
                        LatLng currentLatLng = new LatLng(newestLocation.getLatitude(), newestLocation.getLongitude());
                        view.moveCameraToCurrentPosition(currentLatLng);
                        fetchCoffeeShops();
                    });
                }
            }
        }
    };

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
        boolean isInternetAvailable();
        void requestInternetConnection();
        void requestLocationPermission();
        void locationSettingNeedsResolution(ResolvableApiException resolvable);
        void showServiceUnavailableMessage();
        void makeSnackBar(String message, boolean infinity);
        void setStatusBarDarkIndicator();
        void moveCameraToCurrentPosition(LatLng latLng);
        void onCoffeeShopFetched(List<CoffeeShop> coffeeShops);
        void navigateToLocation(Intent intent);
        void showNeedsGoogleMapMessage();
        void showBottomSheetDetailView(CoffeeShopViewModel viewModel);
        void shareCoffeeShop(Intent shareIntent);
        void showFab(boolean show);
        void setShadowAlpha(float offset);
        void setFloatingActionButtonEnable(boolean enable);
        Context getActivityContext();
    }
}
