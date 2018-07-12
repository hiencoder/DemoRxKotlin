package com.tabio.tabioapp.api.decide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/4/16.
 */
public class DecideApiError {
    public static final String TAG = makeLogTag(DecideApiError.class);

    private boolean error = false;
        private int code;
    private String message;

    public DecideApiError(JSONObject json) {
        if (json == null) {
            this.error = true;
            this.message = "undefined error";
            return;
        }

        if (json.has("error")) {
            try {
                JSONArray errors = json.getJSONArray("errors");
                if (errors.length() > 0) {
                    this.error = true;
                    JSONObject errorJson = errors.getJSONObject(0);
                    this.message = errorJson.getString("message");
                    this.code = errorJson.getInt("code");
                } else {
                    this.error = false;
                }
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
                this.error = true;
                this.message = "undefined error";
            }

        }
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
