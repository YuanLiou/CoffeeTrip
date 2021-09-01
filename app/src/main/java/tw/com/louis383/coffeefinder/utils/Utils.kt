package tw.com.louis383.coffeefinder.utils

import android.content.Context
import androidx.annotation.StringRes

/**
 * Created by louis383 on 2017/1/13.
 */
fun Context.getResourceString(@StringRes stringId: Int): String {
    return resources.getString(stringId)
}
