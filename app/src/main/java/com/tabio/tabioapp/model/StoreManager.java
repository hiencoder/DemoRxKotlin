package com.tabio.tabioapp.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/25/16.
 */
public class StoreManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(StoreManager.class);

    private Store self;

    public StoreManager(Store store) {
        this.self = store;
    }

    public boolean store(JSONObject json) {
        if (json == null) {
            return false;
        }
        try {
//            LOGD(TAG, "ストア:"+json.toString(4));

            if (isSafe(json, "store_id")) self.setStoreId(String.valueOf(json.getInt("store_id")));
            if (isSafe(json, "store_code")) self.setCode(json.getString("store_code"));
            if (isSafe(json, "store_name")) self.setName(json.getString("store_name"));
            if (isSafe(json, "brand")) self.setBrand(json.getString("brand"));
            if (isSafe(json, "latitude")) self.setLatitude(json.getDouble("latitude"));
            if (isSafe(json, "longitude")) self.setLongitude(json.getDouble("longitude"));
            if (isSafe(json, "open_time")) self.setOpenTimeOfDay(json.getString("open_time"));
            if (isSafe(json, "close_time")) self.setCloseTimeOfDay(json.getString("close_time"));
            if (isSafe(json, "operation_date")) self.setOperationDate(json.getString("operation_date"));
            if (isSafe(json, "holiday_open_time"))
                self.setOpenTimeOfHoliday(json.getString("holiday_open_time"));
            if (isSafe(json, "holiday_close_time"))
                self.setCloseTimeOfHoliday(json.getString("holiday_close_time"));
            if (isSafe(json, "good")) self.setFavorite(json.getInt("good") == 1 ? true : false);
            if (isSafe(json, "distance")) self.setDistance(json.getString("distance"));
            if (isSafe(json, "good")) self.setFavorite(json.getInt("good")==1?true:false);
            if (isSafe(json, "tel")) self.setTel(json.getString("tel"));
            if (isSafe(json, "addr")) self.setAddress(json.getString("addr"));
            if (isSafe(json, "access_way")) self.setAccess(json.getString("access_way"));
            if (isSafe(json, "zip")) self.setZip(json.getString("zip"));
            if (isSafe(json, "rss_url")) self.setRssUrl(json.getString("rss_url"));
            if (isSafe(json, "date")) self.setCheckinDate(json.getString("date"));

            if (isSafe(json, "service")) {
                JSONArray jservices = json.getJSONArray("service");
                if (self.getServices() == null) self.setServices(new ArrayList<String>());
                for (int i=0; i<jservices.length(); i++) {
                    JSONObject obj = jservices.getJSONObject(i);
                    self.getServices().add(obj.getString("class"));
                }
            }

            Date nowDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String now = sdf.format(nowDate);
            self.setUpdatedAt(now);

            if (isSafe(json, "status") && isSafe(json, "color")
                    && isSafe(json, "class_id")) {
                Stock stock = new Stock(json);
                self.setStock(stock);
            }
            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean save(JSONObject json) {
        boolean results = store(json);
        if (results) {
            if (self.isExists(Store.KEYS.STORE_ID, new String[]{self.getStoreId()})) {
                return self.update(Store.KEYS.STORE_ID, new String[]{self.getStoreId()}, self.getContentValues());
            } else {
                self.insert(self.getContentValues());
            }
        }
        return results;
    }

}
