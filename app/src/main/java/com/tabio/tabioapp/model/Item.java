package com.tabio.tabioapp.model;

import android.content.Context;

import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/18/15.
 */
public class Item extends Product implements Serializable {
    private static final String TAG = makeLogTag(Item.class);

    private String itemId; //品番
    private int price;
    private String description;
    private String size;
    private String material;
    private String brand = "";
    private String pictureImgUrl;
    private int reviewCount;
    private boolean didReview;
    private Asset asset;
    private List<Review> reviews;
    private List<Magazine> postHistories;
    private String fromDate;

    private ItemManager manager;

    public Item() {
        super();
    }

    public Item(JSONObject json) {
        this.postHistories = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.getBaseManager().save(json);
        this.getManager().save(json);
        this.asset = new Asset(json);
    }

    public ItemManager getManager() {
        return new ItemManager(this);
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public List<Review> getReviews() {
        if (reviews == null) {
            return Arrays.asList();
        }
        return reviews;
    }

    public boolean hasReviews() {
        return getReviews().size() > 0;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<Magazine> getPostHistories() {
        if (postHistories == null) {
            return Arrays.asList();
        }
        return postHistories;
    }

    public boolean hasPostHistories() {
        return getPostHistories().size() > 0;
    }

    public void setPostHistories(List<Magazine> postHistories) {
        this.postHistories = postHistories;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getShareUrl() {
        return "http://www.tabio.com/jp/detail/"+getItemId()+"/";
    }

    public boolean didReview() {
        return didReview;
    }

    public void setDidReview(boolean didReview) {
        this.didReview = didReview;
    }

    public String getPictureImgUrl() {
        if (pictureImgUrl == null && getAsset().getMainImgUrls().size() > 0) {
            return ImageUtils.convertHttpsUrl(getAsset().getMainImgUrls().get(0));
        }
        return ImageUtils.convertHttpsUrl(pictureImgUrl);
    }

    public void setPictureImgUrl(String pictureImgUrl) {
        this.pictureImgUrl = pictureImgUrl;
    }
}
