package tw.com.louis383.coffeefinder.about;

import android.content.Intent;
import android.net.Uri;

import java.util.Random;

import tw.com.louis383.coffeefinder.BasePresenter;
import tw.com.louis383.coffeefinder.R;

/**
 * Created by louis383 on 2017/3/14.
 */

public class AboutPresenter extends BasePresenter<AboutPresenter.ViewHandler> {

    private static final String TWITTER_HOST = "https://twitter.com/";

    private int counter;

    @Override
    public void attachView(ViewHandler view) {
        super.attachView(view);

        String[] message = view.getResourceStringArray(R.array.message_about);
        Random random = new Random();
        int randomNumber = random.nextInt(message.length);

        view.setDarkStatusBar();
        view.setMessage(message[randomNumber]);
        view.setTitle(view.getResourceString(R.string.app_name));
    }

    public void openTwitterProfile(String id) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String twitterId = id.substring(1);
        intent.setData(Uri.parse(TWITTER_HOST + twitterId));
        view.openTwitterPage(intent);
    }

    public void openTopSecret() {
        counter++;
        if (counter == 5) {
            view.openBossPage();
            counter = 0;
        } else if (counter == 4) {
            view.makeToast("!?");
        }
    }

    public interface ViewHandler {
        String getResourceString(int resId);
        String[] getResourceStringArray(int arrayId);
        void setTitle(String title);
        void setMessage(String message);
        void openTwitterPage(Intent intent);
        void openBossPage();
        void setDarkStatusBar();
        void makeToast(String message);
    }
}
