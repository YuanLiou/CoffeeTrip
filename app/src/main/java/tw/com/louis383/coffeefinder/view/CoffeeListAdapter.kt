package tw.com.louis383.coffeefinder.view

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.list.ListTappedHandler
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop

/**
 * Created by louis383 on 2017/2/26.
 */

class CoffeeListAdapter(private val handler: ListTappedHandler) : RecyclerView.Adapter<CoffeeListAdapter.ViewHolder>() {

    private val coffeeShops = mutableListOf<CoffeeShop>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.coffee_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val index = holder.adapterPosition
        val coffeeShop = coffeeShops[index]
        val coffeeShopViewModel = coffeeShop.viewModel

        val context = holder.rootView.context
        val distanceString = context.resources.getString(R.string.unit_m, coffeeShopViewModel.distances.toString())

        with(holder) {
            title.text = coffeeShopViewModel.shopName
            distance.text = distanceString
            expenseChart.rating = coffeeShopViewModel.cheapPoints
            if (coffeeShopViewModel.wifiPoints > 0) {
                wifiIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary_orange), PorterDuff.Mode.SRC_IN)
            } else {
                wifiIcon.setColorFilter(0)
            }

            rootView.setOnClickListener {
                if (index != RecyclerView.NO_POSITION) {
                    handler.onItemTapped(coffeeShop, index)
                }
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
        this.coffeeShops.addAll(coffeeShops)
        notifyDataSetChanged()
    }

    class ViewHolder(internal val rootView: View) : RecyclerView.ViewHolder(rootView) {
        internal val title: TextView = rootView.findViewById(R.id.list_title)
        internal val distance: TextView = rootView.findViewById(R.id.list_distance)
        internal val expenseNoData: TextView = rootView.findViewById(R.id.list_chart_money_nodata)
        internal val wifiIcon: ImageView = rootView.findViewById(R.id.list_wifi_icon)
        internal val bookmarkIcon: ImageView = rootView.findViewById(R.id.list_bookmark_icon)
        internal val expenseChart: RatingBar = rootView.findViewById(R.id.list_chart_money)
    }
}
