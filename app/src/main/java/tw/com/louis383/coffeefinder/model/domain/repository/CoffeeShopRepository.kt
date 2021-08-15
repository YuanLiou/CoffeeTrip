package tw.com.louis383.coffeefinder.model.domain.repository

import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.com.louis383.coffeefinder.model.data.CoffeeTripService
import tw.com.louis383.coffeefinder.model.domain.comparator.DistanceComparator
import tw.com.louis383.coffeefinder.model.data.entity.Shop
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by louis383 on 2017/2/23.
 */

@Singleton
class CoffeeShopRepository @Inject constructor(private val coffeeTripApi: CoffeeTripService) {

    var coffeeShops: List<Shop> = emptyList()
        private set

    val coffeeShopsCount: Int
        get() = coffeeShops.size

    suspend fun getNearByCoffeeShopsAsync(location: Location, range: Int) = withContext(Dispatchers.IO) {
        val listResult = coffeeTripApi.getCoffeeShops(location.latitude, location.longitude, range)
        if (listResult.isSuccessful) {
            val shops = listResult.body()?.apply {
                sortWithDistance(this)
            } ?: emptyList()
            this@CoffeeShopRepository.coffeeShops = shops
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
