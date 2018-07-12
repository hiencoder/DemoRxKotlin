package com.tabio.tabioapp.model;

import android.content.Context;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/18/15.
 */
public abstract class Product extends AppModel implements Serializable {
    private static final String TAG = makeLogTag(Product.class);

    private String jan;//janコード
    private int productId;//商品ID
    private String name = "";//商品名
    private String classId;//商品規格ID
    private boolean isFavorite = false;//お気に入り
    private int favoriteCount = 0;//お気に入り数

    private ProductManager manager;

    public Product() {
    }

    public ProductManager getBaseManager() {
        return new ProductManager(this);
    }

    public String getJan() {
        return jan;
    }

    public void setJan(String jan) {
        this.jan = jan;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
}
