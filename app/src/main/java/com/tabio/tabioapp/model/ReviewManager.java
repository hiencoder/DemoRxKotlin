package com.tabio.tabioapp.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/11/16.
 */
public class ReviewManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(ReviewManager.class);

    private Review self;

    public ReviewManager(Review review) {
        this.self = review;
    }

    @Override
    public boolean save(JSONObject json) {
        try {
            if (isSafe(json, "review_id")) self.setReviewId(json.getInt("review_id"));
            if (isSafe(json, "review_status")) self.setStatus(json.getInt("review_status")==2?true:false);
            if (isSafe(json, "nickname")) self.setReviewerNickname(json.getString("nickname"));
            if (isSafe(json, "face_icon")) self.setReviewerIconImgUrl(json.getString("face_icon"));
            if (isSafe(json, "date")) self.setDate(json.getString("date"));
            if (isSafe(json, "comment")) self.setComment(json.getString("comment"));

            if (isSafe(json, "name")) self.setItemName(json.getString("name"));
            if (isSafe(json, "product")) self.setItemProductId(json.getInt("product"));
            if (isSafe(json, "main_picture")) {
                JSONArray mainPictures = json.getJSONArray("main_picture");
                for (int i=0; i<mainPictures.length(); i++) {
                    JSONObject jpic = mainPictures.getJSONObject(i);
                    if (isSafe(jpic, "picture")) {
                        self.setItemImgUrl(jpic.getString("picture"));
                        break;
                    }
                }
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            LOGE(TAG, e.getMessage());
        }
        return false;
    }
}
