package com.tabio.tabioapp.model;


import com.tabio.tabioapp.util.DateUtils;
import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/19/15.
 */
public class Review extends AppModel implements Serializable {
    private static final String TAG = makeLogTag(Review.class);

    private int itemProductId;
    private String itemName;
    private String itemImgUrl;

    private int reviewId;
    private boolean status;
    private String reviewerNickname;
    private String reviewerIconImgUrl;
    private String date;
    private String comment;

    public Review(JSONObject json) {
        getManager().save(json);
    }

    public ReviewManager getManager() {
        return new ReviewManager(this);
    }

    public int getItemProductId() {
        return itemProductId;
    }

    public void setItemProductId(int itemProductId) {
        this.itemProductId = itemProductId;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public boolean isConfirmed() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemImgUrl() {
        return ImageUtils.convertHttpsUrl(itemImgUrl);
    }

    public void setItemImgUrl(String itemImgUrl) {
        this.itemImgUrl = itemImgUrl;
    }

    public String getReviewerIconImgUrl() {
        return ImageUtils.convertHttpsUrl(reviewerIconImgUrl);
    }

    public void setReviewerIconImgUrl(String reviewerIconImgUrl) {
        this.reviewerIconImgUrl = reviewerIconImgUrl;
    }

    public String getReviewerNickname() {
        return reviewerNickname;
    }

    public void setReviewerNickname(String reviewerNickname) {
        this.reviewerNickname = reviewerNickname;
    }

    public String getDate() {
        return date;
    }

    public String getDateDisplay(String lang) {
        return DateUtils.getDateFromFormat("yyyy/MM/dd HH:mm:SS", getDate(), lang);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
