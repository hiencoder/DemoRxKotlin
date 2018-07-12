package com.tabio.tabioapp.model;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/24/16.
 */
public class CoordinateManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(CoordinateManager.class);

    private Coordinate self;

    public CoordinateManager(Coordinate coordinate) {
        this.self = coordinate;
    }

    @Override
    public boolean save(JSONObject json) {
        try {
//            LOGD(TAG, json.toString(4));
            if (isSafe(json, "store_id"))self.setStoreId(json.getInt("store_id"));
            if (isSafe(json, "url"))self.setShareurl(json.getString("url"));
            if (isSafe(json, "id"))self.setCoordinateId(json.getInt("id"));
            if (isSafe(json, "coordinate_picture"))self.setImgUrl(json.getString("coordinate_picture"));
            if (isSafe(json, "bland"))self.setBrandName(json.getString("brand"));
            if (isSafe(json, "colorchip_picture"))self.setChipImgUrl(json.getString("colorchip_picture"));
            if (isSafe(json, "store_name"))self.setStoreName(json.getString("store_name"));
            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
