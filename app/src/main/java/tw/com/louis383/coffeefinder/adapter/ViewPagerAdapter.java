package tw.com.louis383.coffeefinder.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by louis383 on 2017/2/21.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public static final int MAP_FRAGMENT = 0;
    public static final int LIST_FRAGMENT = 1;

    private List<Fragment> fragments;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
    }

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
        notifyDataSetChanged();
    }

    public void addFragment(Fragment fragment, int order) {
        fragments.add(order, fragment);
    }

    public void clean() {
        this.fragments.clear();
        notifyDataSetChanged();
    }
}
