package tw.com.louis383.coffeefinder.boss;

import android.content.Intent;
import android.net.Uri;

import tw.com.louis383.coffeefinder.BasePresenter;

/**
 * Created by louis383 on 2017/3/15.
 */

public class BossPresenter extends BasePresenter<BossPresenter.ViewHandler> {

    private static final String HOST = "https://twitter.com/";

    @Override
    public void attachView(ViewHandler view) {
        super.attachView(view);
        view.setDarkStatusBar();
    }

    public void openTwitterProfile(String id) {
        String twitterId = id.substring(1);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(HOST + twitterId));
        view.openTwitterPage(intent);
    }

    public interface ViewHandler {
        void setDarkStatusBar();
        void openTwitterPage(Intent intent);
    }
}
