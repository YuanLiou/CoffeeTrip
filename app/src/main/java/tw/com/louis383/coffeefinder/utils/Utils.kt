package tw.com.louis383.coffeefinder.utils

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes

/**
 * Created by louis383 on 2017/1/13.
 */
fun Context.getResourceString(@StringRes stringId: Int): String {
    return resources.getString(stringId)
}

object QuickCheckUtils {
    fun canApplyDynamicColor(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}

