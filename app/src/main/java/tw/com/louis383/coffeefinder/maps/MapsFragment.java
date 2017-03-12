package tw.com.louis383.coffeefinder.maps;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import javax.inject.Inject;

import tw.com.louis383.coffeefinder.BaseFragment;
import tw.com.louis383.coffeefinder.CoffeeTripApplication;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.mainpage.MainActivity;
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;
import tw.com.louis383.coffeefinder.utils.ChromeCustomTabsHelper;

public class MapsFragment extends BaseFragment implements OnMapReadyCallback, MapsPresenter.MapView, View.OnClickListener, GoogleMap.OnMapClickListener {

    public static final float ZOOM_RATE = 16f;

    private GoogleMap googleMap;
    private MapView mapView;
    private MapsPresenter presenter;

    private FrameLayout rootView;
    private Snackbar snackbar;
    private FloatingActionButton myLocationButton;

    private MapsClickHandler handler;

    public MapsFragment() {}

    public static MapsFragment newInstance() {

        Bundle args = new Bundle();

        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = (FrameLayout) view.findViewById(R.id.map_rootview);
        mapView = (MapView) view.findViewById(R.id.map_view);
        myLocationButton = (FloatingActionButton) view.findViewById(R.id.my_location_button);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ((CoffeeTripApplication) getActivity().getApplication()).getAppComponent().inject(this);

        presenter.attachView(this);
        myLocationButton.setOnClickListener(this);
    }

    @Inject
    public void initPresenter(CoffeeShopListManager coffeeShopListManager) {
        presenter = new MapsPresenter(coffeeShopListManager);
    }

    public void setMapClickHandler(MapsClickHandler handler) {
        this.handler = handler;
    }

    public void setMarkerActive(CoffeeShop coffeeShop) {
        presenter.activeMarker(coffeeShop);
    }

    @Override
    public void prepareCoffeeShops(List<CoffeeShop> coffeeShops) {
        presenter.prepareToShowCoffeeShops(coffeeShops);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMapClickListener(this);

        presenter.setGoogleMap(googleMap);
        setupDetailedMapInterface();
    }

    @Override
    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void enableMyLocation() {
        if (checkLocationPermission() && isMapReady() && !googleMap.isMyLocationEnabled()) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public Marker addMakers(LatLng latLng, String title, String snippet, CoffeeShop coffeeShop, BitmapDescriptor icon) {
        if (!isMapReady()) {
            return null;
        }

        String distance = getResources().getString(R.string.unit_m, snippet);
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.title(title);
        options.snippet(distance);
        options.icon(icon);

        Marker marker = googleMap.addMarker(options);
        marker.setTag(coffeeShop);
        return marker;
    }

    @Override
    public void moveCamera(LatLng latLng, Float zoom) {
        if (!isMapReady()) {
            // FIXME:: it's a dirty hack to prevent get google map on an asynchonous way and get null if not ready.
            presenter.setTemporaryLatlang(latLng);
            return;
        }

        if (!googleMap.isMyLocationEnabled()) {
            enableMyLocation();
        }

        float currentZoomLevel = googleMap.getCameraPosition().zoom;
        if (zoom == null && currentZoomLevel < 15f) {
            zoom = ZOOM_RATE;
        }

        CameraUpdate cameraUpdate;
        if (zoom != null) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        }
        googleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void openDetailView(CoffeeShop coffeeShop) {
        if (handler != null && coffeeShop != null) {
            handler.onMarkerClicked(coffeeShop);
        }
    }

    @Override
    public void openWebsite(Uri uri) {
        CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabIntent = customTabBuilder.build();

        ChromeCustomTabsHelper.openCustomTab(getActivity(), customTabIntent, uri, (activity, uri1) -> {
            // TODO:: a webview page to open a link.
            Intent intent = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(intent);
        });
    }

    @Override
    public void setupDetailedMapInterface() {
        if (isMapReady()) {
            enableMyLocation();
            googleMap.setBuildingsEnabled(true);

            UiSettings mapUISettings = googleMap.getUiSettings();
            mapUISettings.setRotateGesturesEnabled(false);
            mapUISettings.setTiltGesturesEnabled(false);
            mapUISettings.setMapToolbarEnabled(false);
            mapUISettings.setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public void showNoCoffeeShopDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(getResources().getString(R.string.dialog_no_coffeeshop_title));
        alertDialogBuilder.setMessage(getResources().getString(R.string.dialog_no_coffeeshop_message));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.dialog_no_coffeeshop_ok), (dialog, which) -> {});
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    @Override
    public void cleanMap() {
        if (isMapReady()) {
            googleMap.clear();
        }
    }

    @Override
    public Location getCurrentLocation() {
        return ((MainActivity) getActivity()).getCurrentLocation();
    }

    @Override
    public Drawable getResourceDrawable(int resId) {
        return ContextCompat.getDrawable(getContext(), resId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_location_button:
                Location currentLocation = getCurrentLocation();
                if (currentLocation != null) {
                    LatLng lastLatlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    moveCamera(lastLatlng, null);
                }
                break;
        }
    }

    private boolean isMapReady() {
        return googleMap != null;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        handler.onMapClicked();
    }
}
