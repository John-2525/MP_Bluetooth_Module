package com.example.mp_bluetooth_module.ui.main;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.mp_bluetooth_module.Image_Fragment;
import com.example.mp_bluetooth_module.R;
import com.example.mp_bluetooth_module.Video_Fragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

//TODO Research what does this thing do

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[] {R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    /**
     * The switch case is to display custom fragment layouts created
     */
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch(position) {
            case 0:
                fragment = new Image_Fragment();
                break;
            case 1:
                fragment = new Video_Fragment();
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "Images";
            case 1:
                return "Videos";
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}