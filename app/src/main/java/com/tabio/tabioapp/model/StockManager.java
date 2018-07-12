package com.tabio.tabioapp.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/9/16.
 */
public class StockManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(StockManager.class);

    private Stock self;

    public StockManager(Stock stock) {
        this.self = stock;
    }

    @Override
    public boolean save(JSONObject json) {
        if (json == null) {
            return false;
        }
        try {
            if (isSafe(json, "class_id")) self.setClassId(json.getInt("class_id"));
            if (isSafe(json, "color")) self.setColorCode(json.getString("color"));
            if (isSafe(json, "status")) self.setStatus(json.getInt("status"));
            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
