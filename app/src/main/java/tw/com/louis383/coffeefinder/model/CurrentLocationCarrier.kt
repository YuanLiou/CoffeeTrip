package tw.com.louis383.coffeefinder.model

import android.location.Location
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentLocationCarrier @Inject constructor() {
    var currentLocation: Location? = null
}