package com.tabio.tabioapp.order;

import com.tabio.tabioapp.model.ModelManager;
import com.tabio.tabioapp.model.Order;
import com.tabio.tabioapp.model.OrderDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/23/16.
 */
public class OrderManager extends ModelManager {
    public static final String TAG = makeLogTag(OrderManager.class);

    private Order self;

    public OrderManager(Order order) {
        this.self = order;
    }

    @Override
    public boolean save(JSONObject json) {
        try {
            if (isSafe(json, "brand")) self.setStoreBrand(json.getString("brand"));
            // distanceはいらない
            if (isSafe(json, "id")) self.setOrderId(json.getInt("id"));
            if (isSafe(json, "date")) self.setOrderedDate(json.getString("date"));
            if (isSafe(json, "store_name")) self.setStoreName(json.getString("store_name"));
            if (isSafe(json, "payment")) self.setPrice(json.getInt("payment"));
            if (isSafe(json, "status")) self.setStatus(json.getInt("status"));

            if (isSafe(json, "subtotal")) self.setSubTotal(json.getInt("subtotal"));
            if (isSafe(json, "tax")) self.setTax(json.getInt("tax"));
            if (isSafe(json, "deliv_fee")) self.setDeliverFee(json.getInt("deliv_fee"));
            if (isSafe(json, "charge")) self.setCharge(json.getInt("charge"));
            if (isSafe(json, "point_discount")) self.setPointDiscountPrice(json.getInt("point_discount"));
            if (isSafe(json, "cpn_price")) self.setCouponDiscountPrice(json.getInt("cpn_price"));
            if (isSafe(json, "add_piece")) self.setAddPiece(json.getInt("add_piece"));
            if (isSafe(json, "ec_flag")) self.setOrderedByOnlineStore(json.getInt("ec_flag")==1);

            if (isSafe(json, "purchase")) {
                JSONArray details = json.getJSONArray("purchase");
                for (int i=0; i<details.length(); i++) {
                    OrderDetail detail = new OrderDetail(details.getJSONObject(i));
                    self.getDetails().add(detail);
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
