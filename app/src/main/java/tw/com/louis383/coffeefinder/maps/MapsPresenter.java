package tw.com.louis383.coffeefinder.maps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;

import java.util.HashMap;
import java.util.List;

import tw.com.louis383.coffeefinder.BasePresenter;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/1/13.
 */

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> implements GoogleMap.OnMarkerClickListener {

    private static final int CAMERA_MOVE_DELAY = 250;

    private Marker lastMarker;
    private CoffeeShopListManager coffeeShopListManager;

    private LatLng temporaryLatlang;
    private HashMap<String, Marker> markerMap;

    public MapsPresenter(CoffeeShopListManager coffeeShopListManager) {
        this.coffeeShopListManager = coffeeShopListManager;

        markerMap = new HashMap<>();
    }

    @Override
    public void attachView(MapView view) {
        super.attachView(view);
    }

    public void setGoogleMap(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(this);

        if (temporaryLatlang != null) {
            view.moveCamera(temporaryLatlang, null);
            temporaryLatlang = null;
        }
    }

    public void setTemporaryLatlang(LatLng latLng) {
        this.temporaryLatlang = latLng;
    }

    public void activeMarker(CoffeeShop coffeeShop) {
        if (!markerMap.isEmpty() && markerMap.get(coffeeShop.getId()) != null) {
            Marker marker = markerMap.get(coffeeShop.getId());
            moveCameraToMarker(marker);
        }
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

    private void highlightMarker(Marker marker, boolean highlight) {
        if (highlight) {
            BitmapDescriptor highlightMarker = getDrawableBitmapDescriptor(R.drawable.ic_map_pin_active);
            marker.setZIndex(1.0f);
            marker.setIcon(highlightMarker);
        } else {
            BitmapDescriptor normalMarker = getDrawableBitmapDescriptor(R.drawable.ic_map_pin);
            marker.setZIndex(0.0f);
            marker.setIcon(normalMarker);
        }
    }

    private void moveCameraToMarker(Marker marker) {
        if (lastMarker != null) {
            // Restore last marker's color and zIndex
            highlightMarker(lastMarker, false);
        }

        view.moveCamera(marker.getPosition(), null);
        highlightMarker(marker, true);
        this.lastMarker = marker;
    }

    //region GoogleMap OnMarkerClickListener
    @Override
    public boolean onMarkerClick(Marker marker) {
        moveCameraToMarker(marker);

        CoffeeShop coffeeShop = (CoffeeShop) marker.getTag();
        view.openDetailView(coffeeShop);
        return true;    // disable snippet
    }
    //endregion

    public void prepareToShowCoffeeShops(List<CoffeeShop> coffeeShops) {
        if (!coffeeShops.isEmpty()) {
            view.cleanMap();

            BitmapDescriptor normalMarker = getDrawableBitmapDescriptor(R.drawable.ic_map_pin);
            for (CoffeeShop coffeeShop : coffeeShops) {
                LatLng latLng = new LatLng(coffeeShop.getLatitude(), coffeeShop.getLongitude());

                String distence = String.valueOf(coffeeShop.getDistance());
                Marker generatedMarker = view.addMakers(latLng, coffeeShop.getName(), distence, coffeeShop, normalMarker);
                if (generatedMarker != null) {
                    markerMap.put(coffeeShop.getId(), generatedMarker);
                }
            }
        } else {
            view.showNoCoffeeShopDialog();
        }
    }

    public interface MapView {
        boolean checkLocationPermission();
        Drawable getResourceDrawable(int resId);
        Location getCurrentLocation();
        Marker addMakers(LatLng latLng, String title, String snippet, CoffeeShop coffeeShop, BitmapDescriptor icon);

        void moveCamera(LatLng latLng, Float zoom);
        void setupDetailedMapInterface();
        void showNoCoffeeShopDialog();
        void openWebsite(Uri uri);
        void cleanMap();
        void openDetailView(CoffeeShop coffeeShop);
    }
}
