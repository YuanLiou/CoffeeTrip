package tw.com.louis383.coffeefinder.di;

import dagger.Component;
import javax.inject.Singleton;
import tw.com.louis383.coffeefinder.mainpage.MainActivity;
import tw.com.louis383.coffeefinder.maps.MapsFragment;
import tw.com.louis383.coffeefinder.model.CoffeeTripAPI;
import tw.com.louis383.coffeefinder.model.PreferenceManager;

/**
 * Created by louis383 on 2017/2/22.
 */

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    CoffeeTripAPI getCoffeeTripAPI();
    PreferenceManager getPreferenceManager();

    void inject(MainActivity mainActivity);
    void inject(MapsFragment mapsFragment);
}
