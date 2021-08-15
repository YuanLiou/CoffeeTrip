package tw.com.louis383.coffeefinder.model.domain.comparator

import tw.com.louis383.coffeefinder.model.data.entity.Shop

/**
 * Created by louis383 on 2017/3/2.
 */
class DistanceComparator : Comparator<Shop> {
    override fun compare(firstShop: Shop, secondShop: Shop): Int {
        return java.lang.Double.compare(firstShop.distance, secondShop.distance)
    }
}