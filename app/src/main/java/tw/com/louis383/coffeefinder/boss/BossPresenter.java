package tw.com.louis383.coffeefinder.boss;

import android.content.Intent;
import android.net.Uri;

import tw.com.louis383.coffeefinder.BasePresenter;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.model.PreferenceManager;

/**
 * Created by louis383 on 2017/3/15.
 */

public class BossPresenter extends BasePresenter<BossPresenter.ViewHandler> {

    private static final String HOST = "https://twitter.com/";

    private PreferenceManager preferenceManager;

    public BossPresenter(PreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
    }

    @Override
    public void attachView(ViewHandler view) {
        super.attachView(view);
        view.setDarkStatusBar();

        if (!preferenceManager.isMetBoss()) {
            preferenceManager.setBossHasMet(true);

            String message = view.getResourceString(R.string.trophy_boss);
            view.showSnackBar(message, true);
        }
    }

    public void openTwitterProfile(String id) {
        String twitterId = id.substring(1);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(HOST + twitterId));
        view.openTwitterPage(intent);
    }

    public interface ViewHandler {
        String getResourceString(int stringId);
        void setDarkStatusBar();
        void openTwitterPage(Intent intent);
        void showSnackBar(String message, boolean longDuration);
    }
}
