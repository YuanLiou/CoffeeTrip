package tw.com.louis383.coffeefinder.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager;
import tw.com.louis383.coffeefinder.model.CoffeeTripAPI;
import tw.com.louis383.coffeefinder.model.ConnectivityChecker;
import tw.com.louis383.coffeefinder.model.PreferenceManager;

/**
 * Created by louis383 on 2017/2/22.
 */

@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    CoffeeTripAPI provideCoffeeAPI() {
        CoffeeTripAPI coffeeTripAPI = new CoffeeTripAPI();
        return coffeeTripAPI;
    }

    @Provides
    @Singleton
    CoffeeShopListManager provideCoffeeShopListManager(CoffeeTripAPI coffeeTripAPI) {
        CoffeeShopListManager coffeeShopListManager = new CoffeeShopListManager(coffeeTripAPI);
        return coffeeShopListManager;
    }

    @Provides
    @Singleton
    PreferenceManager providePreferenceManager() {
        return new PreferenceManager(application);
    }

    @Provides
    ConnectivityChecker provideConnectivityChecker() {
        return new ConnectivityChecker(application.getBaseContext());
    }
}
