package tw.com.louis383.coffeefinder.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.libraries.maps.model.LatLng
import kotlinx.android.synthetic.main.detail_info.*
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.mainpage.MainActivity
import tw.com.louis383.coffeefinder.model.CurrentLocationCarrier
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop
import tw.com.louis383.coffeefinder.utils.FragmentArgumentDelegate
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel
import javax.inject.Inject

class DetailsFragment: Fragment() {
    companion object {
        fun newInstance(coffeeShop: CoffeeShop) = DetailsFragment().apply {
            this.coffeeShop = coffeeShop
        }
    }

    private var coffeeShop by FragmentArgumentDelegate<CoffeeShop>()
    var detailsItemClickListener: DetailsItemClickListener? = null

    @Inject
    internal lateinit var currentLocationCarrier: CurrentLocationCarrier

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            context.getAppComponent().inject(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDetailInfo(coffeeShop.viewModel)

        detail_view_button_navigate.setOnClickListener {
            detailsItemClickListener?.onNavigationButtonClicked()
        }

        detail_view_button_share.setOnClickListener {
            detailsItemClickListener?.onShareButtonClicked()
        }

        detail_view_back_button.setOnClickListener {
            detailsItemClickListener?.onBackButtonClicked()
        }

        val anchorOffset = resources.getDimensionPixelOffset(R.dimen.store_panel_anchor_offset)
        view.setPadding(0, 0, 0, anchorOffset)
    }

    fun setDetailInfo(viewModel: CoffeeShopViewModel) {
        with(viewModel) {
            detail_view_title.text = shopName
            detail_view_expense.rating = cheapPoints

            detail_view_wifi_quality.progress = wifiPoints.toInt() * 20
            detail_view_wifi_score.text = wifiPoints.toString()
            detail_view_seat_quality.progress = seatPoints.toInt() * 20
            detail_view_seat_score.text = seatPoints.toString()

            detail_view_website.text = getWebsiteURL(context)
            detail_view_opentime.text = getOpenTimes(context)
            detail_view_mrt.text = getMrtInfo(context)

            detail_view_limited_time.text = getLimitTimeString(context)
            detail_view_socket.text = getSocketString(context)
            detail_view_standing_desk.text = getStandingDeskString(context)

            currentLocationCarrier.currentLocation?.run {
                val currentLatLng = LatLng(latitude, longitude)
                detail_view_distance.text = getDistancesFromLocation(currentLatLng)
            } ?: run {
                detail_view_distance.text = ""
            }
        }
    }

    fun setNestScrollingEnable(enable: Boolean) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            detail_view_scrollview.isSmoothScrollingEnabled = enable
        }
    }
}