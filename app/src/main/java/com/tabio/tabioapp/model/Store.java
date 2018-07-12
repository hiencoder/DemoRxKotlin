package com.tabio.tabioapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/19/15.
 */
public class Store extends DBModel implements Serializable {
    public static final String TAG = makeLogTag(Store.class);
    public static final String TABLE_NAME = "stores";


    public static final class KEYS implements Serializable {
        public static final String STORE_ID = "store_id";
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String BRAND = "brand";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String UPDATED_AT = "updated_at";
    }

    public static final String CREATE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
            + KEYS.STORE_ID + " " + "TEXT PRIMARY KEY UNIQUE NOT NULL" + ","
            + KEYS.CODE + " " + "TEXT" + ","
            + KEYS.NAME + " " + "TEXT" + ","
            + KEYS.BRAND + " " + "TEXT" + ","
            + KEYS.LATITUDE + " " + "TEXT" + ","
            + KEYS.LONGITUDE + " " + "TEXT" + ","
            + KEYS.UPDATED_AT + " " + "TEXT" + ""
            + ");";

    public static final String MENS = "01";
    public static final String LADIES = "02";
    public static final String KIDS = "03";
    public static final String EMBROIDERY = "11"; //刺繍
    public static final String PRINTING = "12"; //プリント
    public static final String NONSKID = "13"; //滑り止め・ツボ押し
    public static final String CHINA_UNIONPAY = "22"; //銀聯カード
    public static final String DUTY_FREE = "23"; //免税対応
    public static final String PIECE = "91"; //ピース付与
    public static final String POINT = "92"; //ポイント付与

    @StringDef({MENS,LADIES,KIDS,EMBROIDERY,PRINTING,NONSKID,CHINA_UNIONPAY,DUTY_FREE,PIECE,POINT})
    public @interface StoreItemService {
    }

    private String storeId = "";
    private String code = "";
    private String name = "";
    private double latitude = 0;
    private double longitude = 0;
    private String updatedAt = "";
    private boolean isFavorite = false;
    private String zip = "";
    private String prefecture = "";
    private String address = "";
    private String tel = "";
    private String access = "";
    private String operationDate = "";
    private String openTimeOfDay = "";
    private String closeTimeOfDay = "";
    private String openTimeOfHoliday = "";
    private String closeTimeOfHoliday = "";
    private String brand = "";
    private String distance = "";//meter
    private String rssUrl = "";
    private List<String> services = new ArrayList<>();
    private List<Coordinate> coordinates = new ArrayList<>();
    private int coordinateStartPosition = 0;
    private List<Blog> blogs = new ArrayList<>();
    private int blogStartPosition = 0;
    private List<Integer> itemServices = new ArrayList<>();
    private List<Checkin> checkins = new ArrayList<>();
    private Stock stock;

    private String checkinDate = "";

    private boolean canCheckin = false; // 現在地から600M以内

    private StoreManager manager;

    public Store() {
        super(TABLE_NAME);
    }

    public Store(JSONObject json) {
        super(TABLE_NAME);
        getManager().store(json);
    }

