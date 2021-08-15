package tw.com.louis383.coffeefinder.model.domain.repository

import android.location.Location
import tw.com.louis383.coffeefinder.model.domain.SimpleResult
import tw.com.louis383.coffeefinder.model.domain.model.CoffeeShop

interface CoffeeShopRepository {
    fun coffeeShopsCount(): Int
    suspend fun getNearByCoffeeShopsAsync(location: Location, range: Int): SimpleResult<List<CoffeeShop>>
}