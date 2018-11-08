package tw.com.louis383.coffeefinder.mainpage

import android.content.Context
import android.content.Intent
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.model.LatLng
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop

interface MainView {
    val isInternetAvailable: Boolean
    val activityContext: Context
    fun isApplicationInstalled(packageName: String): Boolean
    fun checkLocationPermission(): Boolean
    fun requestInternetConnection()
    fun requestLocationPermission()
    fun locationSettingNeedsResolution(resolvable: ResolvableApiException)
    fun showServiceUnavailableMessage()
    fun makeSnackBar(message: String, infinity: Boolean)
    fun setStatusBarDarkIndicator()
    fun moveCameraToCurrentPosition(latLng: LatLng)
    fun onCoffeeShopFetched(coffeeShops: List<CoffeeShop>)
    fun navigateToLocation(intent: Intent)
    fun showNeedsGoogleMapMessage()
    fun showBottomSheetDetailView(coffeeShop: CoffeeShop)
    fun shareCoffeeShop(shareIntent: Intent)
    fun updateListPage(coffeeShops : List<CoffeeShop>)
    fun moveMapView(offset: Float)
    fun getViewPagerBottomSheetBehavior(): AnchorBottomSheetBehavior<ViewPager>
}