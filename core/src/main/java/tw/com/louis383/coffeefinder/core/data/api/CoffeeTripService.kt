package tw.com.louis383.coffeefinder.core.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tw.com.louis383.coffeefinder.core.data.dto.NetworkShop

/**
 * Created by louis383 on 2017/1/16.
 */
interface CoffeeTripService {
    @GET("cafes/")
    suspend fun getCoffeeShops(@Query("latitude") latitude: Double, @Query("longitude") longitude: Double, @Query("range") range: Int): Response<List<NetworkShop>>
}