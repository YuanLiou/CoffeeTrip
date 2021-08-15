package tw.com.louis383.coffeefinder.model;

import android.content.SharedPreferences;

import javax.inject.Inject;

/**
 * Created by louis383 on 2017/3/16.
 */

public class PreferenceManager {

    private static final String BOSS_TROPHY = "BOSS_TROPHY";

    private SharedPreferences preferences;

    @Inject
    public PreferenceManager(SharedPreferences preferences) {
        this.preferences = preferences;
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
