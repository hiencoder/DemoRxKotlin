package com.tabio.tabioapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/18/15.
 */
public class Route extends DBModel implements Serializable {
    public static final String TAG = makeLogTag(Route.class);
    public static final String TABLE_NAME = "routes";

    public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
            +KEYS.FROM            +" "+"TEXT PRIMARY KEY UNIQUE NOT NULL"+","
            +KEYS.PROVIDER_ID     +" "+"TEXT"+","
            +KEYS.SECURITY_KEY    +" "+"TEXT"+""
            +");";


    public static final class KEYS {
        public static final String FROM = "_from";
        public static final String PROVIDER_ID = "provider_id";
        public static final String SECURITY_KEY = "security_key";
    }

    @Retention(RetentionPolicy.SOURCE)
        @StringDef({EMAIL, TWITTER, FACEBOOK})
    public @interface From{}
    public static final String EMAIL = "0";
    public static final String TWITTER = "1";
    public static final String FACEBOOK = "2";
    public static final String[] FROM_LIST = new String[]{EMAIL,TWITTER,FACEBOOK};

    private String from;
    private String providerId;
    private String securityKey;

    private RouteManager manager;

    public Route(@From String from) {
        super(TABLE_NAME);
        this.from = from;
        initialize();
        this.manager = new RouteManager(this);
    }

    public boolean isExists() {
        return isExists(KEYS.FROM, new String[]{this.getFrom()});
    }

    @Override
    protected void initialize() {
        if (!isExists()) {
            return;
        }
        SQLiteDatabase rdb = getRDB();
        Cursor c = null;
        try {
            c = rdb.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+KEYS.FROM+" = ?", new String[]{this.from});
            if (c.moveToNext()) {
                setProviderId(c.getString(c.getColumnIndex(KEYS.PROVIDER_ID)));
                setSecurityKey(c.getString(c.getColumnIndex(KEYS.SECURITY_KEY)));
            }
        } catch (SQLiteException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
//            LOGD(TAG, "Route initialize");
//            LOGD(TAG, "from"+getName());
//            LOGD(TAG, "ProviderId:"+getProviderId());
//            LOGD(TAG, "SecurityKey:"+getSecurityKey());
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    protected ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEYS.FROM, getFrom());
        cv.put(KEYS.PROVIDER_ID, getProviderId());
        cv.put(KEYS.SECURITY_KEY, getSecurityKey());
        return cv;
    }

    public RouteManager getManager() {
        return manager;
    }

    public String getName() {
        if (getFrom().equals(FACEBOOK)) {
            return "facebook";
        } else if (getFrom().equals(TWITTER)) {
            return "twitter";
        } else if (getFrom().equals(EMAIL)) {
            return "email";
        }
        return "";
    }

    @From
    public String getFrom() {
        return from;
    }

    public void setFrom(@From String from) {
        this.from = from;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }
}
