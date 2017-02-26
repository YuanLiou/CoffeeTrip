package tw.com.louis383.coffeefinder.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tw.com.louis383.coffeefinder.R;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;
import tw.com.louis383.coffeefinder.viewmodel.CoffeeShopViewModel;

/**
 * Created by louis383 on 2017/2/26.
 */

public class CoffeeListAdapter extends RecyclerView.Adapter<CoffeeListAdapter.ViewHolder> {

    private Context context;

    private List<CoffeeShop> coffeeShops;

    public CoffeeListAdapter(Context context) {
        coffeeShops = new ArrayList<>();

        this.context = context.getApplicationContext();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coffee_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CoffeeShopViewModel coffeeShopViewModel = coffeeShops.get(position).getViewModel();
        holder.title.setText(coffeeShopViewModel.getShopName());
        String distanceString = context.getResources().getString(R.string.unit_m, String.valueOf(coffeeShopViewModel.getDistances()));
        holder.distance.setText(distanceString);
        holder.chairPossibility.setText(String.valueOf(coffeeShopViewModel.getSeatPoints()));
        holder.expenseChart.setRating(5.0f - coffeeShopViewModel.getCheapPoints());

        if (coffeeShopViewModel.getWifiPoints() > 0) {
            holder.wifiIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary_orange), PorterDuff.Mode.SRC_IN);
        } else {
            holder.wifiIcon.setColorFilter(0);
        }
    }

    @Override
    public int getItemCount() {
        return coffeeShops.size();
    }

    public void setItems(List<CoffeeShop> coffeeShops) {
        this.coffeeShops.addAll(coffeeShops);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView title, chairPossibility, distance;
        ImageView wifiIcon, bookmarkIcon;
        RatingBar expenseChart;

        public ViewHolder(View itemView) {
            super(itemView);

            this.rootView = itemView;

            title = (TextView) itemView.findViewById(R.id.list_title);
            chairPossibility = (TextView) itemView.findViewById(R.id.list_chair_possibility);
            distance = (TextView) itemView.findViewById(R.id.list_distance);
            wifiIcon = (ImageView) itemView.findViewById(R.id.list_wifi_icon);
            bookmarkIcon = (ImageView) itemView.findViewById(R.id.list_bookmark_icon);
            expenseChart = (RatingBar) itemView.findViewById(R.id.list_chart_money);
        }


    }
}
