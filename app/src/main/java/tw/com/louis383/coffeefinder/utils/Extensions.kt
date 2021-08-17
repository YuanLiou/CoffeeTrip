package tw.com.louis383.coffeefinder.utils

import android.app.Activity
import android.location.Location
import android.view.View
import androidx.annotation.IdRes
import com.google.android.libraries.maps.model.LatLng
import tw.com.louis383.coffeefinder.core.domain.model.MapLocation

fun <T: View> Activity.bindView(@IdRes resId: Int): Lazy<T> = lazy {
    findViewById<T>(resId)
}

inline fun <First, Second> ifNotNull(first: First?, second: Second?, action: (First, Second) -> Unit) {
    if (first != null && second != null) {
        action(first, second)
    }
}

fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun MapLocation.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.toMapLocation(): MapLocation {
    return MapLocation(latitude, longitude)
}

