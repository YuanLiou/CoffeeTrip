package tw.com.louis383.coffeefinder.view

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.core.domain.model.CoffeeShop
import tw.com.louis383.coffeefinder.list.ListAdapterHandler
import tw.com.louis383.coffeefinder.uimodel.getUiModel
import tw.com.louis383.coffeefinder.utils.canApplyDynamicColor

/**
 * Created by louis383 on 2017/2/26.
 */

class CoffeeListAdapter(private val handler: ListAdapterHandler) : RecyclerView.Adapter<CoffeeListAdapter.ViewHolder>() {

    private var coffeeShops: List<CoffeeShop> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.coffee_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val index = holder.adapterPosition
        val coffeeShop = coffeeShops[index]
        val coffeeShopUiModel = coffeeShop.getUiModel()

        val context = holder.rootView.context

        val distanceString = handler.requestCurrentLocation()?.run {
            val distance = coffeeShopUiModel.getDistancesFromLocation(LatLng(latitude, longitude))
            context.resources.getString(R.string.unit_m, distance)
        } ?: ""

        with(holder) {
            title.text = coffeeShopUiModel.shopName
            distance.text = distanceString
            expenseChart.rating = coffeeShopUiModel.cheapPoints
            if (coffeeShopUiModel.wifiPoints > 0) {
                if (canApplyDynamicColor()) {
                    wifiIcon.setColorFilter(ContextCompat.getColor(context, R.color.dynamic_light_primary), PorterDuff.Mode.SRC_IN)
                } else {
                    wifiIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary_orange), PorterDuff.Mode.SRC_IN)
                }
            } else {
                wifiIcon.setColorFilter(0)
            }

            rootView.setOnClickListener {
                if (index != RecyclerView.NO_POSITION) {
                    handler.onItemTapped(coffeeShop, index)
                }
            }

            if (canApplyDynamicColor()) {
                expenseChart.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dynamic_light_primary))
            }
        }
    }

    fun findPositionInList(coffeeShop: CoffeeShop): Int {
        if (coffeeShops.isNotEmpty()) {
            return coffeeShops.indexOf(coffeeShop)
        }
        return -1
    }

    override fun getItemCount(): Int = coffeeShops.size

    fun setItems(coffeeShops: List<CoffeeShop>) {
        this.coffeeShops = coffeeShops
        notifyDataSetChanged()
    }

    class ViewHolder(internal val rootView: View) : RecyclerView.ViewHolder(rootView) {
        val title: TextView = rootView.findViewById(R.id.list_title)
        val distance: TextView = rootView.findViewById(R.id.list_distance)
        val expenseNoData: TextView = rootView.findViewById(R.id.list_chart_money_nodata)
        val wifiIcon: ImageView = rootView.findViewById(R.id.list_wifi_icon)
        val bookmarkIcon: ImageView = rootView.findViewById(R.id.list_bookmark_icon)
        val expenseChart: RatingBar = rootView.findViewById(R.id.list_chart_money)
    }
}
