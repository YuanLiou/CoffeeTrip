package tw.com.louis383.coffeefinder;

import android.support.v4.app.Fragment;

import java.util.List;

import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/2/26.
 */

public abstract class BaseFragment extends Fragment {
    abstract public void prepareCoffeeShops(List<CoffeeShop> coffeeShops);
}
