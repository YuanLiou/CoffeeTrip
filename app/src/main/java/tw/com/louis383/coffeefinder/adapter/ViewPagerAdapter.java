package tw.com.louis383.coffeefinder.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import tw.com.louis383.coffeefinder.details.DetailsFragment;
import tw.com.louis383.coffeefinder.list.ListFragment;

/**
 * Created by louis383 on 2017/2/21.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public static final int LIST_FRAGMENT = 0;
    public static final int DETAIL_FRAGMENT = 1;

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

    @Override
    public int getItemPosition(@NonNull final Object object) {
        int index = fragments.indexOf(object);
        return index < 0 ? POSITION_NONE : index;
    }

    // DO NOT CONVERT THIS JAVA SOURCE TO KOTLIN!!
    // CAUTION:: Crash on Kotlin because it can't override this method
    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, final Object object) {
        super.setPrimaryItem(container, position, object);
        if (object == null) {
            return;
        }

        // The default behaviour of BottomSheetBehaviour will only allow first item
        // to be nest scrolling. It needs to be enable nest scrolling every primary view manually.
        // Also, disable other nest scrolling view's function.
        for (int i = 0; i < getCount(); i++) {
            boolean shouldEnable = (i == position);
            Fragment fragment = fragments.get(i);
            if (fragment instanceof ListFragment) {
                ((ListFragment) fragment).setNestScrollingEnable(shouldEnable);
            } else if (fragment instanceof DetailsFragment) {
                ((DetailsFragment) fragment).setNestScrollingEnable(shouldEnable);
            }
        }
        container.requestLayout();
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
        notifyDataSetChanged();
    }

    public void setListFragment(Fragment listFragment) {
        fragments.add(listFragment);
        notifyDataSetChanged();
    }

    public void setDetailFragment(Fragment detailFragment) {
        fragments.add(detailFragment);
        notifyDataSetChanged();
    }

    public void clean() {
        this.fragments.clear();
        notifyDataSetChanged();
    }

    public boolean isListPageInitiated() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof ListFragment) {
                return true;
            }
        }
        return false;
    }

    public boolean isDetailPageInitiated() {
        for (Fragment fragment : fragments) {
            if (fragment instanceof DetailsFragment) {
                return true;
            }
        }
        return false;
    }
}
