package tw.com.louis383.coffeefinder.maps

import tw.com.louis383.coffeefinder.model.data.entity.Shop

/**
 * Created by louis383 on 2017/3/7.
 */

interface MapsClickHandler {
    fun onMapClicked()
    fun onMarkerClicked(coffeeShop: Shop)
}
