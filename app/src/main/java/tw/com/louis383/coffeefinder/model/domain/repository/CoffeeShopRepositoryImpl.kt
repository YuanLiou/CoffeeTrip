package tw.com.louis383.coffeefinder.model.domain.repository

import android.location.Location
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.com.louis383.coffeefinder.model.data.api.CoffeeTripService
import tw.com.louis383.coffeefinder.model.data.mapper.NetworkShopListMapper
import tw.com.louis383.coffeefinder.model.domain.Result
import tw.com.louis383.coffeefinder.model.domain.SimpleResult
import tw.com.louis383.coffeefinder.model.domain.comparator.DistanceComparator
import tw.com.louis383.coffeefinder.model.domain.model.CoffeeShop
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by louis383 on 2017/2/23.
 */

@Singleton
class CoffeeShopRepositoryImpl @Inject constructor(
    private val mapper: NetworkShopListMapper,
    private val coffeeTripApi: CoffeeTripService
) : CoffeeShopRepository {

    private val tag = "CoffeeShopRepositoryImp"

    var coffeeShops: List<CoffeeShop> = emptyList()
        private set

    override fun coffeeShopsCount(): Int {
        return coffeeShops.size
    }

    override suspend fun getNearByCoffeeShopsAsync(location: Location, range: Int): SimpleResult<List<CoffeeShop>> {
        return try {
            val result = coffeeTripApi.getCoffeeShops(location.latitude, location.longitude, range)
            if (result.isSuccessful) {
                val shops = result.body()?.run {
                    val coffeeShops = mapper.map(this)
                    sortWithDistance(coffeeShops)
                }.orEmpty()
                this@CoffeeShopRepositoryImpl.coffeeShops = shops
                Result.Success(shops)
            } else {
                this@CoffeeShopRepositoryImpl.coffeeShops = emptyList()
                Result.Success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(tag, Log.getStackTraceString(e))
            Result.failed(e)
        }
    }

    private suspend fun sortWithDistance(coffeeShops: List<CoffeeShop>) = withContext(Dispatchers.Default) {
        Collections.sort(coffeeShops, DistanceComparator())
        coffeeShops
    }
}
