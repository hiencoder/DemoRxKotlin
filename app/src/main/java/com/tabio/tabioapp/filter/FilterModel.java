package com.tabio.tabioapp.filter;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 2/2/16.
 */
public class FilterModel implements Serializable {
    public static final String TAG = makeLogTag(FilterModel.class);

    private String displayName;
    private String key;
    private String value;
    private boolean select = false;
    private boolean showResource = false;
    private int resourceId;

    public FilterModel(String key, String displayName, boolean select) {
        this.key = key;
        this.displayName = displayName;
        this.select = select;
    }

    public FilterModel(String key, String displayName, boolean select, int resourceId) {
        this.displayName = displayName;
        this.key = key;
        this.select = select;
        this.showResource = true;
        this.resourceId = resourceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        setSelect(this.value.length()>0);
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public static List<Object> getSelectedKeys(List<FilterModel> filterModels) {
        List<Object> selectedKeyList = new ArrayList<>();
        for (int i=0; i<filterModels.size(); i++) {
            FilterModel model = filterModels.get(i);
            if (model.isSelect()) {
                selectedKeyList.add(model.getKey());
            }
        }
//        return selectedKeyList.toArray(new String[selectedKeyList.size()]);
        return selectedKeyList;
    }

    public boolean isShowResource() {
        return showResource;
    }

    public void setShowResource(boolean showResource) {
        this.showResource = showResource;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}
