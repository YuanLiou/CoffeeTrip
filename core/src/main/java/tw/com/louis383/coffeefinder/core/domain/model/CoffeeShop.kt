package tw.com.louis383.coffeefinder.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import tw.com.louis383.coffeefinder.core.utils.MathUtils

@Parcelize
data class CoffeeShop(
    val id: String,
    val name: String,
    val wifi: Float,
    val seat: Float,
    val cheap: Float,
    val url: String,
    val address: String,
    val mapLocation: MapLocation,
    val distance: Double,
    val limitedTime: String?,
    val socket: String?,
    val standingDesk: Boolean,
    val mrt: String?,
    val openTime: String?
) : Parcelable {
    fun calculateDistanceFromLocation(fromLatLng: MapLocation): Int {
        return MathUtils.calculateDistance(fromLatLng, mapLocation).toInt()
    }
}
