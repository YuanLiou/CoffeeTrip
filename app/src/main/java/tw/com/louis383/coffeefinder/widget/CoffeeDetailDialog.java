package tw.com.louis383.coffeefinder.widget;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;

/**
 * Created by louis383 on 2017/1/23.
 */

public class CoffeeDetailDialog {

    private Context context;
    private AlertDialog coffeeDialog;

    private ProgressBar wifiProgress, cheapProgress, seatProgress, quietProgress;
    private TextView wifiValue, cheapValue, seatValue, quietValue;
    private TextView shopName, shopDistance;
    private ImageView openBrowser, wifi;

    private CoffeeShopViewModel coffeeShopViewModel;
    private CoffeeDetailDialog.Callback callback;

    public CoffeeDetailDialog(Context context, CoffeeShopViewModel coffeeShopViewModel, CoffeeDetailDialog.Callback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;

        View dialogView = LayoutInflater.from(this.context).inflate(R.layout.coffee_detailed, null);
        wifi = (ImageView) dialogView.findViewById(R.id.coffee_detailed_wifi);
        wifiProgress = (ProgressBar) dialogView.findViewById(R.id.coffee_detailed_wifi_progress);
        wifiValue = (TextView) dialogView.findViewById(R.id.coffee_detailed_wifi_value);
        cheapProgress = (ProgressBar) dialogView.findViewById(R.id.coffee_detailed_cheap_progress);
        cheapValue = (TextView) dialogView.findViewById(R.id.coffee_detailed_cheap_value);
        seatProgress = (ProgressBar) dialogView.findViewById(R.id.coffee_detailed_seat_progress);
        seatValue = (TextView) dialogView.findViewById(R.id.coffee_detailed_seat_value);
        quietProgress = (ProgressBar) dialogView.findViewById(R.id.coffee_detailed_quiet_progress);
        quietValue = (TextView) dialogView.findViewById(R.id.coffee_detailed_quiet_value);
        shopName = (TextView) dialogView.findViewById(R.id.coffee_detailed_title);
        shopDistance = (TextView) dialogView.findViewById(R.id.coffee_detailed_distance);
        openBrowser = (ImageView) dialogView.findViewById(R.id.coffee_detailed_open_web);
        openBrowser.setOnClickListener(v -> callback.onOpenWebsiteButtonClicked(coffeeShopViewModel));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dismiss());
        builder.setPositiveButton(getString(R.string.dialog_navigate), (dialog, which) -> callback.onNavigationTextClicked(coffeeShopViewModel));

        coffeeDialog = builder.create();

        setupCoffeeShop(coffeeShopViewModel);
    }

    public void setupCoffeeShop(CoffeeShopViewModel viewModel) {
        this.coffeeShopViewModel = viewModel;

        setTitle(viewModel.getShopName());
        setDistance(viewModel.getDistances());
        setWifiPoint(viewModel.getWifiPoints());
        setCheapPoint(viewModel.getCheapPoints());
        setSeatPoint(viewModel.getSeatPoints());
        setQuietPoint(viewModel.getQueitPoints());
    }

    public void show() {
        coffeeDialog.show();
    }

    public void dismiss() {
        coffeeDialog.dismiss();
    }

    public void setWifiPoint(float point) {
        if (point < 0f) {
            return;
        }

        if (point > 0f) {
            if (point == 5.0) {
                wifi.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_24px);
            } else {
                wifi.setImageResource(R.drawable.ic_network_wifi_black_24px);
            }

            int progress = getProgressIntegerValue(point);
            wifiProgress.setProgress(progress);
        } else {
            wifiProgress.setProgress(0);
            wifi.setImageResource(R.drawable.ic_signal_wifi_off_black_24px);
        }

        wifiValue.setText(context.getResources().getString(R.string.wifi_value, Float.toString(point)));
    }

    public void setCheapPoint(float point) {
        if (point >= 0f) {
            int progress = getProgressIntegerValue(point);
            cheapProgress.setProgress(progress);
            cheapValue.setText(context.getResources().getString(R.string.cheap_value, Float.toString(point)));
        }
    }

    public void setSeatPoint(float point) {
        if (point >= 0f) {
            int progress = getProgressIntegerValue(point);
            seatProgress.setProgress(progress);
            seatValue.setText(context.getResources().getString(R.string.seat_value, Float.toString(point)));
        }
    }

    public void setQuietPoint(float point) {
        if (point >= 0f) {
            int progress = getProgressIntegerValue(point);
            quietProgress.setProgress(progress);
            quietValue.setText(context.getResources().getString(R.string.quiet_value, Float.toString(point)));
        }
    }

    public void setTitle(String title) {
        shopName.setText(title);
    }

    public void setDistance(int distance) {
        shopDistance.setText(context.getResources().getString(R.string.unit_m, String.valueOf(distance)));
    }

    public boolean isShowing() {
        if (coffeeDialog != null) {
            return coffeeDialog.isShowing();
        }

        return false;
    }

    private String getString(int resStringId) {
        String result = context.getResources().getString(resStringId);
        return !TextUtils.isEmpty(result) ? result : "";
    }

    private int getProgressIntegerValue(float point) {
        return (int) (point * 10.0);
    }

    public interface Callback {
        void onNavigationTextClicked(CoffeeShopViewModel viewModel);
        void onOpenWebsiteButtonClicked(CoffeeShopViewModel viewModel);
    }
}
