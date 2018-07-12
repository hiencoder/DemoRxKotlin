package com.tabio.tabioapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.IntDef;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.preference.LanguageSettingsActivity;
import com.tabio.tabioapp.util.DateUtils;
import com.tabio.tabioapp.util.ImageUtils;
import com.tabio.tabioapp.util.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/18/15.
 */
public class Me extends DBModel implements Serializable {
    public static final String TAG = makeLogTag(Me.class);
    public static final String TABLE_NAME = "self";
    public static final String IDENTIFIER = "tabio";

    public static final int EXCHANGE_PIECE = 5000;
    public static final int EXCHANGE_POINT = 100;

    public static final class KEYS {
        public static final String IDENTIFIER = "identifier";
        public static final String UUID = "uuid";
        public static final String TABIO_ID = "tabio_id";
        public static final String PIN_CODE = "pin_code";
        public static final String TOKEN = "token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String TOKEN_EXPIRES = "token_expires";
        public static final String BARCODE_URL = "barcode_url";
        public static final String APP_URL = "app_url";
        public static final String LANGUAGE = "language";
        public static final String GIVEN_INCENTIVE = "given_incentive";
        public static final String USER_ID = "user_id";
        public static final String PIECE = "piece";
        public static final String PIECE_EXPIRES = "piece_expires";
        public static final String POINT = "point";
        public static final String POINT_EXPIRES = "point_expires";
        public static final String RANK = "rank";
        public static final String RECEIVE_NEWS = "receive_news";
        public static final String RECEIVE_MAIL_MAGAZINE = "receive_mail_magazine";
        public static final String APP_START_COUNT = "app_start_count";
        public static final String STATUS = "status";
        public static final String DEVICE_ID = "device_id";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String LOGIN = "login";
        public static final String DECIDE_TIME_DELTA = "decide_time_delta";
    }

    public static final String CREATE_SQL = "CREATE TABLE "+TABLE_NAME+" ("
            +KEYS.IDENTIFIER             +" "+"TEXT PRIMARY KEY UNIQUE NOT NULL"+","
            +KEYS.UUID                   +" "+"TEXT"+","
            +KEYS.TABIO_ID               +" "+"TEXT"+","
            +KEYS.PIN_CODE               +" "+"TEXT"+","
            +KEYS.TOKEN                  +" "+"TEXT"+","
            +KEYS.REFRESH_TOKEN          +" "+"TEXT"+","
            +KEYS.TOKEN_EXPIRES          +" "+"TEXT"+","
            +KEYS.BARCODE_URL            +" "+"TEXT"+","
            +KEYS.APP_URL                +" "+"TEXT"+","
            +KEYS.LANGUAGE               +" "+"TEXT"+","
            +KEYS.USER_ID                +" "+"TEXT"+","
            +KEYS.PIECE                  +" "+"INTEGER"+","
            +KEYS.PIECE_EXPIRES          +" "+"TEXT"+","
            +KEYS.POINT                  +" "+"INTEGER"+","
            +KEYS.POINT_EXPIRES          +" "+"TEXT"+","
            +KEYS.RANK                   +" "+"INTEGER"+","
            +KEYS.RECEIVE_NEWS           +" "+"INTEGER"+","
            +KEYS.RECEIVE_MAIL_MAGAZINE  +" "+"INTEGER"+","
            +KEYS.APP_START_COUNT        +" "+"INTEGER"+","
            +KEYS.STATUS                 +" "+"INTEGER"+","
            +KEYS.DEVICE_ID              +" "+"TEXT"+","
            +KEYS.LATITUDE               +" "+"TEXT"+","
            +KEYS.LONGITUDE              +" "+"TEXT"+","
            +KEYS.LOGIN                  +" "+"INTEGER"+","
            +KEYS.DECIDE_TIME_DELTA      +" "+"INTEGER"
            +");";

