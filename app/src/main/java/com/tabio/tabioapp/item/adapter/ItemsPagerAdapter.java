package com.tabio.tabioapp.item.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tabio.tabioapp.item.ItemFragment;
import com.tabio.tabioapp.item.ItemsFragment;
import com.tabio.tabioapp.model.Item;

import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 12/11/15.
 */
public class ItemsPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = makeLogTag(ItemsPagerAdapter.class);

    private int[] productIds;
    private String jan;
    private String classId;
    private ItemFragment.OnItemFragmentCallbacks itemFragmentCallbacks;

    public ItemsPagerAdapter(FragmentManager fm, int[] productIds, ItemFragment.OnItemFragmentCallbacks itemFragmentCallbacks) {
        super(fm);
        this.productIds = productIds;
        this.itemFragmentCallbacks = itemFragmentCallbacks;
    }

    public ItemsPagerAdapter(FragmentManager fm, String jan, ItemFragment.OnItemFragmentCallbacks itemFragmentCallbacks) {
        super(fm);
        this.jan = jan;
        this.itemFragmentCallbacks = itemFragmentCallbacks;
    }

//    public ItemsPagerAdapter(FragmentManager fm, String classId, ItemFragment.OnItemFragmentCallbacks itemFragmentCallbacks) {
//        super(fm);
//        this.classId = classId;
//        this.itemFragmentCallbacks = itemFragmentCallbacks;
//    }

    @Override
    public Fragment getItem(int position) {
        if (productIds == null) {
            return ItemFragment.newInstance(this.jan, this.itemFragmentCallbacks);
        } else {
            return ItemFragment.newInstance(this.productIds[position], this.itemFragmentCallbacks);
        }
    }

    public int[] getProductIds() {
        return this.productIds;
    }

    @Override
    public int getCount() {
        return this.productIds == null ? 1 : this.productIds.length;
    }
}
