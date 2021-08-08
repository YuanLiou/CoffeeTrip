package tw.com.louis383.coffeefinder.di.module

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.SettingsClient
import dagger.Module
import dagger.Provides
import dagger.Reusable
import tw.com.louis383.coffeefinder.di.ApplicationContext
import tw.com.louis383.coffeefinder.model.CurrentLocationCarrier
import tw.com.louis383.coffeefinder.model.PermissionChecker
import tw.com.louis383.coffeefinder.model.UserLocationListener
import javax.inject.Singleton

@Module
class LocationModule {

    @Singleton
    @Provides
    fun provideLocationCarrier(): CurrentLocationCarrier {
        return CurrentLocationCarrier()
    }

    @Reusable
    @Provides
    fun provideUserLocationListener(
        fusedLocationProviderClient: FusedLocationProviderClient,
        settingsClient: SettingsClient,
        currentLocationCarrier: CurrentLocationCarrier,
        permissionChecker: PermissionChecker
    ): UserLocationListener {
        return UserLocationListener(
            fusedLocationProviderClient,
            settingsClient,
            currentLocationCarrier,
            permissionChecker
        )
    }

    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    fun provideSettingsClient(
        @ApplicationContext context: Context
    ): SettingsClient {
        return LocationServices.getSettingsClient(context)
    }
}