    @Retention(RetentionPolicy.SOURCE)
        @IntDef ({ACTIVE, SUSPENDED, LEAVED})
        public @interface UserStatus{}
        public static final int ACTIVE = 0; // 通常
        public static final int SUSPENDED = 1; // 停止
        public static final int LEAVED = 2; // 退会

    private String identifier = IDENTIFIER;
    private String uuid = "";
    private String tabioId = "";
    private String pinCode = "";
    private String token = "";
    private String refreshToken = "";
    private String tokenExpires = "";
    private String barcodeUrl = "";
    private String appUrl = "";
    private String language = "";
    private String userId = "";
    private int piece = 0; // total piece count
    private String pieceExpires = "";
    private int point = 0; // total point count
    private String pointExpires = "";
    private int rank = 0;
    private boolean receiveNews = false;
    private boolean receiveMailMagazine = false;
    private int appStartCount = 0;
    private int status = ACTIVE;
    private String deviceId = "";
    private double latitude = 0;
    private double longitude = 0;
    private int decideTimeDelta = 0;

    private Profile profile;
    private List<Route> routes = new ArrayList<>();
    private List<Coupon> coupons = new ArrayList<>();
    private List<Order> purchasedHistories = new ArrayList<>();

    private boolean login = false;
    private boolean active; // true: status is 0. false: status is 1.
    private boolean suspended; // true: status is 1. false: status is 0.
    private boolean leaved; // true: status is 2.

    private MyManager manager;

    public static Me newInstance() {
        Me me = new Me();
        me.initialize();
        return me;
    }

    public Me() {
        super(TABLE_NAME);
//        initialize();
    }

