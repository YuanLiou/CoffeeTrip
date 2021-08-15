package tw.com.louis383.coffeefinder.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.libraries.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.model.CurrentLocationCarrier
import tw.com.louis383.coffeefinder.model.entity.Shop
import tw.com.louis383.coffeefinder.utils.FragmentArgumentDelegate
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment: Fragment() {
    companion object {
        fun newInstance(coffeeShop: Shop) = DetailsFragment().apply {
            this.coffeeShop = coffeeShop
        }
    }

    private var coffeeShop by FragmentArgumentDelegate<Shop>()
    var detailsItemClickListener: DetailsItemClickListener? = null
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var titleText: TextView
    private lateinit var cheapRating: RatingBar

    private lateinit var wiFiPointBar: ProgressBar
    private lateinit var wiFiScoreText: TextView
    private lateinit var seatPointBar: ProgressBar
    private lateinit var seatScoreText: TextView

    private lateinit var webSiteText: TextView
    private lateinit var openTimeText: TextView
    private lateinit var mrtText: TextView

    private lateinit var limitedTimeText: TextView
    private lateinit var socketText: TextView
    private lateinit var standingDestText: TextView

    private lateinit var distanceText: TextView

    @Inject
    lateinit var currentLocationCarrier: CurrentLocationCarrier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveViews(view)
        nestedScrollView = view.findViewById(R.id.detail_view_scrollview)
        setDetailInfo(coffeeShop.getViewModel())

        view.findViewById<Button>(R.id.detail_view_button_navigate).setOnClickListener {
            detailsItemClickListener?.onNavigationButtonClicked()
        }

        view.findViewById<Button>(R.id.detail_view_button_share).setOnClickListener {
            detailsItemClickListener?.onShareButtonClicked()
        }

        view.findViewById<ImageView>(R.id.detail_view_back_button).setOnClickListener {
            detailsItemClickListener?.onBackButtonClicked()
        }
        val anchorOffset = resources.getDimensionPixelOffset(R.dimen.store_panel_anchor_offset)
        view.setPadding(0, 0, 0, anchorOffset)
    }

    private fun retrieveViews(view: View) {
        titleText = view.findViewById<TextView>(R.id.detail_view_title)
        cheapRating = view.findViewById<RatingBar>(R.id.detail_view_expense)

        wiFiPointBar = view.findViewById<ProgressBar>(R.id.detail_view_wifi_quality)
        wiFiScoreText = view.findViewById<TextView>(R.id.detail_view_wifi_score)
        seatPointBar = view.findViewById<ProgressBar>(R.id.detail_view_seat_quality)
        seatScoreText = view.findViewById<TextView>(R.id.detail_view_seat_score)

        webSiteText = view.findViewById<TextView>(R.id.detail_view_website)
        openTimeText = view.findViewById<TextView>(R.id.detail_view_opentime)
        mrtText = view.findViewById<TextView>(R.id.detail_view_mrt)

        limitedTimeText = view.findViewById<TextView>(R.id.detail_view_limited_time)
        socketText = view.findViewById<TextView>(R.id.detail_view_socket)
        standingDestText = view.findViewById<TextView>(R.id.detail_view_standing_desk)

        distanceText = view.findViewById<TextView>(R.id.detail_view_distance)
    }

    fun setDetailInfo(viewModel: CoffeeShopViewModel) {
        with(viewModel) {
            titleText.text = shopName
            cheapRating.rating = cheapPoints

            wiFiPointBar.progress = wifiPoints.toInt() * 20
            wiFiScoreText.text = wifiPoints.toString()
            seatPointBar.progress = seatPoints.toInt() * 20
            seatScoreText.text = seatPoints.toString()

            webSiteText.text = getWebsiteURL(requireContext())
            openTimeText.text = getOpenTimes(requireContext())
            mrtText.text = getMrtInfo(requireContext())

            limitedTimeText.text = getLimitTimeString(requireContext())
            socketText.text = getSocketString(requireContext())
            standingDestText.text = getStandingDeskString(requireContext())

            currentLocationCarrier.currentLocation?.run {
                val currentLatLng = LatLng(latitude, longitude)
                distanceText.text = getDistancesFromLocation(currentLatLng)
            } ?: run {
                distanceText.text = ""
            }
        }
    }

    fun setNestScrollingEnable(enable: Boolean) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            nestedScrollView.isSmoothScrollingEnabled = enable
        }
    }
}