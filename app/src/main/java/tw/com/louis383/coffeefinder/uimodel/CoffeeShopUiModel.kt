package tw.com.louis383.coffeefinder.uimodel

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.google.android.gms.maps.model.LatLng
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.core.domain.model.CoffeeShop
import tw.com.louis383.coffeefinder.utils.toMapLocation

/**
 * Created by louis383 on 2017/1/24.
 */

fun CoffeeShop.getUiModel(): CoffeeShopUiModel {
    return CoffeeShopUiModel(this)
}

class CoffeeShopUiModel(private val coffeeShop: CoffeeShop) {
    companion object {
        private const val CAFE_NOMAD_PATH = "https://cafenomad.tw/shop/"
    }

    private val limitedTimeStringResources: Map<String, Int> = mapOf(
        "yes" to R.string.limit_time_yes, "maybe" to R.string.limit_time_maybe, "no" to R.string.limit_time_no
    )
    private val socketStringResources: Map<String, Int> = mapOf(
        "yes" to R.string.socket_yes, "maybe" to R.string.socket_maybe, "no" to R.string.socket_no
    )
    private val standingDeskStringResources: Map<Boolean, Int> = mapOf(
        true to R.string.standing_desk_yes, false to R.string.standing_desk_no
    )

    val shopName: String
        get() = if (!TextUtils.isEmpty(coffeeShop.name)) {
            coffeeShop.name
        } else {
            ""
        }
    val detailUri: Uri
        get() = Uri.parse(CAFE_NOMAD_PATH + coffeeShop.id)
    val wifiPoints: Float
        get() = coffeeShop.wifi
    val seatPoints: Float
        get() = coffeeShop.seat
    val cheapPoints: Float
        get() = 5.0f - coffeeShop.cheap
    val address: String
        get() = coffeeShop.address

    fun getDistancesFromLocation(latLng: LatLng): String {
        val distance: Int = coffeeShop.calculateDistanceFromLocation(latLng.toMapLocation())
        return distance.toString()
    }

    fun getWebsiteURL(context: Context): String {
        val noInfoString = context.getResourceString(R.string.opentime_mrt_none)
        return if (TextUtils.isEmpty(coffeeShop.url)) {
            noInfoString
        } else {
            coffeeShop.url
        }
    }

    fun getOpenTimes(context: Context): String {
        val openTime = coffeeShop.openTime
        val noInfoString = context.getResourceString(R.string.opentime_mrt_none)
        return if (!openTime.isNullOrBlank()) {
            openTime
        } else {
            noInfoString
        }
    }

    fun getMrtInfo(context: Context): String {
        val mrtInfo = coffeeShop.mrt
        val noInfoString = context.getResourceString(R.string.opentime_mrt_none)
        return if (!mrtInfo.isNullOrBlank()) {
            mrtInfo
        } else {
            noInfoString
        }
    }

    fun getLimitTimeString(context: Context): String {
        val limitTimeString = coffeeShop.limitedTime
        val stringResource = limitedTimeStringResources[coffeeShop.limitedTime]
        if (limitTimeString != null && stringResource != null) {
            return context.getResourceString(stringResource)
        }
        return context.getResourceString(R.string.maps_string_null)
    }

    fun getSocketString(context: Context): String {
        val socketString = coffeeShop.socket
        val stringResource = socketStringResources[coffeeShop.socket]
        if (socketString != null && stringResource != null) {
            return context.getResourceString(stringResource)
        }
        return context.getResourceString(R.string.maps_string_null)
    }

    fun getStandingDeskString(context: Context): String {
        val stringResource = standingDeskStringResources[coffeeShop.standingDesk]
        if (stringResource != null) {
            return context.getResourceString(stringResource)
        }
        return context.getResourceString(R.string.maps_string_null)
    }

    private fun Context.getResourceString(stringResId: Int): String {
        return resources.getString(stringResId)
    }
}