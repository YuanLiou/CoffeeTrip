package tw.com.louis383.coffeefinder.list

import tw.com.louis383.coffeefinder.BaseView
import tw.com.louis383.coffeefinder.model.data.entity.Shop

interface CoffeeShopListView : BaseView {
    fun setLoadingProgressBarVisibility(visible: Boolean)
    fun setRecyclerViewVisibility(visible: Boolean)
    fun setNoCoffeeShopPictureVisibility(visible: Boolean)
    fun showNoCoffeeShopMessage()
    fun setItems(items: List<Shop>)
}