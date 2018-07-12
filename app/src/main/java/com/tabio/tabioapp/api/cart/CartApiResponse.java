package com.tabio.tabioapp.api.cart;

import com.tabio.tabioapp.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/12/16.
 */
public class CartApiResponse {
    public static final String TAG = makeLogTag(CartApiResponse.class);

    private CartApiError error;
    private CartApiRequest request;
    private CartApiParams params;
    private JSONObject body;

    public CartApiResponse(CartApiRequest request, JSONObject body, CartApiParams params) {
        this.request = request;
        this.body = body;
        this.params = params;
        if (this.body != null) {
            try {
                LOGD(TAG,
                        "URL:"+ params.getUrl()+"\n"+
                                "PARAMS:"+params.getProductsJson().toString(4)+"\n"+
                                "commmand:"+params.getCommand()+"\n"+
                                "RESPONSE:"+body.toString(4));
            } catch (JSONException e) {
                LOGE(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        CartApiError error = new CartApiError(this.body);
        this.error = error;
    }

    public int getProductsCount() {
        try {
            int result = 0;
            JSONArray jproducts = this.body.getJSONArray("product");
            for (int i=0; i<jproducts.length(); i++) {
                JSONObject jproduct = jproducts.getJSONObject(i);
                result += jproduct.getInt("quantity");
            }
            LOGD(TAG, "cartresult:"+result);
            return result;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
        return 0;
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

    public CartApiRequest getRequest() {
        return request;
    }

    public CartApiParams getParams() {
        return params;
    }

    public CartApiError getError() {
        return error;
    }
}
