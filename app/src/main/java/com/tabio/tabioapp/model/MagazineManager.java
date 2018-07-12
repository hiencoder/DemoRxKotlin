package com.tabio.tabioapp.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/11/16.
 */
public class MagazineManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(MagazineManager.class);

    private Magazine self;

    public MagazineManager(Magazine magazine) {
        this.self = magazine;
    }

    @Override
    public boolean save(JSONObject json) {
        if (json == null) {
            return false;
        }
        try {
            if (isSafe(json, "id")) self.setMagazineId(json.getInt("id"));
            if (isSafe(json, "name")) self.setName(json.getString("name"));
            if (isSafe(json, "issue")) self.setIssue(json.getString("issue"));
            if (isSafe(json, "sold_date")) self.setPubDate(json.getString("sold_date"));
            if (isSafe(json, "page")) self.setPage(json.getInt("page"));
            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
