package com.tabio.tabioapp.api.cart;

import com.tabio.tabioapp.api.ApiError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/12/16.
 */
public class CartApiError extends ApiError {
    public static final String TAG = makeLogTag(CartApiError.class);

    public CartApiError(JSONObject json) {
        super(json);
        if (json == null) {
            setError(true);
            setCode(UNDEFINED);
            return;
        }
        if (json.has("result")) {
            try {
                JSONObject jerror = json.getJSONObject("result");
                if (!jerror.getString("result_code").equals("200")) {
                    setError(true);
                    setCode(Integer.valueOf(jerror.getString("result_code")));
                    setMessage(jerror.getString("error_message"));
                }
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
