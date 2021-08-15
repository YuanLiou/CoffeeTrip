package tw.com.louis383.coffeefinder.di.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Created by louis383 on 2017/2/22.
 */
@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun provideDefaultPreference(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val preferenceName = "coffeeTrip_preference"
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }
}