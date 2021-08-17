package tw.com.louis383.coffeefinder.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkShop(
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
)