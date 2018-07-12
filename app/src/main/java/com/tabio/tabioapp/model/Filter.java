package com.tabio.tabioapp.model;

import android.content.Context;
import android.support.annotation.IntDef;

import com.tabio.tabioapp.filter.FilterModel;
import com.tabio.tabioapp.filter.SortFilterModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 2/2/16.
 */
public class Filter implements Serializable {
    public static final String TAG = makeLogTag(Filter.class);

    public static final int FILTER_TYPE_STORE = 0;
    public static final int FILTER_TYPE_ITEM = 1;
    @IntDef({FILTER_TYPE_STORE, FILTER_TYPE_ITEM})
    public @interface FilterType{}

    private int filterType;
    public String[] titles;
    public String[] listTitles;

    public List<List<FilterModel>> filterModelsList;
    public SortFilterModel sortFilterModel;

    public Filter(@FilterType int searchType) {
        this.filterType = searchType;
        this.sortFilterModel = new SortFilterModel();
        this.filterModelsList = new ArrayList<>();
    }

    public SortFilterModel getSortFilterModel() {
        return sortFilterModel;
    }

    public void setSortFilterModel(SortFilterModel sortFilterModel) {
        this.sortFilterModel = sortFilterModel;
    }

    @FilterType
    public int getFilterType() {
        return filterType;
    }

    @SuppressWarnings("SearchType")
    public void setFilterType(@FilterType int filterType) {
        this.filterType = filterType;
    }

    public String[] getTitles() {
        return titles;
    }

    public void setTitles(String[] titles) {
        this.titles = titles;
    }

    public String[] getListTitles() {
        return listTitles;
    }

    public void setListTitles(String[] listTitles) {
        this.listTitles = listTitles;
    }

    public List<List<FilterModel>> getFilterModelsList() {
        return filterModelsList;
    }

    public void setFilterModelsList(List<List<FilterModel>> filterModelsList) {
        this.filterModelsList = filterModelsList;
    }

    public boolean isFiltering() {
        for (int i=0; i<filterModelsList.size(); i++) {
            List<FilterModel> filterModels = getFilterModelsList().get(i);
            List<Object> selectedKeys = FilterModel.getSelectedKeys(filterModels);
            if (selectedKeys.size() > 0) {
                return true;
            }

        }
        return false;
    }
}
