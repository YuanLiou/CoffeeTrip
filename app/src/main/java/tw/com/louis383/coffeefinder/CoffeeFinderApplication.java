package tw.com.louis383.coffeefinder;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

/**
 * Created by louis383 on 2017/1/24.
 */

public class CoffeeFinderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
