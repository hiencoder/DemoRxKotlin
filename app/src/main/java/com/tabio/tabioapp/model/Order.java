package com.tabio.tabioapp.model;


import android.content.Context;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.order.OrderManager;
import com.tabio.tabioapp.util.DateUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/16/15.
 */
public class Order implements Serializable {
    private static final String TAG = makeLogTag(Order.class);

    private String storeBrand = "";
    private int orderId;
    private String orderedDate;
    private String storeName = "";
    private int price;// 合計（税込）
    private int status;

    private int subtotal; // 商品代金（税抜）＝小計
    private int tax; // 消費税金額
    private int deliverFee; // 送料
    private int charge; // 決済手数料(税込)
    private int pointDiscountPrice; // ポイント利用（税抜）
    private int couponDiscountPrice; // クーポン値引き（税抜）
    private int addPiece; // 今回付与ピース数
    private boolean orderedByOnlineStore; // オンラインストアで購入されたかどうか

    private List<OrderDetail> details;

    public Order(JSONObject json) {
        this.details = new ArrayList<>();
        getManager().save(json);
    }

    public OrderManager getManager() {
        return new OrderManager(this);
    }

    public int getSubTotal() {
        return subtotal;
    }

    public void setSubTotal(int total) {
        this.subtotal = total;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderedDate() {
        return orderedDate;
    }

    public String getOrderedDateForDisplay(String lang) {
        try {
            return DateUtils.getDateFromFormat("yyyy/MM/dd HH:mm:ss", getOrderedDate(), lang);
        } catch (Exception e) {
            return "";
        }
    }

    public void setOrderedDate(String orderedDate) {
        this.orderedDate = orderedDate;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreNameWithBrand() {
        try {
            return getStoreBrand() + " " + storeName;
        } catch (NullPointerException e) {
            return storeName;
        }
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusName(Context c) {
        //1: 購入済、 2: 注文完了、 3: 発注待ち、 4: 発注済、 5: キャンセル、 6: 返品
        switch (getStatus()) {
            case 1:
                return c.getString(R.string.text_item_order_status_1);
            case 2:
                return c.getString(R.string.text_item_order_status_2);
            case 3:
                return c.getString(R.string.text_item_order_status_3);
            case 4:
                return c.getString(R.string.text_item_order_status_4);
            case 5:
                return c.getString(R.string.text_item_order_status_5);
            case 6:
                return c.getString(R.string.text_item_order_status_6);
            default:
                return "";
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPriceWithYen() {
        return String.format("¥%1$,3d", getPrice());
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public int getDeliverFee() {
        return deliverFee;
    }

    public void setDeliverFee(int deliverFee) {
        this.deliverFee = deliverFee;
    }

    public int getPointDiscountPrice() {
        return pointDiscountPrice;
    }

    public void setPointDiscountPrice(int pointDiscountPrice) {
        this.pointDiscountPrice = pointDiscountPrice;
    }

    public int getCouponDiscountPrice() {
        return couponDiscountPrice;
    }

    public void setCouponDiscountPrice(int couponDiscountPrice) {
        this.couponDiscountPrice = couponDiscountPrice;
    }

    public int getAddPiece() {
        return addPiece;
    }

    public void setAddPiece(int addPiece) {
        this.addPiece = addPiece;
    }

    public List<OrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetail> details) {
        this.details = details;
    }

    public String getStoreBrand() {
        return storeBrand;
    }

    public void setStoreBrand(String storeBrand) {
        this.storeBrand = storeBrand;
    }

    public boolean isOrderedByOnlineStore() {
        return orderedByOnlineStore;
    }

    public void setOrderedByOnlineStore(boolean orderedByOnlineStore) {
        this.orderedByOnlineStore = orderedByOnlineStore;
    }
}
