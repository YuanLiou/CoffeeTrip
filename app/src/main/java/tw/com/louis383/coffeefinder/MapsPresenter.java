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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.com.louis383.coffeefinder.model.CoffeeTripAPI;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;

/**
 * Created by louis383 on 2017/1/13.
 */

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> implements LocationListener, GoogleMap.OnMarkerClickListener {

    private static final float ZOOM_RATE = 16f;
    private static final int UPDATE_INTERVAL = 10000;    // 10 Sec
    private static final int FASTEST_UPDATE_INTERVAL = 5000; // 5 Sec

    private static final int RANGE = 2000;    // 2m
    private static final int CAMERA_MOVE_DELAY = 250;

    public static final String GOOGLE_MAP_PACKAGE = "com.google.android.apps.maps";

    private GoogleApiClient googleApiClient;
    private CoffeeTripAPI coffeeTripAPI;
    private LocationRequest locationRequest;

    private Location currentLocation;
    private boolean isRequestingLocation;
    private List<CoffeeShop> coffeeShops;

    private Marker lastMarker;

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

    public void requestUserLocation(boolean force) {
        // Prevent request twice current location
        if (currentLocation != null && !force) {
            return;
        }

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
//        if (googleApiClient.isConnected() && isRequestingLocation) {
//            requestUserLocation();
//        }
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
                            coffeeShops = new ArrayList<>();
                            coffeeShops.addAll(listResponse.body());

                            if (!coffeeShops.isEmpty()) {
                                view.cleanMap();

                                int index = 0;
                                BitmapDescriptor normalMarker = getDrawableBitmapDescriptor(R.drawable.ic_pin);
                                for (CoffeeShop shop : coffeeShops) {
                                    LatLng latLng = new LatLng(shop.getLatitude(), shop.getLongitude());

                                    int distance = (int) shop.getDistance();
                                    view.addMakers(latLng, shop.getName(), String.valueOf(distance), String.valueOf(index), normalMarker);

                                    index++;
                                }
                            } else {
                                view.showNoCoffeeShopDialog();
                            }
                        }
                    }, throwable -> {
                        view.makeCustomSnackbar(throwable.getLocalizedMessage(), true);
                        Log.e("fetchingCoffeeShop", Log.getStackTraceString(throwable));
                    });
        }
    }

    public void setGoogleMap(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(this);
    }

    public void prepareNavigation() {
        if (!view.isGoogleMapInstalled(GOOGLE_MAP_PACKAGE)) {
            view.showNeedsGoogleMapMessage();
            return;
        }

        String urlString = String.format(Locale.getDefault(), "http://maps.google.com/maps?daddr=%f,%f&saddr=%f,%f",
                lastMarker.getPosition().latitude, lastMarker.getPosition().longitude,
                currentLocation.getLatitude(), currentLocation.getLongitude());

        Intent intent = new Intent();
        intent.setClassName(GOOGLE_MAP_PACKAGE, "com.google.android.maps.MapsActivity");
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlString));

        view.navigateToLocation(intent);
    }

    private BitmapDescriptor getDrawableBitmapDescriptor(int resId) {
        Drawable drawable = view.getResourceDrawable(resId);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void highlightMarker(Marker marker, boolean isHighlight) {
        if (isHighlight) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            marker.setZIndex(1.0f);
        } else {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker());
            marker.setZIndex(0.0f);
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

    //region GoogleMap OnMarkerClickListener
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (lastMarker != null) {
            // Restore last marker's color and zIndex
            highlightMarker(lastMarker, false);
        }

        int index = Integer.parseInt((String) marker.getTag());
        final CoffeeShopViewModel viewModel = coffeeShops.get(index).getViewModel();
        view.moveCamera(marker.getPosition(), null);
        Handler handler = new Handler();
        handler.postDelayed(() -> view.openCoffeeDetailDialog(viewModel), CAMERA_MOVE_DELAY);
        highlightMarker(marker, true);

        this.lastMarker = marker;
        return true;    // disable snippet
    }
    //endregion

    public interface MapView {
        boolean isLocationPermissionGranted();
        boolean isGoogleMapInstalled(String packageName);
        Drawable getResourceDrawable(int resId);
        void requestLocationPermission();
        void addMakers(LatLng latLng, String title, String snippet, String id, BitmapDescriptor icon);
        void moveCamera(LatLng latLng, Float zoom);
        void setupDetailedMapInterface();
        void locationSettingNeedsResolution(Status status);
        void showNeedsGoogleMapMessage();
        void showServiceUnavailableMessage();
        void makeCustomSnackbar(String message, boolean infinity);
        void showNoCoffeeShopDialog();
        void openWebsite(Uri uri);
        void openCoffeeDetailDialog(CoffeeShopViewModel viewModel);
        void cleanMap();
        void navigateToLocation(Intent intent);
    }
}
