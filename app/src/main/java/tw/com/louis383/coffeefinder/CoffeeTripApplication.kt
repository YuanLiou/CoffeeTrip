package tw.com.louis383.coffeefinder

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import tw.com.louis383.coffeefinder.di.components.AppComponent
import tw.com.louis383.coffeefinder.di.components.DaggerAppComponent
import tw.com.louis383.coffeefinder.di.module.AppModule

/**
 * Created by louis383 on 2017/1/24.
 */
class CoffeeTripApplication : Application() {
    var appComponent: AppComponent? = null
        private set

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }
}