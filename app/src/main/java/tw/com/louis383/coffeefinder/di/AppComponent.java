package tw.com.louis383.coffeefinder.di;

import javax.inject.Singleton;

import dagger.Component;
import tw.com.louis383.coffeefinder.mainpage.MainActivity;
import tw.com.louis383.coffeefinder.maps.MapsFragment;
import tw.com.louis383.coffeefinder.model.CoffeeTripAPI;

/**
 * Created by louis383 on 2017/2/22.
 */

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    CoffeeTripAPI getCoffeeTripAPI();

    void inject(MainActivity mainActivity);
    void inject(MapsFragment mapsFragment);
}
