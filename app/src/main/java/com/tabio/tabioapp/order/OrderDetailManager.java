package com.tabio.tabioapp.order;

import com.tabio.tabioapp.model.ModelManager;
import com.tabio.tabioapp.model.OrderDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/25/16.
 */
public class OrderDetailManager extends ModelManager {
    public static final String TAG = makeLogTag(OrderDetailManager.class);

    private OrderDetail self;

    public OrderDetailManager(OrderDetail orderDetail) {
        this.self = orderDetail;
    }

    @Override
    public boolean save(JSONObject json) {
        try {
            if (isSafe(json, "good")) self.setFavorite(json.getInt("good")==1?true:false);
            if (isSafe(json, "count")) self.setFavoriteCount(json.getInt("count"));
            if (isSafe(json, "review_flag")) self.setPostReview(json.getInt("review_flag")==1?true:false);
            if (isSafe(json, "name")) self.setName(json.getString("name"));
            if (isSafe(json, "detail_id")) self.setPurchaseDetailId(json.getInt("detail_id"));
            if (isSafe(json, "product_id")) self.setProductId(json.getInt("product_id"));
            if (isSafe(json, "product_class")) self.setClassId(json.getInt("product_class"));
            if (isSafe(json, "price")) self.setPrice(json.getInt("price"));
            if (isSafe(json, "quantity")) self.setQuantity(json.getInt("quantity"));
            if (isSafe(json, "number")) self.setItemId(json.getString("number"));
            if (isSafe(json, "color_code")) self.setColorCode(json.getString("color_code"));
            if (isSafe(json, "picture")) self.setPictureUrl(json.getString("picture"));
            if (isSafe(json, "color_name")) self.setColorName(json.getString("color_name"));
            if (isSafe(json, "size")) self.setSize(json.getString("size"));

            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
