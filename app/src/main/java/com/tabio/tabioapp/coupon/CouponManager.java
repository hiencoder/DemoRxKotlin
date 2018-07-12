package com.tabio.tabioapp.coupon;

import android.content.Context;

import com.tabio.tabioapp.model.Coupon;
import com.tabio.tabioapp.model.ModelManager;

import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/22/16.
 */
public class CouponManager extends ModelManager {
    public static final String TAG = makeLogTag(CouponManager.class);

    private Context context;
    private Coupon self;

    public CouponManager(Context context, Coupon coupon) {
        this.context = context;
        this.self = coupon;
    }


    @Override
    public boolean save(JSONObject json) {
        try {
//            LOGD(TAG, json.toString(4));
            if (isSafe(json, "coupon_id"))self.setCouponId(String.valueOf(json.getInt("coupon_id")));
            if (isSafe(json, "restriction"))self.setType(json.getString("restriction"));
            if (isSafe(json, "coupon_image"))self.setImgUrl(json.getString("coupon_image"));
            if (isSafe(json, "barcode"))self.setBarcodeImgUrl(json.getString("barcode"));
            if (isSafe(json, "title"))self.setName(json.getString("title"));
            if (isSafe(json, "start_date"))self.setStartDate(json.getString("start_date"));
            if (isSafe(json, "end_date"))self.setEndDate(json.getString("end_date"));
            if (isSafe(json, "coupon_code"))self.setCode(json.getString("coupon_code"));
            if (isSafe(json, "stores"))self.setStores(json.getString("stores"));
            if (isSafe(json, "annotation"))self.setComment(json.getString("annotation"));
            return true;

        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
