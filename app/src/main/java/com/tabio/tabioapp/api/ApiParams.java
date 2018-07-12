package com.tabio.tabioapp.api;

import com.tabio.tabioapp.model.Me;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/20/16.
 */
public class ApiParams extends HashMap<String, Object> implements Serializable {
    public static final String TAG = makeLogTag(ApiParams.class);

    private Me self;
    private String url;

    public ApiParams() {
    }

    public ApiParams(Me self, HashMap<String, Object> params, String url) {
        this.self = self;
        this.url = url;
        this.putAll(params);
    }

    public ApiParams(Me self, boolean needDefaultParams, boolean needLocationParams, String url) {
        initialize(self, needDefaultParams, needLocationParams, url);
    }

    public ApiParams(Me self, boolean needDefaultParams, String url) {
        initialize(self, needDefaultParams, false, url);
    }

    private void initialize(Me self, boolean needDefaultParams, boolean needLocationParams, String url) {
        this.self = self;
        this.url = url;
        if (needDefaultParams) {
            this.put("tabio_id", self.getTabioId());
            this.put("token", self.getToken());
        }
        if (needLocationParams) {
            this.put("latitude", self.getLatitude());
            this.put("longitude", self.getLongitude());
        }
        this.put("language", self.getLanguage());
    }

    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Object> e : this.entrySet()) {
                if (e.getValue() instanceof HashMap) {

                    HashMap<String, Object> listDict = (HashMap<String, Object>)e.getValue();
                    JSONArray array = new JSONArray();
                    for (Map.Entry<String, Object> e2 : listDict.entrySet()) {
                        // 1個しかないはず
                        String key = e2.getKey();
                        List<Object> list = (List<Object>) e2.getValue();
                        for (Object id : list) {
                            JSONObject obj = new JSONObject();
                            obj.put(key, id);
                            array.put(obj);
                        }
                    }
                    json.put(e.getKey(), array);
                } else {
//                    LOGD(TAG, "KEY:" + e.getKey() + " VALUE:" + e.getValue());
                    json.put(e.getKey(), e.getValue());
                }
            }
//            LOGD(TAG, this.url+":"+json.toString(4));
        } catch (JSONException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            return json;
        }

    }

    public Me getSelf() {
        return self;
    }

    public String getUrl() {
        return url;
    }
}