    @Override
    protected void initialize() {
        SQLiteDatabase rdb = getRDB();
        int count = getCount();
        LOGD(TAG, "self's count:"+count);
        if (count == 0) {
            LOGD(TAG, "insert default values");
            insert(getContentValues());
        }

        Cursor c = null;
        try {
            c = rdb.rawQuery("SELECT * from "+TABLE_NAME, null);
            if (c.moveToNext()) {
                // データベースからメモリにセットする
                setUuid(c.getString(c.getColumnIndex(KEYS.UUID)));
                setTabioId(c.getString(c.getColumnIndex(KEYS.TABIO_ID)));
                setPinCode(c.getString(c.getColumnIndex(KEYS.PIN_CODE)));
                setToken(c.getString(c.getColumnIndex(KEYS.TOKEN)));
                setRefreshToken(c.getString(c.getColumnIndex(KEYS.REFRESH_TOKEN)));
                setTokenExpires(c.getString(c.getColumnIndex(KEYS.TOKEN_EXPIRES)));
                setBarcodeUrl(c.getString(c.getColumnIndex(KEYS.BARCODE_URL)));
                setAppUrl(c.getString(c.getColumnIndex(KEYS.APP_URL)));
                setLanguage(c.getString(c.getColumnIndex(KEYS.LANGUAGE)));
                setUserId(c.getString(c.getColumnIndex(KEYS.USER_ID)));
                setPiece(c.getInt(c.getColumnIndex(KEYS.PIECE)));
                setPieceExpires(c.getString(c.getColumnIndex(KEYS.PIECE_EXPIRES)));
                setPoint(c.getInt(c.getColumnIndex(KEYS.POINT)));
                setPointExpires(c.getString(c.getColumnIndex(KEYS.POINT_EXPIRES)));
                setRank(c.getInt(c.getColumnIndex(KEYS.RANK)));
                setReceiveNews(c.getInt(c.getColumnIndex(KEYS.RECEIVE_NEWS))==1?true:false);
                setReceiveMailMagazine(c.getInt(c.getColumnIndex(KEYS.RECEIVE_MAIL_MAGAZINE))==1?true:false);
                setAppStartCount(c.getInt(c.getColumnIndex(KEYS.APP_START_COUNT)));
                int status = c.getInt(c.getColumnIndex(KEYS.STATUS));
                if (status == ACTIVE) {status = ACTIVE;}
                else if (status == SUSPENDED) {status = SUSPENDED;}
                else if (status == LEAVED) {status = LEAVED;}
                else {status = ACTIVE;}
                setStatus(status);
                setDeviceId(c.getString(c.getColumnIndex(KEYS.DEVICE_ID)));
                setLatitude(Double.parseDouble(c.getString(c.getColumnIndex(KEYS.LATITUDE))));
                setLongitude(Double.parseDouble(c.getString(c.getColumnIndex(KEYS.LONGITUDE))));
                setLogin(c.getInt(c.getColumnIndex(KEYS.LOGIN))==1?true:false);
                setDecideTimeDelta(c.getInt(c.getColumnIndex(KEYS.DECIDE_TIME_DELTA)));
                setProfile(new Profile());

                routes = new ArrayList<>();
                for (String from : Route.FROM_LIST) {
                    Route route = new Route(from);
                    if (route != null && route.isExists()) {
                        getRoutes().add(route);
                    }
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            LOGE(TAG, e.getLocalizedMessage());
        } finally {
            if (c != null) {
                c.close();
            }
            if (rdb != null && rdb.isOpen()) {
//                rdb.close();
            }
        }
    }

    @Override
    protected ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEYS.IDENTIFIER, getIdentifier());
        cv.put(KEYS.UUID, getUuid());
        cv.put(KEYS.TABIO_ID, getTabioId());
        cv.put(KEYS.PIN_CODE, getPinCode());
        cv.put(KEYS.TOKEN, getToken());
        cv.put(KEYS.REFRESH_TOKEN, getRefreshToken());
        cv.put(KEYS.TOKEN_EXPIRES, getTokenExpires());
        cv.put(KEYS.BARCODE_URL, getBarcodeUrl());
        cv.put(KEYS.APP_URL, getAppUrl());
        cv.put(KEYS.LANGUAGE, getLanguage());
        cv.put(KEYS.USER_ID, getUserId());
        cv.put(KEYS.PIECE, getPiece());
        cv.put(KEYS.PIECE_EXPIRES, getPieceExpires());
        cv.put(KEYS.POINT, getPoint());
        cv.put(KEYS.POINT_EXPIRES, getPieceExpires());
        cv.put(KEYS.RANK, getRank());
        cv.put(KEYS.RECEIVE_NEWS, isReceiveNews());
        cv.put(KEYS.RECEIVE_MAIL_MAGAZINE, isReceiveMailMagazine());
        cv.put(KEYS.APP_START_COUNT, getAppStartCount());
        cv.put(KEYS.STATUS, getStatus());
        cv.put(KEYS.DEVICE_ID, getDeviceId());
        cv.put(KEYS.LATITUDE, String.valueOf(getLatitude()));
        cv.put(KEYS.LONGITUDE, String.valueOf(getLongitude()));
        cv.put(KEYS.LOGIN, isLogin());
        cv.put(KEYS.DECIDE_TIME_DELTA, getDecideTimeDelta());
//        cv.put(KEYS.DECIDE_CSRF_TOKEN, getDecideCsrfToken());
        return cv;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTabioId() {
        return tabioId;
    }

    public void setTabioId(String tabioId) {
        this.tabioId = tabioId;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenExpires() {
        return tokenExpires;
    }

    public void setTokenExpires(String tokenExpires) {
        this.tokenExpires = tokenExpires;
    }

    public String getBarcodeUrl() {
        return ImageUtils.convertHttpsUrl(barcodeUrl);
    }

    public void setBarcodeUrl(String barcodeUrl) {
        this.barcodeUrl = barcodeUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public String getLanguage() {
        String lang = Locale.getDefault().getLanguage();
        LOGD(TAG, "default language=" + lang);
        if (
                !lang.equals(LanguageSettingsActivity.LANG_JA) &&
                !lang.equals(LanguageSettingsActivity.LANG_EN) &&
                !lang.equals(LanguageSettingsActivity.LANG_FR) &&
                !lang.equals(LanguageSettingsActivity.LANG_ZH) &&
                !lang.equals(LanguageSettingsActivity.LANG_KO)) {
            lang = LanguageSettingsActivity.LANG_EN;
        }
        if (this.language.isEmpty()) {
            this.language = lang;
        }
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPiece() {
        return piece;
    }

    public int getCurrentPiece(int piece) {
        return piece % EXCHANGE_PIECE;
    }

    public int getCurrentPiece() {
        return getCurrentPiece(getPiece());
    }

    // あと何ピースでポイント取得できるか
    public int getPieceLeft(int piece) {
        return EXCHANGE_PIECE - piece;
    }
    public void setPiece(int piece) {
        this.piece = piece;
    }

    public String getPieceExpires() {
        return pieceExpires;
    }

    public String getPieceExpiresForDisplay(Context c, String lang) {
        return DateUtils.getDateFromFormat2(c, "yyyy/MM/dd HH:mm:ss", getPieceExpires(), lang);
    }

    public void setPieceExpires(String pieceExpires) {
        this.pieceExpires = pieceExpires;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getPointExpires() {
        return pointExpires;
    }

    public String getPointExpiresForDisplay(Context c, String lang) {
        return DateUtils.getDateFromFormat2(c, "yyyy/MM/dd HH:mm:ss", getPointExpires(), lang);
    }

    public void setPointExpires(String pointExpires) {
        this.pointExpires = pointExpires;
    }

    public boolean isReceiveNews() {
        return receiveNews;
    }

    public void setReceiveNews(boolean receiveNews) {
        this.receiveNews = receiveNews;
    }

    public boolean isReceiveMailMagazine() {
        return receiveMailMagazine;
    }

    public void setReceiveMailMagazine(boolean receiveMailMagazine) {
        this.receiveMailMagazine = receiveMailMagazine;
    }

    public int getAppStartCount() {
        return appStartCount;
    }

    public void setAppStartCount(int appStartCount) {
        this.appStartCount = appStartCount;
    }

    @UserStatus
    public int getStatus() {
        return status;
    }

    public void setStatus(@UserStatus int status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return "latitude:"+getLatitude()+" "+"longitude:"+getLongitude();
    }

//    public boolean haveLocation() {
//        if (getLatitude() > 0.0 && getLongitude() > 0.0) {
//            return true;
//        }
//        return false;
//    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons;
    }

    public List<Order> getPurchasedHistories() {
        return purchasedHistories;
    }

    public void setPurchasedHistories(List<Order> purchasedHistories) {
        this.purchasedHistories = purchasedHistories;
    }


    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public boolean isLeaved() {
        return leaved;
    }

    public void setLeaved(boolean leaved) {
        this.leaved = leaved;
    }

    public int getRank() {
        return getRankFromPiece(getPiece());
    }

    public int getRankFromPiece(int piece) {
        return ((int)(piece / 5000)+1);
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getDecideTimeDelta() {
        return decideTimeDelta;
    }

    public void setDecideTimeDelta(int decideTimeDelta) {
        this.decideTimeDelta = decideTimeDelta;
    }

    public boolean haveStores() {
        try {
            int count = (int) DatabaseUtils.queryNumEntries(getRDB(), Store.TABLE_NAME);
            LOGD(TAG, Store.TABLE_NAME + "'s size is " + count);
            return count > 0 ? true : false;
        } catch (RuntimeException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getNickname() {
        if (getProfile().getNickname().equals("")) {
            return getTabioId();
        } else {
            return getProfile().getNickname();
        }
    }

    public MyManager getManager() {
        this.manager = new MyManager(this);
        return this.manager;
    }
}
