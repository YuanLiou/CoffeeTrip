package tw.com.louis383.coffeefinder.model;

import android.location.Location;
import android.util.Log;

import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tw.com.louis383.coffeefinder.model.comparator.DistanceComparator;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/2/23.
 */

public class CoffeeShopListManager {

    private CoffeeTripAPI coffeeTripAPI;
    private List<CoffeeShop> coffeeShops;
    private CoffeeShopListManager.Callback callback;

    private Disposable connection;

    public CoffeeShopListManager(CoffeeTripAPI coffeeTripAPI) {
        this.coffeeTripAPI = coffeeTripAPI;

        coffeeShops = new ArrayList<>();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void fetch(Location location, int range) {
        connection = coffeeTripAPI.getCoffeeShops(location.getLatitude(), location.getLongitude(), range)
                .subscribe(listResponse -> {
                    List<CoffeeShop> coffeeShops = listResponse.body();
                    if (callback != null) {
                        Collections.sort(coffeeShops, new DistanceComparator());
                        callback.onCoffeeShopFetchedComplete(coffeeShops);
                    }
                }, throwable -> {
                    Log.e("CoffeeShopListManager", Log.getStackTraceString(throwable));
                    if (callback != null) {
                        callback.onCoffeeShopFetchedFailed(throwable.getLocalizedMessage());
                    }
                });
    }

    public List<CoffeeShop> getCoffeeShops() {
        if (coffeeShops != null) {
            return coffeeShops;
        }

        return null;
    }

    public void stop() {
        if (connection != null && !connection.isDisposed()) {
            connection.dispose();
            Log.i("CoffeeShopListManager", "stop coffee shop fetching.");
        }
    }

    public int getCoffeeShopsCount() {
        return coffeeShops.size();
    }

    public interface Callback {
        void onCoffeeShopFetchedComplete(List<CoffeeShop> coffeeShops);
        void onCoffeeShopFetchedFailed(String message);
    }
}
