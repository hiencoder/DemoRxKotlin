package com.tabio.tabioapp.api.decide;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/4/16.
 */
public class DecideApiParams extends HashMap<String, Object> {
    public static final String TAG = makeLogTag(DecideApiParams.class);

    private String url;

    public DecideApiParams(String url) {
        this.url = url;
    }

    public String addQueryStrings(String url) {
        if (this.size() > 0) {
            url += "?";
            for (Map.Entry entry : this.entrySet()) {
                url += entry.getKey();
                url += "=";
                url += entry.getValue();
                url += "&";
//                this.remove(entry.getKey());
            }
        }
        return url;
    }

    public JSONObject getPostParams() {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Object> e : this.entrySet()) {

                /*
                 notificationIdを指定するときは、
                 HashMap<String, Object> map = new HashMap<>();
                 map.put("app", notificationId);
                  */
                if (e.getKey().equals("app")) {
                    JSONObject notificationId = new JSONObject();
                    notificationId.put("notification_id", e.getValue());
                    json.put("app", notificationId);
                } else {
                    json.put(e.getKey(), e.getValue());
                }
            }
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
        return json;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
