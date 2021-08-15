package tw.com.louis383.coffeefinder.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tw.com.louis383.coffeefinder.model.CoffeeShopListManager
import tw.com.louis383.coffeefinder.model.CoffeeTripAPI
import tw.com.louis383.coffeefinder.model.ConnectivityChecker
import tw.com.louis383.coffeefinder.model.PermissionChecker
import tw.com.louis383.coffeefinder.model.PreferenceManager
import javax.inject.Singleton

/**
 * Created by louis383 on 2017/2/22.
 */
@InstallIn(SingletonComponent::class)
@Module
class AppModule {

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