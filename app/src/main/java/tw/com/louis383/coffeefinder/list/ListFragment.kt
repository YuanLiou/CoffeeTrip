package tw.com.louis383.coffeefinder.list

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import tw.com.louis383.coffeefinder.BaseFragment
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.R.layout
import tw.com.louis383.coffeefinder.model.CurrentLocationCarrier
import tw.com.louis383.coffeefinder.model.entity.Shop
import tw.com.louis383.coffeefinder.utils.FragmentArgumentDelegate
import tw.com.louis383.coffeefinder.utils.RecyclerViewDividerHelper
import tw.com.louis383.coffeefinder.view.CoffeeListAdapter
import javax.inject.Inject

/**
 * Created by louis383 on 2017/2/21.
 */

@AndroidEntryPoint
class ListFragment : BaseFragment(), CoffeeShopListView, ListAdapterHandler {
    companion object {
        fun newInstance(coffeeShops: List<Shop>): ListFragment {
            return ListFragment().apply {
                this.coffeeShops = coffeeShops as ArrayList<Shop>
            }
        }
    }

    private var presenter: ListPresenter? = null
    private val coffeeListAdapter: CoffeeListAdapter = CoffeeListAdapter(this)
    private var callback: Callback? = null
    private var coffeeShops by FragmentArgumentDelegate<ArrayList<Shop>>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noCoffeeShopImage: ImageView
    private lateinit var noCoffeeShopMessage: TextView

    private val actionBarHeight: Int
        get() {
            val styledAttribute = context?.theme?.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            val actionBarSize = styledAttribute?.getDimension(0, 0f)?.toInt() ?: 40
            styledAttribute?.recycle()
            return actionBarSize
        }

    @Inject
    fun initPresenter(currentLocationCarrier: CurrentLocationCarrier) {
        presenter = ListPresenter(currentLocationCarrier)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview)
        progressBar = view.findViewById(R.id.list_progressbar)
        noCoffeeShopImage = view.findViewById(R.id.list_none_picture)
        noCoffeeShopMessage = view.findViewById(R.id.list_none_message)

        val dividerHelper = RecyclerViewDividerHelper(activity, RecyclerViewDividerHelper.VERTICAL_LIST, false, false)
        with(recyclerView) {
            visibility = View.INVISIBLE
            addItemDecoration(dividerHelper)
            setPadding(0, actionBarHeight, 0, 0)
            adapter = coffeeListAdapter
        }

        presenter?.attachView(this)
        presenter?.prepareToShowCoffeeShops(coffeeShops)

        val anchorOffset = resources.getDimensionPixelOffset(R.dimen.store_panel_anchor_offset)
        view.setPadding(0, 0, 0, anchorOffset)
    }

    override fun provideLifecycleOwner(): LifecycleOwner {
        return viewLifecycleOwner
    }

    fun setNestScrollingEnable(enable: Boolean) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            recyclerView.isNestedScrollingEnabled = enable
        }
    }

    fun setCallback(callback: Callback) {
        // Setter only
        this.callback = callback
    }

    fun scrollToItemPosition(coffeeShop: Shop) {
        val position = coffeeListAdapter.findPositionInList(coffeeShop)
        if (position > -1 && position < coffeeListAdapter.getItemCount()) {
            recyclerView.smoothScrollToPosition(position)
        }
    }

    override fun showNoCoffeeShopMessage() {
        val message = resources.getString(R.string.dialog_no_coffeeshop_message)
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_INDEFINITE).show()
    }

    override fun setItems(items: List<Shop>) {
        coffeeListAdapter.setItems(items)
    }

    override fun setLoadingProgressBarVisibility(visible: Boolean) {
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setRecyclerViewVisibility(visible: Boolean) {
        recyclerView.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    override fun setNoCoffeeShopPictureVisibility(visible: Boolean) {
        noCoffeeShopImage.visibility = if (visible) View.VISIBLE else View.GONE
        noCoffeeShopMessage.visibility = if (visible) View.VISIBLE else View.GONE
    }

    //region BaseFragment
    override fun prepareCoffeeShops(coffeeShops: List<Shop>) {
        presenter?.prepareToShowCoffeeShops(coffeeShops)
    }
    //endregion

    //region ListAdapterHandler
    override fun onItemTapped(coffeeShop: Shop, index: Int) {
        callback?.onItemTapped(coffeeShop)
    }

    override fun requestCurrentLocation(): Location? {
        return presenter?.getCurrentLocation()
    }

    //endregion

    interface Callback {
        fun onItemTapped(coffeeShop: Shop)
    }
}
