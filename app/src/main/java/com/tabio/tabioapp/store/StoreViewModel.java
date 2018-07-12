package com.tabio.tabioapp.store;

import android.support.annotation.IntDef;

/**
 * Created by san on 4/6/16.
 */
public class StoreViewModel {
    public static final int VIEW_TYPE_MAP = 0;
    public static final int VIEW_TYPE_BASIC_INFO = 1;
    public static final int VIEW_TYPE_TYPES = 2;
    public static final int VIEW_TYPE_ACTIONS = 3;
    public static final int VIEW_TYPE_ACCESS = 4;
    public static final int VIEW_TYPE_COORDINATE_TITLE = 5;
    public static final int VIEW_TYPE_COORDINATE = 6;
    public static final int VIEW_TYPE_COORDINATE_MORE_BUTTON = 7;
    public static final int VIEW_TYPE_BLOG_TITLE = 8;
    public static final int VIEW_TYPE_BLOG = 9;
    public static final int VIEW_TYPE_BLOG_MORE_BUTTON = 10;
    public static final int VIEW_TYPE_NODATA = 11;

//    @IntDef({VIEW_TYPE_MAP,VIEW_TYPE_BASIC_INFO,VIEW_TYPE_TYPES,VIEW_TYPE_ACTIONS,
//            VIEW_TYPE_ACCESS,VIEW_TYPE_COORDINATE,VIEW_TYPE_COORDINATE_MORE_BUTTON,
//            VIEW_TYPE_COORDINATE_TITLE, VIEW_TYPE_BLOG_TITLE,VIEW_TYPE_BLOG,
//            VIEW_TYPE_BLOG_MORE_BUTTON})
//    public @interface ViewType{}

    private int viewType;
    private String text;

    public StoreViewModel(int viewType) {
        this.viewType = viewType;
    }

    public StoreViewModel(int viewType, String text) {
        this.viewType = viewType;
        this.text = text;
    }

    public int getViewType() {
        return viewType;
    }

    public String getText() {
        return text;
    }
}
