package tw.com.louis383.coffeefinder;

import android.content.Context;

/**
 * Created by louis383 on 2017/1/13.
 */

public class Utils {
    public static String getResourceString(Context context, int stringId) {
        return context.getResources().getString(stringId);
    }

    public static boolean isDebuggingBuild() {
        return BuildConfig.BUILD_TYPE.equals("debug");
    }
}
