package com.tabio.tabioapp.model;

import android.support.annotation.IntDef;

import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/19/15.
 */
public class Lineup implements Serializable {
    private static final String TAG = makeLogTag(Lineup.class);

    public static final int SALE = 1;
    public static final int CAN_RESTOCK_REQUEST = 2;
    public static final int SOLD_OUT = 3;
    @IntDef ({SALE, CAN_RESTOCK_REQUEST, SOLD_OUT})
    public @interface SaleStatus{}

    private int classId;
    private String code = "";
    private String name = "";
    private String chipImgUrl;
    private String imgUrl;
    private int price;
    private int status;
    private boolean restocked;
    private List<Store> stockHasStores;
    private boolean main;
    private boolean selected;

    public Lineup(JSONObject json) {
        this.stockHasStores = new ArrayList<>();
        getManager().save(json);
    }

    public LineupManager getManager() {
        return new LineupManager(this);
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChipImgUrl() {
        return ImageUtils.convertHttpsUrl(chipImgUrl);
    }

    public void setChipImgUrl(String chipImgUrl) {
        this.chipImgUrl = chipImgUrl;
    }

    public String getImgUrl() {
        return ImageUtils.convertHttpsUrl(imgUrl);
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getPrice() {
        return price;
    }

    public String getPriceWithYen() {
        return String.format("¥%1$,3d", getPrice());
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @SaleStatus
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Store> getStockHasStores() {
        if (stockHasStores == null) {
            return Arrays.asList();
        }
        return stockHasStores;
    }

    public void setStockHasStores(List<Store> stockHasStores) {
        this.stockHasStores = stockHasStores;
    }

    public boolean isRestocked() {
        return restocked;
    }

    public void setRestocked(boolean restocked) {
        this.restocked = restocked;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean hasStock() {
        return getStockHasStores().size() > 0;
    }
}
