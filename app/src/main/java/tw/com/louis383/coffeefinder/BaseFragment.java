package tw.com.louis383.coffeefinder;


import java.util.List;

import androidx.fragment.app.Fragment;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/2/26.
 */

public abstract class BaseFragment extends Fragment {
    abstract public void prepareCoffeeShops(List<CoffeeShop> coffeeShops);
}
