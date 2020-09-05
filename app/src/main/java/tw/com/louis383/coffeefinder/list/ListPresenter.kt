package tw.com.louis383.coffeefinder.list

import android.location.Location
import tw.com.louis383.coffeefinder.BasePresenter
import tw.com.louis383.coffeefinder.model.CurrentLocationCarrier
import tw.com.louis383.coffeefinder.model.entity.Shop

/**
 * Created by louis383 on 2017/2/21.
 */

class ListPresenter(private val currentLocationCarrier: CurrentLocationCarrier) : BasePresenter<CoffeeShopListView>() {

    fun prepareToShowCoffeeShops(coffeeShops: List<Shop>) {
        if (coffeeShops.isNotEmpty()) {
            view?.setItems(coffeeShops)
            view?.setRecyclerViewVisibility(true)
        } else {
            view?.showNoCoffeeShopMessage()
            view?.setNoCoffeeShopPictureVisibility(true)
        }
        view?.setLoadingProgressBarVisibility(false)
    }

    fun getCurrentLocation(): Location? {
        return currentLocationCarrier.currentLocation
    }

}
