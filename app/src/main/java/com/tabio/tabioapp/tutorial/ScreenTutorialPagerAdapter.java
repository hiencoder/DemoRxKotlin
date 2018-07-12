package com.tabio.tabioapp.tutorial;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 5/27/16.
 */
public class ScreenTutorialPagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = makeLogTag(ScreenTutorialPagerAdapter.class);

    private String[] fileNames;
//    private int[] stringResIds;

    public ScreenTutorialPagerAdapter(FragmentManager fm, String[] fileNames)  {
        super(fm);
        this.fileNames = fileNames;
//        this.stringResIds = stringResIds;
    }

    @Override
    public Fragment getItem(int position) {
        return ScreenTutorialItemFragment.newInstance(fileNames[position]);
    }

    @Override
    public int getCount() {
        return this.fileNames.length;
    }
}
