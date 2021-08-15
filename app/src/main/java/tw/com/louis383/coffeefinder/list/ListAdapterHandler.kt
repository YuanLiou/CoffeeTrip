package tw.com.louis383.coffeefinder.list

import android.location.Location
import tw.com.louis383.coffeefinder.model.data.entity.Shop

/**
 * Created by louis383 on 2017/3/8.
 */

interface ListAdapterHandler {
    fun requestCurrentLocation(): Location?
    fun onItemTapped(coffeeShop: Shop, index: Int)
}
