package tw.com.louis383.coffeefinder.list

import tw.com.louis383.coffeefinder.model.domain.CoffeeShop

interface CoffeeShopListView {
    fun setLoadingProgressBarVisibility(visible: Boolean)
    fun setRecyclerViewVisibility(visible: Boolean)
    fun setNoCoffeeShopPictureVisibility(visible: Boolean)
    fun showNoCoffeeShopMessage()
    fun setItems(items: List<CoffeeShop>)
}