package tw.com.louis383.coffeefinder.core.utils

import tw.com.louis383.coffeefinder.core.domain.model.MapLocation

object MathUtils {
    private const val EARTH_RADIUS = 6378137

    /***
     * Haversine formula. Check Wikipedia: <br>
     * https://en.wikipedia.org/wiki/Haversine_formula
     *
     * @return Double, the unit of the return value is **meter**.
     */
    @JvmStatic
    fun calculateDistance(previousMapLocation: MapLocation, mapLocation: MapLocation): Double {
        val currentLatitude = mapLocation.latitude
        val currentLongitude = mapLocation.longitude

        val radiansPreviousLatitude = Math.toRadians(previousMapLocation.latitude)
        val radiansCurrentLatitude = Math.toRadians(currentLatitude)

        val radiansPreviousLongitude = Math.toRadians(previousMapLocation.longitude)
        val radiansCurrentLongitude = Math.toRadians(currentLongitude)

        val radiansLatitudeChange = radiansPreviousLatitude - radiansCurrentLatitude
        val radiansLongitudeChange = radiansPreviousLongitude - radiansCurrentLongitude

        val distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(radiansLatitudeChange * 0.5), 2.0) + Math.cos(radiansPreviousLatitude) * Math.cos(radiansCurrentLatitude) * Math.pow(Math.sin(radiansLongitudeChange * 0.5), 2.0)))
        return distance * EARTH_RADIUS
    }

}
