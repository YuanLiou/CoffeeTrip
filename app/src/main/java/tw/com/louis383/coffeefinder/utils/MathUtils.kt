package tw.com.louis383.coffeefinder.utils

import com.google.android.gms.maps.model.LatLng

object MathUtils {
    private const val EARTH_RADIUS = 6378137

    /***
     * Haversine formula. Check Wikipedia: <br>
     * https://en.wikipedia.org/wiki/Haversine_formula
     *
     * @return Double, the unit of the return value is **meter**.
     */
    @JvmStatic
    fun calculateDistance(previousLocation: LatLng, location: LatLng): Double {
        val currentLatitude = location.latitude
        val currentLongitude = location.longitude

        val radiansPreviousLatitude = Math.toRadians(previousLocation.latitude)
        val radiansCurrentLatitude = Math.toRadians(currentLatitude)

        val radiansPreviousLongitude = Math.toRadians(previousLocation.longitude)
        val radiansCurrentLongitude = Math.toRadians(currentLongitude)

        val radiansLatitudeChange = radiansPreviousLatitude - radiansCurrentLatitude
        val radiansLongitudeChange = radiansPreviousLongitude - radiansCurrentLongitude

        val distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(radiansLatitudeChange * 0.5), 2.0) + Math.cos(radiansPreviousLatitude) * Math.cos(radiansCurrentLatitude) * Math.pow(Math.sin(radiansLongitudeChange * 0.5), 2.0)))
        return distance * EARTH_RADIUS
    }

}
