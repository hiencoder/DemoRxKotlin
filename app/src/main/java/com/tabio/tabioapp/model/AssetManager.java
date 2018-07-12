package com.tabio.tabioapp.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/8/16.
 */
public class AssetManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(AssetManager.class);

    private Asset self;

    public AssetManager(Asset asset) {
        this.self = asset;
    }

    @Override
    public boolean save(JSONObject json) {
        if (json == null) {
            return false;
        }
        if (isSafe(json, "main_picture")) {
            try {
                JSONArray jMainPictures = json.getJSONArray("main_picture");
                for (int i=0; i<jMainPictures.length(); i++) {
                    JSONObject picture = jMainPictures.getJSONObject(i);

                    if (isSafe(picture, "picture")) {
                        self.getMainImgUrls().add(picture.getString("picture"));
                    }
                }
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
            }

        }

        if (isSafe(json, "color")) {
            try {
                JSONArray jcolors = json.getJSONArray("color");
                for (int i = 0; i < jcolors.length(); i++) {
                    Lineup lineup = new Lineup(jcolors.getJSONObject(i));
                    if (i==0) {
                        lineup.setSelected(true);// デフォルトは1番最初のラインナップ
                    }
                    self.getLineups().add(lineup);
                }
                return true;
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }
}
