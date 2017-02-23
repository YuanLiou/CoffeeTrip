package tw.com.louis383.coffeefinder.maps;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.com.louis383.coffeefinder.BasePresenter;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;

/**
 * Created by louis383 on 2017/1/13.
 */

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> implements GoogleMap.OnMarkerClickListener, CoffeeShopListManager.Callback {

    public static final String GOOGLE_MAP_PACKAGE = "com.google.android.apps.maps";
    private static final int CAMERA_MOVE_DELAY = 250;
    private static final int RANGE = 5000;

    private Marker lastMarker;
    private CoffeeShopListManager coffeeShopListManager;

    private List<CoffeeShop> coffeeShops;

    public MapsPresenter(CoffeeShopListManager coffeeShopListManager) {
        this.coffeeShopListManager = coffeeShopListManager;
        this.coffeeShopListManager.setCallback(this);

        coffeeShops = new ArrayList<>();
    }

    @Override
    public void attachView(MapView view) {
        super.attachView(view);
    }

    public void setGoogleMap(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(this);
    }

    public void prepareNavigation() {
        if (!view.isGoogleMapInstalled(GOOGLE_MAP_PACKAGE)) {
            view.showNeedsGoogleMapMessage();
            return;
        }

        Location currentLocation = view.getCurrentLocation();
        if (currentLocation != null) {
            String urlString = String.format(Locale.getDefault(), "http://maps.google.com/maps?daddr=%f,%f&saddr=%f,%f",
                    lastMarker.getPosition().latitude, lastMarker.getPosition().longitude,
                    currentLocation.getLatitude(), currentLocation.getLongitude());

            Intent intent = new Intent();
            intent.setClassName(GOOGLE_MAP_PACKAGE, "com.google.android.maps.MapsFragment");
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(urlString));

            view.navigateToLocation(intent);
        }
    }

    public void fetchCoffeeShops() {
        Location currentLocation = view.getCurrentLocation();
        if (currentLocation != null) {
            coffeeShopListManager.fetch(currentLocation, RANGE);
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

    //region GoogleMap OnMarkerClickListener
    @Override
    public boolean onMarkerClick(Marker marker) {
//        int index = Integer.parseInt((String) marker.getTag());
//        final CoffeeShopViewModel viewModel = coffeeShops.get(index).getViewModel();
//        view.moveCamera(marker.getPosition(), null);
//        Handler handler = new Handler();
//        handler.postDelayed(() -> view.openCoffeeDetailDialog(viewModel), CAMERA_MOVE_DELAY);

        this.lastMarker = marker;
        return true;    // disable snippet
    }
    //endregion

    //region CoffeeShopListManager.Callback
    @Override
    public void onCoffeeShopFetchedComplete(List<CoffeeShop> coffeeShops) {
        coffeeShops.addAll(coffeeShops);

        if (!coffeeShops.isEmpty()) {
            view.cleanMap();

            int index = 0;
            BitmapDescriptor normalMarker = getDrawableBitmapDescriptor(R.drawable.ic_pin);
            for (CoffeeShop coffeeShop : coffeeShops) {
                LatLng latLng = new LatLng(coffeeShop.getLatitude(), coffeeShop.getLongitude());

                String distence = String.valueOf(coffeeShop.getDistance());
                view.addMakers(latLng, coffeeShop.getName(), distence, String.valueOf(index), normalMarker);

                index++;
            }
        } else {
            view.showNeedsGoogleMapMessage();
        }
    }

    @Override
    public void onCoffeeShopFetchedFailed(String message) {
        view.makeCustomSnackbar(message, false);
    }
    //endregion

    public interface MapView {
        boolean isGoogleMapInstalled(String packageName);
        Drawable getResourceDrawable(int resId);
        Location getCurrentLocation();
        void addMakers(LatLng latLng, String title, String snippet, String id, BitmapDescriptor icon);
        void moveCamera(LatLng latLng, Float zoom);
        void setupDetailedMapInterface();
        void showNeedsGoogleMapMessage();
        void makeCustomSnackbar(String message, boolean infinity);
        void showNoCoffeeShopDialog();
        void openWebsite(Uri uri);
        void openCoffeeDetailDialog(CoffeeShopViewModel viewModel);
        void cleanMap();
        void navigateToLocation(Intent intent);
    }
}
