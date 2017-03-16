package tw.com.louis383.coffeefinder.boss;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.com.louis383.coffeefinder.CoffeeTripApplication;
import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.model.PreferenceManager;

/**
 * Created by louis383 on 2017/3/15.
 */

public class BossActivity extends AppCompatActivity implements BossPresenter.ViewHandler, View.OnClickListener {

    private BossPresenter presenter;
    private Animation pokeAnimation;

    @BindView(R.id.boss_rootview) View rootView;
    @BindView(R.id.boss_toolbar) Toolbar toolbar;
    @BindView(R.id.boss_twitter) TextView bossId;
    @BindView(R.id.boss_twitter_icon) ImageView bossTwitterIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss);
        ButterKnife.bind(this);
        ((CoffeeTripApplication) getApplication()).getAppComponent().inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        getSupportActionBar().setTitle(R.string.boss_title);

        toolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        pokeAnimation = AnimationUtils.loadAnimation(this, R.anim.poke_effect);

        bossId.setOnClickListener(this);
        bossTwitterIcon.setOnClickListener(this);
    }

    @Inject
    void initPresenter(PreferenceManager preferenceManager) {
        presenter = new BossPresenter(preferenceManager);
        presenter.attachView(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void setDarkStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    public void openTwitterPage(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void showSnackBar(String message, boolean longDuration) {
        Snackbar snackbar = Snackbar.make(rootView, message, longDuration ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_stars_black_24dp);
        drawable.setColorFilter(ContextCompat.getColor(this, R.color.copper), PorterDuff.Mode.SRC_ATOP);

        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        textView.setCompoundDrawablePadding(8);

        snackbar.show();
    }

    @Override
    public String getResourceString(int stringId) {
        return getResources().getString(stringId);
    }


    //region View.OnClickListener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.boss_twitter:
                bossId.startAnimation(pokeAnimation);
                presenter.openTwitterProfile(bossId.getText().toString());
                break;
            case R.id.boss_twitter_icon:
                bossTwitterIcon.startAnimation(pokeAnimation);
                presenter.openTwitterProfile(bossId.getText().toString());
                break;
        }
    }
    //endregion

}
