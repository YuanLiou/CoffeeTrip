package tw.com.louis383.coffeefinder.maps

import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.BitmapDescriptor
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import dagger.hilt.android.scopes.FragmentScoped
import tw.com.louis383.coffeefinder.BasePresenter
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.model.data.entity.Shop
import javax.inject.Inject

/**
 * Created by louis383 on 2017/1/13.
 */

@FragmentScoped
class MapsPresenter @Inject constructor() : BasePresenter<MapsView>(), GoogleMap.OnMarkerClickListener {

    private var lastMarker: Marker? = null

    private var temporaryLatlang: LatLng? = null
    private val markerMap = mutableMapOf<String, Marker>()

    fun setGoogleMap(googleMap: GoogleMap) {
        googleMap.setOnMarkerClickListener(this)

        temporaryLatlang = temporaryLatlang?.run {
            view?.moveCamera(this, null)
            null
        }
    }

    fun setTemporaryLatlang(latLng: LatLng) {
        this.temporaryLatlang = latLng
    }

    fun activeMarker(coffeeShop: Shop) {
        if (markerMap.isNotEmpty()) {
            val marker = markerMap[coffeeShop.id]
            marker?.run {
                moveCameraToMarker(this)
            }
        }
    }

    private fun getDrawableBitmapDescriptor(resId: Int): BitmapDescriptor? {
        val drawable = view?.getResourceDrawable(resId)
        drawable?.run {
            val width = intrinsicWidth
            val height = intrinsicHeight
            setBounds(0, 0, width, height)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            draw(canvas)

            return BitmapDescriptorFactory.fromBitmap(bitmap)
        } ?: return null
    }

    private fun highlightMarker(marker: Marker, highlight: Boolean) {
        if (highlight) {
            val highlightMarker = getDrawableBitmapDescriptor(R.drawable.ic_map_pin_active)
            marker.zIndex = 1.0f
            marker.setIcon(highlightMarker)
        } else {
            val normalMarker = getDrawableBitmapDescriptor(R.drawable.ic_map_pin)
            marker.zIndex = 0.0f
            marker.setIcon(normalMarker)
        }
    }

    private fun moveCameraToMarker(marker: Marker) {
        lastMarker?.run {
            // Restore last marker's color and zIndex
            highlightMarker(this, false)
        }

        view?.moveCamera(marker.position, null)
        highlightMarker(marker, true)
        this.lastMarker = marker
    }

    //region GoogleMap OnMarkerClickListener
    override fun onMarkerClick(marker: Marker): Boolean {
        moveCameraToMarker(marker)

        val coffeeShop = marker.tag as? Shop
        coffeeShop?.run {
            view?.openDetailView(this)
        }
        return true    // disable snippet
    }
    //endregion

    fun prepareToShowCoffeeShops(coffeeShops: List<Shop>) {
        if (coffeeShops.isNotEmpty()) {
            view?.cleanMap()

            val normalMarker = getDrawableBitmapDescriptor(R.drawable.ic_map_pin)
            for (coffeeShop in coffeeShops) {
                val latLng = LatLng(coffeeShop.latitude, coffeeShop.longitude)

                val distance = coffeeShop.distance.toString()
                normalMarker?.run {
                    val generatedMarker = view?.addMakers(latLng, coffeeShop.name, distance, coffeeShop, this)
                    generatedMarker?.let {
                        markerMap[coffeeShop.id] = it
                    }
                }
            }
        } else {
            view?.showNoCoffeeShopDialog()
        }
    }
}
