package tw.com.louis383.coffeefinder.model.entity

import android.os.Parcelable
import com.google.android.libraries.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tw.com.louis383.coffeefinder.utils.MathUtils.calculateDistance
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel

@Parcelize
@Serializable
data class Shop(
    val id: String,
    val name: String,
    val city: String,
    val wifi: Float,
    val seat: Float,
    val quiet: Float,
    val tasty: Float,
    val cheap: Float,
    val music: Float,
    val url: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    @SerialName("limited_time") val limitedTime: String?,
    val socket: String?,
    @SerialName("standing_desk") val standingDesk: Boolean? = false,
    val mrt: String?,
    @SerialName("open_time") val openTime: String?
): Parcelable {
    fun getViewModel(): CoffeeShopViewModel {
        return CoffeeShopViewModel(this)
    }

    fun calculateDistanceFromLocation(fromLatLng: LatLng): Int {
        return calculateDistance(fromLatLng, getLatLng()).toInt()
    }

    fun getLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}