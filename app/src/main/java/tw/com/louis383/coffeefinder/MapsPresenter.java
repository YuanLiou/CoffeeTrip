package tw.com.louis383.coffeefinder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.util.Log;

/**
 * Created by louis383 on 2017/1/13.
 */

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> {

    private static final float ZOOM_RATE = 15f;

    private GoogleApiClient googleApiClient;

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

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public interface MapView {
        boolean isLocationPermissionGranted();
        void requestLocationPermission();
        void addMakers(LatLng latLng, String title);
        void moveCamera(LatLng latLng, float zoom);
        void setupDetailedMapInterface();
    }
}
