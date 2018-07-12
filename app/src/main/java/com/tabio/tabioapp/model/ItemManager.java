package com.tabio.tabioapp.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/7/16.
 */
public class ItemManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(ItemManager.class);

    private Item self;

    public ItemManager(Item item) {
        this.self = item;
    }

    @Override
    public boolean save(JSONObject json) {
        try {
            if (isSafe(json, "from_date")) self.setFromDate(json.getString("from_date"));
            if (isSafe(json, "name")) self.setName(json.getString("name"));
            if (isSafe(json, "number")) self.setItemId(json.getString("number"));
            if (isSafe(json, "picture")) self.setPictureImgUrl(json.getString("picture"));
            if (isSafe(json, "material")) self.setMaterial(json.getString("material"));
            if (isSafe(json, "comment")) self.setDescription(json.getString("comment"));
            if (isSafe(json, "size")) self.setSize(json.getString("size"));
            if (isSafe(json, "price")) self.setPrice(json.getInt("price"));
            if (isSafe(json, "review_flag")) self.setDidReview(json.getInt("review_flag")==1?true:false);
            if (isSafe(json, "review")) self.setReviewCount(json.getInt("review"));

            if (isSafe(json, "history")) {
                try {
                    JSONArray jhistories = json.getJSONArray("history");
                    for (int i=0; i<jhistories.length(); i++) {
                        JSONObject jhistory = jhistories.getJSONObject(i);
                        self.getPostHistories().add(new Magazine(jhistory));
                    }
                } catch (JSONException e) {
                    LOGE(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }

            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
