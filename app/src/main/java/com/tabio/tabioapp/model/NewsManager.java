package com.tabio.tabioapp.model;

import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/25/16.
 */
public class NewsManager extends ModelManager {
    public static final String TAG = makeLogTag(NewsManager.class);

    private News self;

    public NewsManager(News news) {
        this.self = news;
    }

    @Override
    public boolean save(JSONObject json) {
        try {
            if (isSafe(json, "class")) self.setClassId(json.getInt("class"));
            if (isSafe(json, "date")) self.setDate(json.getString("date"));
            if (isSafe(json, "message")) self.setMessage(json.getString("message"));
            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
