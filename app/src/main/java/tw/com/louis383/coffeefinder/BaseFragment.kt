package tw.com.louis383.coffeefinder

import androidx.fragment.app.Fragment
import tw.com.louis383.coffeefinder.core.domain.model.CoffeeShop

/**
 * Created by louis383 on 2017/2/26.
 */
abstract class BaseFragment : Fragment() {
    abstract fun prepareCoffeeShops(coffeeShops: List<CoffeeShop>)
}