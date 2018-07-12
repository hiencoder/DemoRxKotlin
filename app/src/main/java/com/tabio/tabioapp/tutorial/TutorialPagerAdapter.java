package com.tabio.tabioapp.tutorial;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/30/15.
 */
public class TutorialPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = makeLogTag(TutorialPagerAdapter.class);

    private String[] fileNames;
    private boolean showLogo;

    public TutorialPagerAdapter(FragmentManager fm, String[] fileNames, boolean showLogo) {
        super(fm);
        this.showLogo = showLogo;
        this.fileNames = fileNames;
    }

    @Override
    public Fragment getItem(int position) {
        if (showLogo && position == 0) {
            return TutorialFragment.newInstance(showLogo, null);
        }
        return TutorialFragment.newInstance(false, showLogo ? this.fileNames[position-1] : this.fileNames[position]);
    }

    @Override
    public int getCount() {
        return showLogo?4:3;
    }

}
