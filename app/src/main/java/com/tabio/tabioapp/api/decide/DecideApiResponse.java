package com.tabio.tabioapp.api.decide;

import com.tabio.tabioapp.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/4/16.
 */
public class DecideApiResponse {
    public static final String TAG = makeLogTag(DecideApiResponse.class);

    private DecideApiError error;
    private DecideApiRequest request;
    private JSONObject body;
    private DecideApiParams params;

    public DecideApiResponse(DecideApiRequest request, JSONObject body, DecideApiParams params) {
        this.request = request;
        this.body = body;
        this.params = params;
        if (this.body != null) {
            try {
                LOGD(TAG,
                        "URL:"+ BuildConfig.DECIDE_URL + BuildConfig.DECIDE_VERSION + "/" +params.getUrl()+"\n"+
                        "PARAMS:"+params.getPostParams().toString(4)+"\n"+
                        "RESPONSE:"+body.toString(4));
            } catch (JSONException e) {
                LOGE(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        DecideApiError error = new DecideApiError(body);
        this.error = error;
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

    public DecideApiRequest getRequest() {
        return request;
    }

    public DecideApiParams getParams() {
        return params;
    }
}
