package com.tabio.tabioapp.model;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/24/16.
 */
public class ProductManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(ProductManager.class);

    private Product self;

    public ProductManager(Product product) {
        this.self = product;
    }

    @Override
    public boolean save(JSONObject json) {
        try {
            if (isSafe(json, "id")) self.setProductId(json.getInt("id"));
            if (isSafe(json, "jan")) self.setJan(json.getString("jan"));
            if (isSafe(json, "product")) self.setProductId(json.getInt("product"));
            if (isSafe(json, "product_name")) self.setName(json.getString("product_name"));
            if (isSafe(json, "product_class")) self.setClassId(json.getString("product_class"));
            if (isSafe(json, "good")) self.setFavorite(json.getInt("good")==1?true:false);
            if (isSafe(json, "count")) self.setFavoriteCount(json.getInt("count"));
            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
