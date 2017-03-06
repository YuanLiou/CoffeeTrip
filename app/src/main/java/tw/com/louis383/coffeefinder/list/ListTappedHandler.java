package tw.com.louis383.coffeefinder.list;

import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/3/8.
 */

public interface ListTappedHandler {
    void onItemTapped(CoffeeShop coffeeShop, int index);
}
