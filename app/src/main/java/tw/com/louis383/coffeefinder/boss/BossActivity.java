package tw.com.louis383.coffeefinder.boss;

import android.content.Intent;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.com.louis383.coffeefinder.R;

/**
 * Created by louis383 on 2017/3/15.
 */

public class BossActivity extends AppCompatActivity implements BossPresenter.ViewHandler, View.OnClickListener {

    private BossPresenter presenter;
    private Animation pokeAnimation;

    @BindView(R.id.boss_toolbar) Toolbar toolbar;
    @BindView(R.id.boss_twitter) TextView bossId;
    @BindView(R.id.boss_twitter_icon) ImageView bossTwitterIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss);

        ButterKnife.bind(this);

        presenter = new BossPresenter();
        presenter.attachView(this);

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
