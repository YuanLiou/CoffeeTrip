package tw.com.louis383.coffeefinder

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import tw.com.louis383.coffeefinder.utils.QuickCheckUtils

/**
 * Created by louis383 on 2017/1/24.
 */
@HiltAndroidApp
class CoffeeTripApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (QuickCheckUtils.canApplyDynamicColor()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}