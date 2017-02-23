package tw.com.louis383.coffeefinder.utils;

import android.content.Context;

import tw.com.louis383.coffeefinder.BuildConfig;

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
