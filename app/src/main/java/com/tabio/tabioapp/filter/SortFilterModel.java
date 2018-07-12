package com.tabio.tabioapp.filter;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/26/16.
 */
public class SortFilterModel implements Serializable {
    public static final String TAG = makeLogTag(SortFilterModel.class);

    @Retention(RetentionPolicy.SOURCE)
    public @interface SortType{}
    public static final int SORT_TYPE_NEW = 1;
    public static final int SORT_TYPE_POPULAR = 2;
    public static final int SORT_TYPE_PRICE_ASCENDING = 3;
    public static final int SORT_TYPE_PRICE_DESCENDING = 4;

    private @SortType int selectedSortType;

    public SortFilterModel() {
        this.selectedSortType = SORT_TYPE_NEW;
    }

    @SortType
    public int getSelectedSortType() {
        return selectedSortType;
    }

    public void setSelectedSortType(int selectedSortType) {
        this.selectedSortType = selectedSortType;
    }

    public int getSortListCount() {
        return 4;
    }
}
