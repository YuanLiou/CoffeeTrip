package tw.com.louis383.coffeefinder.di.module

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import tw.com.louis383.coffeefinder.di.ApplicationContext
import tw.com.louis383.coffeefinder.model.*
import javax.inject.Singleton

/**
 * Created by louis383 on 2017/2/22.
 */
@Module
class AppModule(private val application: Application) {

    @ApplicationContext
    @Provides
    fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideCoffeeAPI(): CoffeeTripAPI {
        return CoffeeTripAPI()
    }

    @Provides
    @Singleton
    fun provideCoffeeShopListManager(coffeeTripAPI: CoffeeTripAPI): CoffeeShopListManager {
        return CoffeeShopListManager(coffeeTripAPI)
    }

    @Provides
    @Singleton
    fun providePreferenceManager(
        @ApplicationContext context: Context
    ): PreferenceManager {
        return PreferenceManager(context)
    }

    @Provides
    fun provideConnectivityChecker(
        @ApplicationContext context: Context
    ): ConnectivityChecker {
        return ConnectivityChecker(context)
    }

    @Provides
    fun providePermissionChecker(
        @ApplicationContext context: Context
    ): PermissionChecker {
        return PermissionChecker(context)
    }
}