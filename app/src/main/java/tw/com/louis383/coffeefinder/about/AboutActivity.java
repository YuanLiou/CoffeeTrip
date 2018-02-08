package tw.com.louis383.coffeefinder.about;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.boss.BossActivity;

/**
 * Created by louis383 on 2017/3/14.
 */

public class AboutActivity extends AppCompatActivity implements AboutPresenter.ViewHandler, View.OnClickListener {

    private AboutPresenter presenter;
    private Animation pokeAnimation;

    private Toolbar toolbar;
    private ImageView logo;
    private TextView versionTitle;
    private TextView message;
    private TextView designerId;
    private TextView designerTitle;
    private ImageView designerIcon;
    private TextView backendId;
    private TextView backendTitle;
    private ImageView backendIcon;
    private TextView androidId;
    private TextView androidTitle;
    private ImageView androidIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        logo = (ImageView) findViewById(R.id.about_logo);
        versionTitle = (TextView) findViewById(R.id.about_title);
        message = (TextView) findViewById(R.id.about_message);
        designerId = (TextView) findViewById(R.id.about_designer);
        designerTitle = (TextView) findViewById(R.id.about_designer_title);
        designerIcon = (ImageView) findViewById(R.id.about_designer_twitter_icon);
        backendId = (TextView) findViewById(R.id.about_backend);
        backendTitle = (TextView) findViewById(R.id.about_backend_title);
        backendIcon = (ImageView) findViewById(R.id.about_backend_twitter_icon);
        androidId = (TextView) findViewById(R.id.about_android);
        androidTitle = (TextView) findViewById(R.id.about_android_title);
        androidIcon = (ImageView) findViewById(R.id.about_android_twitter_icon);

        presenter = new AboutPresenter();
        presenter.attachView(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResourceString(R.string.app_name));
        toolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);

        setupClickEvent();
        pokeAnimation = AnimationUtils.loadAnimation(this, R.anim.poke_effect);
    }

    private void setupClickEvent() {
        designerId.setOnClickListener(this);
        designerTitle.setOnClickListener(this);
        designerIcon.setOnClickListener(this);

        backendId.setOnClickListener(this);
        backendTitle.setOnClickListener(this);
        backendIcon.setOnClickListener(this);

        androidId.setOnClickListener(this);
        androidTitle.setOnClickListener(this);
        androidIcon.setOnClickListener(this);

        logo.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // SupportActionBar Navigation Button Click Event
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void openTwitterPage(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void openBossPage() {
        Intent intent = new Intent(this, BossActivity.class);
        startActivity(intent);
    }

    @Override
    public void setTitle(String title) {
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            title += " V" + version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionTitle.setText(title);
    }

    @Override
    public void setMessage(String messageString) {
        message.setText(messageString);
    }

    @Override
    public String getResourceString(int resId) {
        return getResources().getString(resId);
    }

    @Override
    public String[] getResourceStringArray(int arrayId) {
        return getResources().getStringArray(arrayId);
    }

    @Override
    public void setDarkStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void openProfile(TextView textView, String profileId) {
        textView.startAnimation(pokeAnimation);
        presenter.openTwitterProfile(profileId);
    }

    @Override
    public void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //region View.OnClickListener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_designer:
                openProfile(designerId, designerId.getText().toString());
                break;
            case R.id.about_designer_title:
                openProfile(designerTitle, designerId.getText().toString());
                break;
            case R.id.about_designer_twitter_icon:
                designerIcon.startAnimation(pokeAnimation);
                presenter.openTwitterProfile(designerId.getText().toString());
                break;
            case R.id.about_backend:
                openProfile(backendId, backendId.getText().toString());
                break;
            case R.id.about_backend_title:
                openProfile(backendTitle, backendId.getText().toString());
                break;
            case R.id.about_backend_twitter_icon:
                backendIcon.startAnimation(pokeAnimation);
                presenter.openTwitterProfile(backendId.getText().toString());
                break;
            case R.id.about_android:
                openProfile(androidId, androidId.getText().toString());
                break;
            case R.id.about_android_title:
                openProfile(androidTitle, androidId.getText().toString());
                break;
            case R.id.about_android_twitter_icon:
                androidIcon.startAnimation(pokeAnimation);
                presenter.openTwitterProfile(androidId.getText().toString());
                break;
            case R.id.about_logo:
                logo.startAnimation(pokeAnimation);
                presenter.openTopSecret();
                break;
        }
    }
    //endregion
}
