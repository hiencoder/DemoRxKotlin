package com.tabio.tabioapp.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/8/16.
 */
public class LineupManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(LineupManager.class);

    private Lineup self;

    public LineupManager(Lineup lineup) {
        this.self = lineup;
    }

    @Override
    public boolean save(JSONObject json) {
        if (json == null) {
            return false;
        }
        try {
            if (isSafe(json, "class_id")) self.setClassId(json.getInt("class_id"));
            if (isSafe(json, "code")) self.setCode(json.getString("code"));
            if (isSafe(json, "name")) self.setName(json.getString("name"));
            if (isSafe(json, "chip_picture")) self.setChipImgUrl(json.getString("chip_picture"));
            if (isSafe(json, "color_picture")) self.setImgUrl(json.getString("color_picture"));
            if (isSafe(json, "price")) self.setPrice(json.getInt("price"));
            if (isSafe(json, "status")) self.setStatus(json.getInt("status"));
            if (isSafe(json, "restock_request_flg")) self.setRestocked(json.getInt("restock_request_flg")==1?true:false);
            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
