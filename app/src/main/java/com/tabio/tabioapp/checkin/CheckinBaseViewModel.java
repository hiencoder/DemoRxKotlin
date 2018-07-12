package com.tabio.tabioapp.checkin;

import com.tabio.tabioapp.model.Store;

/**
 * Created by san on 3/25/16.
 */
public class CheckinBaseViewModel {

    private int type;
    private String title;
    private Store store;

    public static final int VIEW_TYPE_VIEW_MAP = 0;
    public static final int VIEW_TYPE_TITLE_CAN_CHECKIN = 1;
    public static final int VIEW_TYPE_VIEW_CHECKINABLE_STORE = 2;
    public static final int VIEW_TYPE_VIEW_CHECKIN_HISTORIES = 3;
    public static final int VIEW_TYPE_TITLE_NEARBY_STORES = 4;
    public static final int VIEW_TYPE_VIEW_NEARBY_STORE = 5;
    public static final int VIEW_TYPE_VIEW_NEARBY_STORES = 6;
    public static final int VIEW_TYPE_VIEW_GRAY_BG = 7;
    public static final int VIEW_TYPE_VIEW_NO_CHECKINABLE_STORE = 8;

    public CheckinBaseViewModel(int type) {
        setType(type);
    }

    public CheckinBaseViewModel(int type, Store store) {
        this(type);
        setStore(store);
    }

    public CheckinBaseViewModel(int type, String title) {
        this(type);
        setTitle(title);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
