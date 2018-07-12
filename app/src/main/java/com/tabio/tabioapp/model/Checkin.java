package com.tabio.tabioapp.model;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/16/15.
 */
public class Checkin implements Serializable {
    private static final String TAG = makeLogTag(Checkin.class);

    private String storeId;
    private String storeCode;
    private String timestamp;

    public Checkin() {
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
