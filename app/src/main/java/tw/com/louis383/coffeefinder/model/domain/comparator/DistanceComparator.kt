package tw.com.louis383.coffeefinder.model.domain.comparator

import tw.com.louis383.coffeefinder.model.domain.model.CoffeeShop

/**
 * Created by louis383 on 2017/3/2.
 */
class DistanceComparator : Comparator<CoffeeShop> {
    override fun compare(firstShop: CoffeeShop, secondShop: CoffeeShop): Int {
        return java.lang.Double.compare(firstShop.distance, secondShop.distance)
    }
}