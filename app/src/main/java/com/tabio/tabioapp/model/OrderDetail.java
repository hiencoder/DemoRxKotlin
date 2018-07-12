package com.tabio.tabioapp.model;


import com.tabio.tabioapp.order.OrderDetailManager;
import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/16/15.
 */
public class OrderDetail implements Serializable {
    private static final String TAG = makeLogTag(OrderDetail.class);

    private int purchaseDetailId;
    private boolean favorite;
    private int favoriteCount;
    private boolean postReview;
    private String name = "";
    private int productId;
    private int classId;
    private int price;
    private int quantity;
    private String itemId;
    private String colorCode = "";
    private String pictureUrl;
    private String colorName = "";
    private String size;

    public OrderDetail(JSONObject json) {
        getManager().save(json);
    }

    public OrderDetailManager getManager() {
        return new OrderDetailManager(this);
    }

    public int getPurchaseDetailId() {
        return purchaseDetailId;
    }

    public void setPurchaseDetailId(int purchaseDetailId) {
        this.purchaseDetailId = purchaseDetailId;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public boolean isPostReview() {
        return postReview;
    }

    public void setPostReview(boolean postReview) {
        this.postReview = postReview;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getPictureUrl() {
        return ImageUtils.convertHttpsUrl(pictureUrl);
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
