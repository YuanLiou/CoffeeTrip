package tw.com.louis383.coffeefinder

import androidx.fragment.app.Fragment
import tw.com.louis383.coffeefinder.model.data.entity.Shop

/**
 * Created by louis383 on 2017/2/26.
 */
abstract class BaseFragment : Fragment() {
    abstract fun prepareCoffeeShops(coffeeShops: List<Shop>)
}