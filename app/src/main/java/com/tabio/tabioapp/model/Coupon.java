package com.tabio.tabioapp.model;

import android.content.Context;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.coupon.CouponManager;
import com.tabio.tabioapp.util.DateUtils;
import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/18/15.
 */
public class Coupon extends AppModel implements Serializable {
    public static final String TAG = makeLogTag(Coupon.class);

    public static final int ONLY_USE_AT_ONLINE = 1;
    public static final int ONLY_USE_AT_STORE = 2;

    private String couponId;
    private String type;
    private String name;
    private String imgUrl;
    private String code;
    private String barcodeImgUrl;
    private String startDate;
    private String endDate;
    private Integer use;
    private String stores;
    private String comment;

    private Context context;
    private CouponManager manager;

    public Coupon(Context context) {
        this.context = context;
    }

    public Coupon(Context context, JSONObject json) {
        this.context = context;
        this.getManager().save(json);
    }

    public CouponManager getManager() {
        return new CouponManager(this.context, this);
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getType() {
        int sid = 0;
        if (this.type.equals("0")) {
            sid = R.string.text_coupon_type_all;
        } else if (this.type.equals("1")) {
            sid = R.string.text_coupon_type_online;
        } else {
            sid = R.string.text_coupon_type_shop;
        }
        return this.context.getString(sid)+this.context.getString(R.string.text_coupon_title);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return ImageUtils.convertHttpsUrl(imgUrl);
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBarcodeImgUrl() {
        return ImageUtils.convertHttpsUrl(barcodeImgUrl);
    }

    public void setBarcodeImgUrl(String barcodeImgUrl) {
        this.barcodeImgUrl = barcodeImgUrl;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDate(String lang) {
        try {
            if (getStartDate().equals("") && getEndDate().equals("")) {
                return "";
            }
            String format = "yyyy/MM/dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date startDate = sdf.parse(getStartDate());
            Date endDate = sdf.parse(getEndDate());

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(startDate);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(endDate);

            String startDateStr = DateUtils.getDateFromFormat2(this.context, format, getStartDate(), lang)+"("+DateUtils.getWeekday(this.context, cal1)+")";

            String endDateStr = DateUtils.getDateFromFormat2(this.context, format, getEndDate(), lang)+"("+DateUtils.getWeekday(this.context, cal2)+")";
            return startDateStr + "〜" + endDateStr;

        } catch (ParseException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
            return "";
        }
    }

    public Integer getUse() {
        return use;
    }

    public void setUse(Integer use) {
        this.use = use;
    }

    public String getStores() {
        return stores;
    }

    public void setStores(String stores) {
        this.stores = stores;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


}
