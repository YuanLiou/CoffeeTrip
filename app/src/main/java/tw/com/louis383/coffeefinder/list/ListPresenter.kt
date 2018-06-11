package tw.com.louis383.coffeefinder.list

import tw.com.louis383.coffeefinder.BasePresenter
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop

/**
 * Created by louis383 on 2017/2/21.
 */

class ListPresenter : BasePresenter<CoffeeShopListView>() {

    fun prepareToShowCoffeeShops(coffeeShops: List<CoffeeShop>) {
        if (coffeeShops.isNotEmpty()) {
            view?.setItems(coffeeShops)
            view?.setRecyclerViewVisibility(true)
        } else {
            view?.showNoCoffeeShopMessage()
            view?.setNoCoffeeShopPictureVisibility(true)
        }
        view?.setLoadingProgressBarVisibility(false)
    }

}
