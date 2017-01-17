package tw.com.louis383.coffeefinder.model;


import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/1/16.
 */

public interface CoffeeTripService {
    @GET("cafes/")
    Observable<Response<CoffeeShop>> getCoffeeShops(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("range") int range);
}
