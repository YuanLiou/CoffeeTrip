package tw.com.louis383.coffeefinder.model

import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.com.louis383.coffeefinder.model.comparator.DistanceComparator
import tw.com.louis383.coffeefinder.model.entity.Shop
import java.util.*

/**
 * Created by louis383 on 2017/2/23.
 */

class CoffeeShopListManager(private val coffeeTripAPI: CoffeeTripAPI) {

    var coffeeShops: List<Shop> = emptyList()
        private set

    val coffeeShopsCount: Int
        get() = coffeeShops.size

    suspend fun getNearByCoffeeShopsAsync(location: Location, range: Int) = withContext(Dispatchers.IO) {
        val listResult = coffeeTripAPI.getCoffeeShops(location.latitude, location.longitude, range)
        if (listResult.isSuccessful) {
            val shops = listResult.body()?.apply {
                sortWithDistance(this)
            } ?: emptyList()
            this@CoffeeShopListManager.coffeeShops = shops
            shops
        } else {
            null
        }
    }

    private suspend fun sortWithDistance(coffeeShops: List<Shop>) = withContext(Dispatchers.Default) {
        Collections.sort(coffeeShops, DistanceComparator())
        coffeeShops
    }
}
