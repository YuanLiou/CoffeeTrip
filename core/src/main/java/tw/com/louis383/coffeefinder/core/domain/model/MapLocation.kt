package tw.com.louis383.coffeefinder.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapLocation(
    val latitude: Double,
    val longitude: Double
) : Parcelable
