package tw.com.louis383.coffeefinder.list;

import java.util.List;

import tw.com.louis383.coffeefinder.BasePresenter;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/2/21.
 */

public class ListPresenter extends BasePresenter<ListPresenter.ViewHandler> {

    public void prepareToShowCoffeeShops(List<CoffeeShop> coffeeShops) {
        if (!coffeeShops.isEmpty()) {
            view.setItems(coffeeShops);
            view.setRecyclerViewVisibility(true);
        } else {
            view.showNoCoffeeShopMessage();
            view.setNoCoffeeShopPictureVisibility(true);
        }
        view.setLoadingProgressBarVisibility(false);
    }

    public interface ViewHandler {
        void setLoadingProgressBarVisibility(boolean visible);
        void setRecyclerViewVisibility(boolean visible);
        void setNoCoffeeShopPictureVisibility(boolean visible);
        void showNoCoffeeShopMessage();
        void setItems(List<CoffeeShop> items);
    }
}
