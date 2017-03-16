package tw.com.louis383.coffeefinder.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by louis383 on 2017/3/16.
 */

public class PreferenceManager {

    private static final String PREFERENCE_NAME = "coffeeTrip_preference";
    // lables
    private static final String BOSS_TROPHY = "BOSS_TROPHY";

    private SharedPreferences preferences;

    public PreferenceManager(Context context) {
        context = context.getApplicationContext();
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isMetBoss() {
        return preferences.getBoolean(BOSS_TROPHY, false);
    }

    public void setBossHasMet(boolean isMet) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(BOSS_TROPHY, isMet);
        editor.apply();
    }
}
