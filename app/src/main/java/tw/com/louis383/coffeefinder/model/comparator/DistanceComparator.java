package tw.com.louis383.coffeefinder.model.comparator;

import java.util.Comparator;

import tw.com.louis383.coffeefinder.model.entity.Shop;

/**
 * Created by louis383 on 2017/3/2.
 */

public class DistanceComparator implements Comparator<Shop> {

    @Override
    public int compare(Shop o1, Shop o2) {
        return Double.compare(o1.getDistance(), o2.getDistance());
    }
}
