package tw.com.louis383.coffeefinder.model.domain.repository

import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.com.louis383.coffeefinder.model.data.CoffeeTripService
import tw.com.louis383.coffeefinder.model.data.entity.Shop
import tw.com.louis383.coffeefinder.model.domain.comparator.DistanceComparator
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by louis383 on 2017/2/23.
 */

@Singleton
class CoffeeShopRepositoryImpl @Inject constructor(private val coffeeTripApi: CoffeeTripService) :
    CoffeeShopRepository {

    var coffeeShops: List<Shop> = emptyList()
        private set

    override fun coffeeShopsCount(): Int {
        return coffeeShops.size
    }

    override suspend fun getNearByCoffeeShopsAsync(location: Location, range: Int): List<Shop> {
        val listResult = withContext(Dispatchers.IO) {
            coffeeTripApi.getCoffeeShops(
                location.latitude,
                location.longitude,
                range
            )
        }

        return if (listResult.isSuccessful) {
            val shops = listResult.body()?.apply {
                sortWithDistance(this)
            }.orEmpty()
            this@CoffeeShopRepositoryImpl.coffeeShops = shops
            shops
        } else {
            this@CoffeeShopRepositoryImpl.coffeeShops = emptyList()
            emptyList()
        }
    }

    private suspend fun sortWithDistance(coffeeShops: List<Shop>) = withContext(Dispatchers.Default) {
        Collections.sort(coffeeShops, DistanceComparator())
        coffeeShops
    }
}
