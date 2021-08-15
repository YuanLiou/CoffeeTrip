package tw.com.louis383.coffeefinder.model.domain.repository

import android.location.Location
import tw.com.louis383.coffeefinder.model.data.entity.Shop

interface CoffeeShopRepository {
    fun coffeeShopsCount(): Int
    suspend fun getNearByCoffeeShopsAsync(location: Location, range: Int): List<Shop>
}