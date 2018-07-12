package com.tabio.tabioapp.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tabio.tabioapp.BuildConfig;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/19/16.
 */
public class DBHelper extends SQLiteOpenHelper implements Serializable {
    public static final String TAG = makeLogTag(DBHelper.class);
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = BuildConfig.APPLICATION_ID+".db";

    private static DBHelper sharedInstance;

    public static synchronized DBHelper getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new DBHelper(context);
        }
        return sharedInstance;
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Me.CREATE_SQL);
        db.execSQL(Profile.CREATE_SQL);
        db.execSQL(Route.CREATE_SQL);
        db.execSQL(Store.CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
