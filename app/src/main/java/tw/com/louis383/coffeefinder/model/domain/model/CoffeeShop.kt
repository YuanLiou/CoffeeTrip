package tw.com.louis383.coffeefinder.model.domain.model

import android.os.Parcelable
import com.google.android.libraries.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import tw.com.louis383.coffeefinder.uimodel.CoffeeShopUiModel
import tw.com.louis383.coffeefinder.utils.MathUtils

@Parcelize
data class CoffeeShop(
    val id: String,
    val name: String,
    val wifi: Float,
    val seat: Float,
    val cheap: Float,
    val url: String,
    val address: String,
    val location: LatLng,
    val distance: Double,
    val limitedTime: String?,
    val socket: String?,
    val standingDesk: Boolean,
    val mrt: String?,
    val openTime: String?
) : Parcelable {
    fun getUiModel(): CoffeeShopUiModel {
        return CoffeeShopUiModel(this)
    }

    fun calculateDistanceFromLocation(fromLatLng: LatLng): Int {
        return MathUtils.calculateDistance(fromLatLng, location).toInt()
    }
}
