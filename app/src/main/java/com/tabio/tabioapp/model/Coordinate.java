package com.tabio.tabioapp.model;

import android.content.Context;

import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/18/15.
 */
public class Coordinate extends Product implements Serializable {
    private static final String TAG = makeLogTag(Coordinate.class);

    private int coordinateId;
    private int storeId; // 実店舗ID
    private String storeName = ""; // 店舗名
    private String brandName = ""; // ブランド
    private String chipImgUrl; // カラーチップ画像
    private String imgUrl; // コーディネート画像
    private String shareurl; // シェアURL
    private boolean judged;

    private CoordinateManager manager;

    public Coordinate() {
        super();
    }

    public Coordinate(JSONObject json) {
        this.getBaseManager().save(json);
        this.getManager().save(json);
    }

    public CoordinateManager getManager() {
        return new CoordinateManager(this);
    }

    public int getCoordinateId() {
        return coordinateId;
    }

    public void setCoordinateId(int coordinateId) {
        this.coordinateId = coordinateId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getStoreNameWithBrand() {
//        return getBrand() + " " + name;
        return getBrandName() + " " + getStoreName();
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getChipImgUrl() {
        return ImageUtils.convertHttpsUrl(chipImgUrl);
    }

    public void setChipImgUrl(String chipImgUrl) {
        this.chipImgUrl = chipImgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getShareurl() {
        return shareurl;
    }

    public void setShareurl(String shareurl) {
        this.shareurl = shareurl;
    }

    public boolean isJudged() {
        return judged;
    }

    public void setJudged(boolean judged) {
        this.judged = judged;
    }
}
