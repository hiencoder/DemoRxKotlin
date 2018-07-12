package com.tabio.tabioapp.api;

import android.content.Intent;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.model.Me;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/20/16.
 */
public class ApiResponse {
    public static final String TAG = makeLogTag(ApiResponse.class);

    private ApiError error;
    private ApiRequest request;
    private ApiParams params;
    private JSONObject body;

    public ApiResponse(ApiRequest request, JSONObject body, ApiParams params) {
        this.request = request;
        this.body = body;
        this.params = params;
        if (this.body != null) {
            try {
                LOGD(TAG,
                        "URL:" + BuildConfig.BASE_URL + params.getUrl() + "\n" +
                                "PARAMS:" + params.getJson().toString(4) + "\n" +
                                "RESPONSE:" + body.toString(4));
            } catch (JSONException e) {
                LOGE(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        ApiError error = new ApiError(this.body);
        this.error = error;

        try {
            if (error.getCode() == ApiError.CODE_7 || error.getCode() == ApiError.CODE_6) {
                Me self = AppController.getInstance().getSelf(false);
                self.setStatus(error.getCode() == ApiError.CODE_7 ? Me.LEAVED : Me.SUSPENDED);
                self.getManager().save();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGE(TAG, e.getMessage());
        }

    }

    public void setError(ApiError error) {
        this.error = error;
    }

    public int getErrorCode() {
        return error.getCode();
    }

    public ApiError getError() {
        return error;
    }

    public boolean hasError() {
        return this.error.isError();
    }

    public String getErrorMessage() {
        return this.error.getMessage();
    }

    public JSONObject getBody() {
        return body;
    }

    public ApiRequest getRequest() {
        return request;
    }

    public ApiParams getParams() {
        return params;
    }
}
