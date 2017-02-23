package tw.com.louis383.coffeefinder;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import tw.com.louis383.coffeefinder.di.AppComponent;
import tw.com.louis383.coffeefinder.di.AppModule;
import tw.com.louis383.coffeefinder.di.DaggerAppComponent;

/**
 * Created by louis383 on 2017/1/24.
 */

public class CoffeeTripApplication extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
