package tw.com.louis383.coffeefinder.core.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.SettingsClient
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tw.com.louis383.coffeefinder.core.CurrentLocationCarrier
import tw.com.louis383.coffeefinder.core.PermissionChecker
import tw.com.louis383.coffeefinder.core.UserLocationListener
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LocationModule {

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