package com.tabio.tabioapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.tabio.tabioapp.AppController;

import java.io.Serializable;
import java.util.Arrays;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/19/16.
 */
public abstract class DBModel implements Serializable {
    public static final String TAG = makeLogTag(DBModel.class);

    private String tableName;
//    protected Context context;

    public DBModel(String tableName) {
//        this.context = context;
        this.tableName = tableName;
    }

    protected SQLiteDatabase getWDB() {
        return AppController.getInstance().getWDB();
    }

    protected SQLiteDatabase getRDB() {
        return AppController.getInstance().getRDB();
    }

    protected abstract void initialize();

    protected abstract ContentValues getContentValues();

    protected int getCount() {
        try {
            int count = (int) DatabaseUtils.queryNumEntries(getRDB(), this.tableName);
//            LOGD(TAG, this.tableName + "'s size is " + count);
            return count;
        } catch (RuntimeException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean isExists(String key, String[] args) {
        SQLiteDatabase rdb = getRDB();
        Cursor c = null;
        try {
            c = rdb.rawQuery("SELECT * FROM "+tableName+" WHERE "+key+" = ?", args);
            if (c.getCount() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLiteException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public boolean isExists() {
        return getCount() > 0 ? true : false;
    }

//    protected Cursor select() {
//        SQLiteDatabase rdb = getRDB();
//        Cursor c = null;
//        try {
//            rdb.beginTransaction();
//        c = rdb.rawQuery("SELECT * from "+TABLE_NAME, null);
//        } catch (SQLiteException e) {
//            LOGE(TAG, e.getLocalizedMessage());
//            e.printStackTrace();
//        } finally {
//            rdb.endTransaction();
//            if (c != null) {
//                c.close();
//            }
//        }
//
//    }

    protected void insert(ContentValues contentValues) {
        SQLiteDatabase db = getWDB();
        try {
//            db.beginTransaction();
            db.insert(this.tableName, null, contentValues);
//            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
//                db.endTransaction();
//                db.close();
            }
        }
    }

    protected boolean update(String key, String[] args, ContentValues contentValues) {
        SQLiteDatabase db = getWDB();
        try {
//            db.beginTransaction();
//            LOGD(TAG, "UPDATE START:"+this.tableName);
//            LOGD(TAG, "key:"+key);
//            LOGD(TAG, "args:"+ Arrays.toString(args));
            int result = db.update(this.tableName, contentValues, key+" = ?", args);
            if (result == 0) {
                LOGE(TAG, "update result:"+result);
            } else {
//                LOGD(TAG, "update result:"+result);
            }
//            db.setTransactionSuccessful();
            return result == 1 ? true : false;
        } catch (SQLiteException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (db != null && db.isOpen()) {
//                db.endTransaction();
//                db.close();
            }
        }
    }

    protected boolean destroy(String key, String[] args) {
        SQLiteDatabase db = getWDB();
        try {
//            db.beginTransaction();
            int result = db.delete(this.tableName, key + " = ?", args);
//            LOGD(TAG, "destroy "+this.tableName+" result is "+result);
//            db.setTransactionSuccessful();
            return result==1? true:false;
        } catch (SQLiteException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
//                db.endTransaction();
//                db.close();
            }
        }
        return false;
    }
}
