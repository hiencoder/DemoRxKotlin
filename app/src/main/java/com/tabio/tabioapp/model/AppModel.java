package com.tabio.tabioapp.model;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/19/16.
 */
public abstract class AppModel implements Serializable {
    public static final String TAG = makeLogTag(AppModel.class);

    public AppModel() {
    }
}
