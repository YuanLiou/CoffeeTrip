package tw.com.louis383.coffeefinder.viewmodel;

import com.google.android.libraries.maps.model.LatLng;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/1/24.
 */

public class CoffeeShopViewModel {

    private static final String CAFE_NOMAD_PATH = "https://cafenomad.tw/shop/";

    private CoffeeShop coffeeShop;

    private Map<String, Integer> limitedTimeStringResources;
    private Map<String, Integer> socketStringResources;
    private Map<Boolean, Integer> standingDeskStringResources;

    public CoffeeShopViewModel(CoffeeShop coffeeShop) {
        this.coffeeShop = coffeeShop;
        initStringMaps();
    }

    private void initStringMaps() {
        limitedTimeStringResources = new HashMap<>();
        limitedTimeStringResources.put("yes", R.string.limit_time_yes);
        limitedTimeStringResources.put("maybe", R.string.limit_time_maybe);
        limitedTimeStringResources.put("no", R.string.limit_time_no);

        socketStringResources = new HashMap<>();
        socketStringResources.put("yes", R.string.socket_yes);
        socketStringResources.put("maybe", R.string.socket_maybe);
        socketStringResources.put("no", R.string.socket_no);

        standingDeskStringResources = new HashMap<>();
        standingDeskStringResources.put(true, R.string.standing_desk_yes);
        standingDeskStringResources.put(false, R.string.standing_desk_no);
    }

    public String getShopName() {
        return !TextUtils.isEmpty(coffeeShop.getName()) ? coffeeShop.getName() : "";
    }

    public Uri getDetailUri() {
        return Uri.parse(CAFE_NOMAD_PATH + coffeeShop.getId());
    }

    public String getDistancesFromLocation(LatLng latLng) {
        int distance = coffeeShop.calculateDistanceFromLocation(latLng);
        return String.valueOf(distance);
    }

    public float getWifiPoints() {
        return coffeeShop.getWifi();
    }

    public float getSeatPoints() {
        return coffeeShop.getSeat();
    }

    public float getCheapPoints() {
        return (5.0f - coffeeShop.getCheap());
    }

    public String getAddress() {
        return coffeeShop.getAddress();
    }

    public String getWebsiteURL(Context context) {
        String noInfoString = getResourceString(context, R.string.opentime_mrt_none);
        return TextUtils.isEmpty(coffeeShop.getUrl()) ? noInfoString : coffeeShop.getUrl();
    }

    public String getOpenTimes(Context context) {
        String noInfoString = getResourceString(context, R.string.opentime_mrt_none);
        return TextUtils.isEmpty(coffeeShop.getOpenTime()) ? noInfoString : coffeeShop.getOpenTime();
    }

    public String getMrtInfo(Context context) {
        String noInfoString = getResourceString(context, R.string.opentime_mrt_none);
        return TextUtils.isEmpty(coffeeShop.getMrt()) ? noInfoString : coffeeShop.getMrt();
    }

    public String getLimitTimeString(Context context) {
        String limitTimeString = coffeeShop.getLimitedTime();
        if (limitTimeString != null) {
            return getResourceString(context, limitedTimeStringResources.get(coffeeShop.getLimitedTime()));
        }
        return getResourceString(context, R.string.maps_string_null);
    }

    public String getSocketString(Context context) {
        String socketString = coffeeShop.getSocket();
        if (socketString != null) {
            return getResourceString(context, socketStringResources.get(coffeeShop.getSocket()));
        }
        return getResourceString(context, R.string.maps_string_null);
    }

    public String getStandingDeskString(Context context) {
        return getResourceString(context, standingDeskStringResources.get(coffeeShop.isStandingDesk()));
    }

    private String getResourceString(Context context, int stringResId) {
        return context.getResources().getString(stringResId);
    }
}
