package tw.com.louis383.coffeefinder;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.FrameLayout;

import tw.com.louis383.coffeefinder.utils.ChromeCustomTabsHelper;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;
import tw.com.louis383.coffeefinder.widget.CoffeeDetailDialog;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapsPresenter.MapView, CoffeeDetailDialog.Callback, View.OnClickListener {

    private GoogleMap googleMap;
    private MapsPresenter presenter;

    private FrameLayout rootView;
    private Snackbar snackbar;
    private CoffeeDetailDialog detailDialog;
    private FloatingActionButton myLocationButton;

    private boolean mapInterfaceInitiated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        rootView = (FrameLayout) findViewById(R.id.map_rootview);
        myLocationButton = (FloatingActionButton) findViewById(R.id.my_location_button);

        presenter = new MapsPresenter();
        presenter.attachView(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        myLocationButton.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        presenter.setGoogleMap(googleMap);
    }

    @Override
    public void addMakers(LatLng latLng, String title, String snippet, String id, BitmapDescriptor icon) {
        String distance = getResources().getString(R.string.unit_m, snippet);
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.title(title);
        options.snippet(distance);
        options.icon(icon);

        Marker marker = googleMap.addMarker(options);
        marker.setTag(id);
    }

    @Override
    public void moveCamera(LatLng latLng, Float zoom) {
        if (!mapInterfaceInitiated) {
            mapInterfaceInitiated = true;
            setupDetailedMapInterface();
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
    public void openWebsite(Uri uri) {
        CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabIntent = customTabBuilder.build();

        ChromeCustomTabsHelper.openCustomTab(this, customTabIntent, uri, (activity, uri1) -> {
            // TODO:: a webview page to open a link.
            Intent intent = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(intent);
        });
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void setupDetailedMapInterface() {
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);

        UiSettings mapUISettings = googleMap.getUiSettings();
        mapUISettings.setRotateGesturesEnabled(false);
        mapUISettings.setTiltGesturesEnabled(false);
        mapUISettings.setMapToolbarEnabled(false);
        mapUISettings.setMyLocationButtonEnabled(false);
    }

    @Override
    public void showNeedsGoogleMapMessage() {
        String message = getResources().getString(R.string.googlemap_not_install);
        makeCustomSnackbar(message, false);
    }

    @Override
    public void makeCustomSnackbar(String message, boolean infinity) {
        int duration = infinity ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG;
        snackbar = Snackbar.make(rootView, message, duration);
        snackbar.show();
    }

    @Override
    public void showNoCoffeeShopDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.dialog_no_coffeeshop_title));
        alertDialogBuilder.setMessage(getResources().getString(R.string.dialog_no_coffeeshop_message));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.dialog_no_coffeeshop_ok), (dialog, which) -> {});
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    @Override
    public void openCoffeeDetailDialog(CoffeeShopViewModel viewModel) {
        if (detailDialog == null) {
            detailDialog = new CoffeeDetailDialog(this, viewModel, this);
        } else if (detailDialog.isShowing()) {
            detailDialog.dismiss();
        } else {
            detailDialog.setupCoffeeShop(viewModel);
        }

        detailDialog.show();
    }

    @Override
    public void cleanMap() {
        if (googleMap != null) {
            googleMap.clear();
        }
    }

    @Override
    public void navigateToLocation(Intent intent) {
        startActivity(intent);
    }

    @Override
    public boolean isGoogleMapInstalled(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public Drawable getResourceDrawable(int resId) {
        return ContextCompat.getDrawable(this, resId);
    }

    //region CoffeeDetailDialog Callback
    @Override
    public void onNavigationTextClicked(CoffeeShopViewModel viewModel) {
        presenter.prepareNavigation();
    }

    @Override
    public void onOpenWebsiteButtonClicked(CoffeeShopViewModel viewModel) {
        openWebsite(viewModel.getDetailUri());
    }
    //endregion

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_location_button:
                presenter.moveCameraToMyLocation();
                break;
        }
    }
}
