package tw.com.louis383.coffeefinder.utils

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes

fun <T: View> Activity.bindView(@IdRes resId: Int): Lazy<T> = lazy {
    findViewById<T>(resId)
}

inline fun <First, Second> ifNotNull(first: First?, second: Second?, action: (First, Second) -> Unit) {
    if (first != null && second != null) {
        action(first, second)
    }
}

