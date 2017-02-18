package tw.com.louis383.coffeefinder;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;

/**
 * Created by louis383 on 2017/1/13.
 */

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> implements GoogleMap.OnMarkerClickListener {

    public static final String GOOGLE_MAP_PACKAGE = "com.google.android.apps.maps";
    private static final int CAMERA_MOVE_DELAY = 250;

    private Marker lastMarker;

    @Override
    public void attachView(MapView view) {
        super.attachView(view);
    }


    public void moveCameraToMyLocation() {
        //TODO :: Request MainPresnter to get current location.
//        Location lastLocation = getLastLocation();
//        if (lastLocation != null) {
//            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//            view.moveCamera(latLng, null);
//        }
    }

    public void setGoogleMap(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(this);
    }

    public void prepareNavigation() {
        if (!view.isGoogleMapInstalled(GOOGLE_MAP_PACKAGE)) {
            view.showNeedsGoogleMapMessage();
            return;
        }

        // TODO:: request MainPresenter current location

//        String urlString = String.format(Locale.getDefault(), "http://maps.google.com/maps?daddr=%f,%f&saddr=%f,%f",
//                lastMarker.getPosition().latitude, lastMarker.getPosition().longitude,
//                currentLocation.getLatitude(), currentLocation.getLongitude());

//        Intent intent = new Intent();
//        intent.setClassName(GOOGLE_MAP_PACKAGE, "com.google.android.maps.MapsActivity");
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse(urlString));

//        view.navigateToLocation(intent);
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

    public interface MapView {
        boolean isGoogleMapInstalled(String packageName);
        Drawable getResourceDrawable(int resId);
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
