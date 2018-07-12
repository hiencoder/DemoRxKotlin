package com.tabio.tabioapp.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/20/16.
 */
public abstract class ModelManager implements Serializable {
    public static final String TAG = makeLogTag(ModelManager.class);

    protected boolean isSafe(JSONObject json, String key) {
        if (!json.has(key)) {
            return false;
        }
        if (json.isNull(key)) {
            return false;
        }
        try {
            if (json.get(key).equals("")) {
                return false;
            }
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }

//        try {
//            LOGD(TAG, "cleaned key:"+key+" value:"+json.get(key));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return true;
    }

    public abstract boolean save(JSONObject json);
}
