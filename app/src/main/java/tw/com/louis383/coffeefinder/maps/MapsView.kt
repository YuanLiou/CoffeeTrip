package tw.com.louis383.coffeefinder.maps

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import tw.com.louis383.coffeefinder.BaseView
import tw.com.louis383.coffeefinder.core.domain.model.CoffeeShop

interface MapsView : BaseView {
    fun checkLocationPermission(): Boolean
    fun getResourceDrawable(@DrawableRes resId: Int): Drawable?
    fun addMakers(latLng: LatLng, title: String, snippet: String, coffeeShop: CoffeeShop, icon: BitmapDescriptor): Marker?
    fun moveCamera(latLng: LatLng, zoom: Float?)
    fun setupDetailedMapInterface()
    fun showNoCoffeeShopDialog()
    fun cleanMap()
    fun openDetailView(coffeeShop: CoffeeShop)
}