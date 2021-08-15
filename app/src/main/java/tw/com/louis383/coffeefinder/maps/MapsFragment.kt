package tw.com.louis383.coffeefinder.maps

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.OnMapReadyCallback
import com.google.android.libraries.maps.model.BitmapDescriptor
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import tw.com.louis383.coffeefinder.BaseFragment
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager
import tw.com.louis383.coffeefinder.model.entity.Shop
import tw.com.louis383.coffeefinder.utils.ifNotNull
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment : BaseFragment(), OnMapReadyCallback, MapsView, GoogleMap.OnMapClickListener {
    companion object {
        const val TAG = "MAP_FRAGMENT"
        const val ZOOM_RATE = 16f

        fun newInstance(): MapsFragment = MapsFragment().apply {
            arguments = Bundle()
        }
    }

    private var presenter: MapsPresenter? = null
    private var googleMap: GoogleMap? = null

    private var mapView: MapView? = null
    private var handler: MapsClickHandler? = null

    private val isMapReady: Boolean
        get() = googleMap != null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view)
        mapView?.run {
            onCreate(savedInstanceState)
            onResume()
            getMapAsync(this@MapsFragment)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        presenter?.attachView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler = null
    }

    override fun provideLifecycleOwner(): LifecycleOwner {
        return viewLifecycleOwner
    }

    @Inject
    fun initPresenter(coffeeShopListManager: CoffeeShopListManager) {
        presenter = MapsPresenter(coffeeShopListManager)
    }

    fun setMapClickHandler(handler: MapsClickHandler) {
        this.handler = handler
    }

    fun setMarkerActive(coffeeShop: Shop) {
        presenter?.activeMarker(coffeeShop)
    }

    override fun prepareCoffeeShops(coffeeShops: List<Shop>) {
        presenter?.prepareToShowCoffeeShops(coffeeShops)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap?.setOnMapClickListener(this)
        // Center Taiwan : 23.973875°N 120.982024°E
        val defaultLocation = CameraUpdateFactory.newLatLngZoom(LatLng(23.973875, 120.982024), ZOOM_RATE)
        this.googleMap?.moveCamera(defaultLocation)

        presenter?.setGoogleMap(googleMap)
        setupDetailedMapInterface()
    }

    override fun checkLocationPermission(): Boolean {
        return activity?.run {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }

    private fun enableMyLocation() {
        googleMap?.run {
            if (checkLocationPermission() && isMapReady && !isMyLocationEnabled) {
                isMyLocationEnabled = true
            }
        }
    }

    override fun addMakers(latLng: LatLng, title: String, snippet: String, coffeeShop: Shop, icon: BitmapDescriptor): Marker? {
        if (!isMapReady) {
            return null
        }

        val distance = resources.getString(R.string.unit_m, snippet)
        val options = MarkerOptions().also {
            it.position(latLng)
            it.title(title)
            it.snippet(distance)
            it.icon(icon)
        }

        val marker = googleMap?.addMarker(options)
        marker?.tag = coffeeShop

        return marker
    }

    override fun moveCamera(latLng: LatLng, zoom: Float?) {
        if (!isMapReady) {
            // FIXME:: it's a dirty hack to prevent get google map on an asynchronous way and get null if not ready.
            presenter?.setTemporaryLatlang(latLng)
            return
        }
        googleMap?.takeUnless { it.isMyLocationEnabled }?.run { enableMyLocation() }

        val cameraUpdate = zoom?.let {
            CameraUpdateFactory.newLatLngZoom(latLng, it)
        } ?: CameraUpdateFactory.newLatLng(latLng)
        googleMap?.animateCamera(cameraUpdate)
    }

    override fun openDetailView(coffeeShop: Shop) {
        ifNotNull(handler, coffeeShop) { first, second ->
            first.onMarkerClicked(second)
        }
    }

    override fun setupDetailedMapInterface() {
        if (isMapReady) {
            enableMyLocation()
            googleMap?.isBuildingsEnabled = true

            val mapUISettings = googleMap?.uiSettings
            mapUISettings?.run {
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isMapToolbarEnabled = false
                isMyLocationButtonEnabled = false
            }
        }
    }

    override fun showNoCoffeeShopDialog() {
        context?.run {
            AlertDialog.Builder(this).also {
                it.setTitle(resources.getString(R.string.dialog_no_coffeeshop_title))
                it.setMessage(resources.getString(R.string.dialog_no_coffeeshop_message))
                it.setPositiveButton(resources.getString(R.string.dialog_no_coffeeshop_ok)) { _, _ -> }
                it.create()
            }.show()
        }
    }

    override fun cleanMap() {
        if (isMapReady) {
            googleMap?.clear()
        }
    }

    override fun getResourceDrawable(resId: Int): Drawable? {
        return context?.run {
            ContextCompat.getDrawable(this, resId)
        }
    }

    fun moveToMyLocation(currentLocation: Location?) {
        currentLocation?.run {
            val lastLatlng = LatLng(latitude, longitude)
            moveCamera(lastLatlng, null)
        }
    }

    override fun onMapClick(latLng: LatLng) {
        handler?.onMapClicked()
    }
}
