package tw.com.louis383.coffeefinder.maps;

import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/3/7.
 */

public interface MapsClickHandler {
    void onMapClicked();
    void onMarkerClicked(CoffeeShop coffeeShop);
}