    public Store(String storeId) {
        super(TABLE_NAME);

        SQLiteDatabase rdb = getRDB();
        Cursor c = null;
        try {
            c = rdb.rawQuery("SELECT * from " + TABLE_NAME + " WHERE " + KEYS.STORE_ID + " = ?", new String[]{storeId});
            c = initializeFromDb(c);
        } catch (SQLiteException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private Cursor initializeFromDb(Cursor c) {
        if (c.moveToNext()) {
            setStoreId(c.getString(c.getColumnIndex(KEYS.STORE_ID)));
            setCode(c.getString(c.getColumnIndex(KEYS.CODE)));
            setName(c.getString(c.getColumnIndex(KEYS.NAME)));
            setBrand(c.getString(c.getColumnIndex(KEYS.BRAND)));
            setLatitude(Double.valueOf(c.getString(c.getColumnIndex(KEYS.LATITUDE))));
            setLongitude(Double.valueOf(c.getString(c.getColumnIndex(KEYS.LONGITUDE))));
            setUpdatedAt(c.getString(c.getColumnIndex(KEYS.UPDATED_AT)));
        }
        return c;
    }

    public boolean setLastOne() {
        SQLiteDatabase rdb = getRDB();
        Cursor c = null;
        try {
            c = rdb.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + KEYS.UPDATED_AT + " DESC LIMIT 1;", null);
            if (c.getCount() > 0) {
                c = initializeFromDb(c);
                return true;
            }
            return false;
        } catch (SQLiteException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    // 1時間以内に店舗情報をアップデートしたかどうか
    public boolean wasUpdatedWithinOneHour() {
        long now = new Date().getTime();
        long updatedAt = getUpdatedAtDate().getTime();

        // TODO: 端末の日付が狂ってるとおかしなことになるぞい
        if (TimeUnit.MILLISECONDS.toHours(now - updatedAt) > 0) {
            return false;
        }
        return true;
    }

    @Override
    protected void initialize() {
    }

    @Override
    protected ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEYS.STORE_ID, getStoreId());
        cv.put(KEYS.CODE, getCode());
        cv.put(KEYS.NAME, getName());
        cv.put(KEYS.BRAND, getBrand());
        cv.put(KEYS.LATITUDE, String.valueOf(getLatitude()));
        cv.put(KEYS.LONGITUDE, String.valueOf(getLongitude()));
        cv.put(KEYS.UPDATED_AT, getUpdatedAt());
        return cv;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
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

    public String getNameWithBrand() {
        try {
            return getBrand() + " " + name;
        } catch (NullPointerException e) {
            return name;
        }
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAtDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return sdf.parse(getUpdatedAt());
        } catch (ParseException e) {
            LOGE(TAG, e.getMessage());
            return null;
        }
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(String operationDate) {
        this.operationDate = operationDate;
    }

    public String getTimeOfDayForDisplay() {
        return getOpenTimeOfDay() + "~" + getCloseTimeOfDay();
    }

    public String getOpenTimeOfDay() {
        // 10:00:00
        if (openTimeOfDay.isEmpty()) {
            return "-";
        }
        return openTimeOfDay.substring(0, 5);
    }

    public void setOpenTimeOfDay(String openTimeOfDay) {
        this.openTimeOfDay = openTimeOfDay;
    }

    public String getCloseTimeOfDay() {
        if (closeTimeOfDay.isEmpty()) {
            return "-";
        }
        return closeTimeOfDay.substring(0, 5);
    }

    public void setCloseTimeOfDay(String closeTimeOfDay) {
        this.closeTimeOfDay = closeTimeOfDay;
    }

    public String getTimeOfHolidayForDisplay() {
        return getOpenTimeOfHoliday() + "~" + getCloseTimeOfHoliday();
    }

    public String getOpenTimeOfHoliday() {
        if (openTimeOfHoliday.isEmpty()) {
            return "-";
        }
        return openTimeOfHoliday.substring(0, 5);
    }

    public void setOpenTimeOfHoliday(String openTimeOfHoliday) {
        this.openTimeOfHoliday = openTimeOfHoliday;
    }

    public String getCloseTimeOfHoliday() {
        if (closeTimeOfHoliday.isEmpty()) {
            return "-";
        }
        return closeTimeOfHoliday.substring(0, 5);
    }

    public void setCloseTimeOfHoliday(String closeTimeOfHoliday) {
        this.closeTimeOfHoliday = closeTimeOfHoliday;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }


    public String getDistance() {
        return distance;
    }

    // 100km以上は小数点以下を表示しない
    public String getDistanceForDisplay() {
        try {
            Double ddistance = Double.parseDouble(getDistance());
            String result = "";
            if (ddistance > 1000) {
                // kilo meter
                Double kilo = ddistance / 1000;
                // 100km以上の場合は小数点を出さない
                if (kilo.intValue() > 100) {
                    result = String.valueOf(kilo.intValue());
                } else {
                    // 100kmより小さい場合は、小数点第１まで表示する
                    BigDecimal bigDecimal = new BigDecimal(String.valueOf(kilo));
                    double floorKilo = bigDecimal.setScale(1, RoundingMode.FLOOR).doubleValue();
                    result = String.valueOf(floorKilo);
                }
            } else {
                // meter
                result = String.valueOf(ddistance.intValue());
            }
            return result+" ";
        } catch (NumberFormatException e) {
            LOGE(TAG, e.getMessage()+","+getDistance()+" ");
            e.printStackTrace();
            return "";
        }
    }

    public String getDistanceUnit() {
        try {
            Double ddistance = Double.parseDouble(getDistance());
            int idistance = ddistance.intValue();
            if (idistance > 1000) {
                return "km";
            } else {
                return "m";
            }
        } catch (NumberFormatException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return "";
        }

    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<Integer> getItemServices() {
        return itemServices;
    }

    public void setItemServices(List<Integer> itemServices) {
        this.itemServices = itemServices;
    }

    public List<Checkin> getCheckins() {
        return checkins;
    }

    public void setCheckins(List<Checkin> checkins) {
        this.checkins = checkins;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public StoreManager getManager() {
        return new StoreManager(this);
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Blog> getBlogs() {
        return blogs;
    }

    public void setBlogs(List<Blog> blogs) {
        this.blogs = blogs;
    }

    public boolean hasStock(int classId) {
        if (getStock() != null ) {
            if (getStock().getClassId() == classId) {
                return true;
            }
        }
        return false;
    }

    public int getBlogStartPosition() {
        return blogStartPosition;
    }

    public void setBlogStartPosition(int blogStartPosition) {
        this.blogStartPosition = blogStartPosition;
    }

    public int getCoordinateStartPosition() {
        return coordinateStartPosition;
    }

    public void setCoordinateStartPosition(int coordinateStartPosition) {
        this.coordinateStartPosition = coordinateStartPosition;
    }

    public String getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(String checkinDate) {
        this.checkinDate = checkinDate;
    }
}

