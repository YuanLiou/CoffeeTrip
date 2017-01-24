package tw.com.louis383.coffeefinder.viewmodel;

import android.net.Uri;
import android.text.TextUtils;

import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/1/24.
 */

public class CoffeeShopViewModel {

    private static final String CAFE_NOMAD_PATH = "https://cafenomad.tw/shop/";

    private CoffeeShop coffeeShop;

    public CoffeeShopViewModel(CoffeeShop coffeeShop) {
        this.coffeeShop = coffeeShop;
    }

    public String getShopName() {
        return !TextUtils.isEmpty(coffeeShop.getName()) ? coffeeShop.getName() : "";
    }

    public Uri getDetailUri() {
        return Uri.parse(CAFE_NOMAD_PATH + coffeeShop.getId());
    }

    public int getDistances() {
        return (int) coffeeShop.getDistance();
    }

    public float getWifiPoints() {
        return coffeeShop.getWifi();
    }

    public float getSeatPoints() {
        return coffeeShop.getSeat();
    }

    public float getQueitPoints() {
        return coffeeShop.getQuiet();
    }

    public float getCheapPoints() {
        return coffeeShop.getCheap();
    }
}
