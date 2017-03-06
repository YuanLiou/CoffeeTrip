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

    public MapsPresenter(CoffeeShopListManager coffeeShopListManager) {
        this.coffeeShopListManager = coffeeShopListManager;
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

    //region GoogleMap OnMarkerClickListener
    @Override
    public boolean onMarkerClick(Marker marker) {
        CoffeeShop coffeeShop = (CoffeeShop) marker.getTag();
        view.moveCamera(marker.getPosition(), null);
        view.openDetailView(coffeeShop);

        this.lastMarker = marker;
        return true;    // disable snippet
    }
    //endregion

    public void prepareToShowCoffeeShops(List<CoffeeShop> coffeeShops) {
        if (!coffeeShops.isEmpty()) {
            view.cleanMap();

            BitmapDescriptor normalMarker = getDrawableBitmapDescriptor(R.drawable.ic_pin);
            for (CoffeeShop coffeeShop : coffeeShops) {
                LatLng latLng = new LatLng(coffeeShop.getLatitude(), coffeeShop.getLongitude());

                String distence = String.valueOf(coffeeShop.getDistance());
                view.addMakers(latLng, coffeeShop.getName(), distence, coffeeShop, normalMarker);
            }
        } else {
            view.showNoCoffeeShopDialog();
        }
    }

    public interface MapView {
        boolean checkLocationPermission();
        Drawable getResourceDrawable(int resId);
        Location getCurrentLocation();
        void addMakers(LatLng latLng, String title, String snippet, CoffeeShop coffeeShop, BitmapDescriptor icon);
        void moveCamera(LatLng latLng, Float zoom);
        void setupDetailedMapInterface();
        void showNoCoffeeShopDialog();
        void openWebsite(Uri uri);
        void cleanMap();
        void openDetailView(CoffeeShop coffeeShop);
    }
}
