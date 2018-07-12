package com.tabio.tabioapp.model;


import android.support.annotation.IntDef;

import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/19/15.
 */
public class Stock implements Serializable {
    private static final String TAG = makeLogTag(Stock.class);

    public static final int OUT_OF_STOCK = 0;
    public static final int HAS_STOCK = 1;
    public static final int FEW = 2;
    @IntDef ({OUT_OF_STOCK, HAS_STOCK, FEW})
    public @interface StockStatus{}

    private String colorCode; // カラーコード
    private int classId; // 商品規格ID
    private int status; // 0:在庫なし,1:在庫あり,2:残り僅か

    public Stock(JSONObject json) {
        getManager().save(json);
    }

    public StockManager getManager() {
        return new StockManager(this);
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    @StockStatus
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
}
