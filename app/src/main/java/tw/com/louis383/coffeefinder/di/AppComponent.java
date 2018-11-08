package tw.com.louis383.coffeefinder.di;

import javax.inject.Singleton;

import dagger.Component;
import tw.com.louis383.coffeefinder.details.DetailsFragment;
import tw.com.louis383.coffeefinder.list.ListFragment;
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
    void inject(DetailsFragment detailsFragment);
    void inject(ListFragment listFragment);
}
