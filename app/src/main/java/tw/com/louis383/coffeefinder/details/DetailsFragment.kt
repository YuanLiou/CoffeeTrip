package tw.com.louis383.coffeefinder.details

import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import tw.com.louis383.coffeefinder.R
import tw.com.louis383.coffeefinder.core.CurrentLocationCarrier
import tw.com.louis383.coffeefinder.core.domain.model.CoffeeShop
import tw.com.louis383.coffeefinder.uimodel.CoffeeShopUiModel
import tw.com.louis383.coffeefinder.uimodel.getUiModel
import tw.com.louis383.coffeefinder.utils.FragmentArgumentDelegate
import tw.com.louis383.coffeefinder.utils.QuickCheckUtils
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment: Fragment() {
    companion object {
        fun newInstance(coffeeShop: CoffeeShop) = DetailsFragment().apply {
            this.coffeeShop = coffeeShop
        }
    }

    private var coffeeShop by FragmentArgumentDelegate<CoffeeShop>()
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
    private lateinit var shareBackground: ImageView

    @Inject
    lateinit var currentLocationCarrier: CurrentLocationCarrier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveViews(view)
        if (QuickCheckUtils.canApplyDynamicColor()) {
            applyColorThemes()
        }

        nestedScrollView = view.findViewById(R.id.detail_view_scrollview)
        setDetailInfo(coffeeShop.getUiModel())

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
        titleText = view.findViewById(R.id.detail_view_title)
        cheapRating = view.findViewById(R.id.detail_view_expense)

        wiFiPointBar = view.findViewById(R.id.detail_view_wifi_quality)
        wiFiScoreText = view.findViewById(R.id.detail_view_wifi_score)
        seatPointBar = view.findViewById(R.id.detail_view_seat_quality)
        seatScoreText = view.findViewById(R.id.detail_view_seat_score)

        webSiteText = view.findViewById(R.id.detail_view_website)
        openTimeText = view.findViewById(R.id.detail_view_opentime)
        mrtText = view.findViewById(R.id.detail_view_mrt)

        limitedTimeText = view.findViewById(R.id.detail_view_limited_time)
        socketText = view.findViewById(R.id.detail_view_socket)
        standingDestText = view.findViewById(R.id.detail_view_standing_desk)

        distanceText = view.findViewById(R.id.detail_view_distance)
        shareBackground = view.findViewById(R.id.detail_view_share_background_image)
    }

    private fun applyColorThemes() {
        cheapRating.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dynamic_light_primary))
        wiFiPointBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dynamic_light_secondary))
        wiFiPointBar.progressTintBlendMode = BlendMode.HUE
        seatPointBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dynamic_light_secondary))
        seatPointBar.progressTintBlendMode = BlendMode.HUE

        // set share background to monochrome
        shareBackground.colorFilter = ColorMatrixColorFilter(
            ColorMatrix().also {
                it.setSaturation(0f)
            }
        )
    }

    fun setDetailInfo(uiModel: CoffeeShopUiModel) {
        with(uiModel) {
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