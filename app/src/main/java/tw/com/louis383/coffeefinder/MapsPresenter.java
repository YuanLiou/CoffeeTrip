package tw.com.louis383.coffeefinder;

/**
 * Created by louis383 on 2017/1/13.
 */

public class MapsPresenter extends BasePresenter<MapsPresenter.MapView> {

    @Override
    public void attachView(MapView view) {
        super.attachView(view);

        if (!view.isLocationPermissionGranted()) {
            view.requestLocationPermission();
        }
    }

    public interface MapView {
        boolean isLocationPermissionGranted();
        void requestLocationPermission();
    }
}
