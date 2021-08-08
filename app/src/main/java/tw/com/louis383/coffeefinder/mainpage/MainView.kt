package tw.com.louis383.coffeefinder.mainpage

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.libraries.maps.model.LatLng
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import tw.com.louis383.coffeefinder.BaseView
import tw.com.louis383.coffeefinder.model.entity.Shop

interface MainView : BaseView {
    fun isApplicationInstalled(packageName: String): Boolean
    fun checkLocationPermission(): Boolean
    fun requestInternetConnection()
    fun requestLocationPermission()
    fun locationSettingNeedsResolution(resolvable: ResolvableApiException)
    fun showServiceUnavailableMessage()
    fun makeSnackBar(message: String, infinity: Boolean)
    fun makeSnackBar(@StringRes stringRes: Int = -1)
    fun setStatusBarDarkIndicator()
    fun moveCameraToCurrentPosition(latLng: LatLng)
    fun onCoffeeShopFetched(coffeeShops: List<Shop>)
    fun navigateToLocation(intent: Intent)
    fun showNeedsGoogleMapMessage()
    fun showBottomSheetDetailView(coffeeShop: Shop)
    fun shareCoffeeShop(shareIntent: Intent)
    fun updateListPage(coffeeShops : List<Shop>)
    fun moveMapView(offset: Float)
    fun getViewPagerBottomSheetBehavior(): AnchorBottomSheetBehavior<ViewPager>
}