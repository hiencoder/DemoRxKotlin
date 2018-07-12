package com.tabio.tabioapp.api.cart;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.model.Me;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/12/16.
 */
public class CartApiParams extends HashMap<String, Object> implements Serializable {
    public static final String TAG = makeLogTag(CartApiParams.class);

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            GET_CART,
            ADD_CART
    })
    public @interface CartCommand {
    }

    public static final String GET_CART = "get_cart";
    public static final String ADD_CART = "add_cart";

    private Me self;
    private String command;

    public CartApiParams(@CartCommand String command, @Nullable Item item) {
        this.command = command;
        this.self = AppController.getInstance().getSelf(false);

        this.put("tabio_id", self.getTabioId());
        this.put("sid", self.getTabioId());
        this.put("shop_id", 1);

        if (item != null) {
            try {
                JSONObject product = new JSONObject();
                product.put("product_class_id", item.getAsset().getCurrentLineup().getClassId());
                product.put("quantity", 1);
                JSONArray products = new JSONArray();
                products.put(product);
                this.put("product", products);
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public String getUrl() {
        return BuildConfig.CART_BASE_URL+ ApiRoute.CART_CRUD;
    }

    public Me getMe() {
        return self;
    }

    public JSONObject getProductsJson() {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Object> e : this.entrySet()) {
//                if (e.getKey().equals("product")) {

//                } else {
                    json.put(e.getKey(), e.getValue());
//                }
            }
            //{"tabio_id":"7209981726944","sid":"7209981726944","shop_id":0}
//            LOGD(TAG, getUrl()+":"+json.toString(4));
        } catch (JSONException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            return json;
        }
    }
}
