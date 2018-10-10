package tw.com.louis383.coffeefinder.model

import android.location.Location
import android.util.Log
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import tw.com.louis383.coffeefinder.model.comparator.DistanceComparator
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop
import java.util.*

/**
 * Created by louis383 on 2017/2/23.
 */

class CoffeeShopListManager(private val coffeeTripAPI: CoffeeTripAPI) {

    private val coffeeShops = mutableListOf<CoffeeShop>()
    private var connection: Disposable? = null

    var callback: CoffeeShopListManager.Callback? = null

    val coffeeShopsCount: Int
        get() = coffeeShops.size

    fun fetch(location: Location, range: Int) {
        connection = coffeeTripAPI.getCoffeeShops(location.latitude, location.longitude, range)
                .subscribeBy (
                    onNext = {
                        if (it.isSuccessful) {
                            it.body()?.run {
                                Collections.sort(this, DistanceComparator())
                                callback?.onCoffeeShopFetchedComplete(this)
                            }
                        }
                    },
                    onError = {
                        Log.e("CoffeeShopListManager", Log.getStackTraceString(it))
                        callback?.onCoffeeShopFetchedFailed(it.localizedMessage)
                    }
                )
    }

    fun getCoffeeShops(): List<CoffeeShop> = coffeeShops

    fun stop() {
        connection?.takeUnless { it.isDisposed }?.dispose()
    }

    interface Callback {
        fun onCoffeeShopFetchedComplete(coffeeShops: List<CoffeeShop>)
        fun onCoffeeShopFetchedFailed(message: String)
    }
}
