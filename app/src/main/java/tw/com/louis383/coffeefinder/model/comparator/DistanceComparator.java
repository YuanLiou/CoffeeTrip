package tw.com.louis383.coffeefinder.model.comparator;

import java.util.Comparator;

import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/3/2.
 */

public class DistanceComparator implements Comparator<CoffeeShop> {

    @Override
    public int compare(CoffeeShop o1, CoffeeShop o2) {
        return Double.compare(o1.getDistance(), o2.getDistance());
    }
}
