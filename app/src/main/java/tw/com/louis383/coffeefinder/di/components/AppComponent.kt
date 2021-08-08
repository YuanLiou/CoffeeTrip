package tw.com.louis383.coffeefinder.di.components

import dagger.Component
import javax.inject.Singleton
import tw.com.louis383.coffeefinder.di.module.AppModule
import tw.com.louis383.coffeefinder.mainpage.MainActivity
import tw.com.louis383.coffeefinder.maps.MapsFragment
import tw.com.louis383.coffeefinder.details.DetailsFragment
import tw.com.louis383.coffeefinder.di.module.LocationModule
import tw.com.louis383.coffeefinder.list.ListFragment

/**
 * Created by louis383 on 2017/2/22.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class, LocationModule::class))
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mapsFragment: MapsFragment)
    fun inject(detailsFragment: DetailsFragment)
    fun inject(listFragment: ListFragment)
}