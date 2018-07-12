package com.tabio.tabioapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringDef;
import android.support.v4.content.ContextCompat;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.util.ImageUtils;
import com.tabio.tabioapp.util.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/18/15.
 */
public class Profile extends DBModel implements Serializable {
    public static final String TAG = makeLogTag(Profile.class);
    public static final String TABLE_NAME = "profile";
    public static final String IDENTIFIER = "tabio";

    public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
            +KEYS.IDENTIFIER      +" "+"TEXT PRIMARY KEY UNIQUE NOT NULL"+","
            +KEYS.ICON_IMG_URL    +" "+"TEXT"+","
            +KEYS.COVER_IMG_URL   +" "+"TEXT"+","
            +KEYS.ICON_IMG_BLOB   +" "+"TEXT"+","
            +KEYS.COVER_IMG_BLOB  +" "+"TEXT"+","
            +KEYS.NICKNAME        +" "+"TEXT"+","
            +KEYS.BIRTHDAY        +" "+"TEXT"+","
            +KEYS.GENDER          +" "+"TEXT"+""
            +");";

    public static final class KEYS {
        public static final String IDENTIFIER = "identifier";
        public static final String ICON_IMG_URL = "icon_img_url";
        public static final String COVER_IMG_URL = "cover_img_url";
        public static final String ICON_IMG_BLOB = "icon_img_blob";
        public static final String COVER_IMG_BLOB = "cover_img_blob";
        public static final String NICKNAME = "nickname";
        public static final String BIRTHDAY = "birthday";
        public static final String GENDER = "gender";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({WOMAN, MAN, UNKNOWN_GENDER})
    public @interface Gender{}
    public static final String MAN = "1";
    public static final String WOMAN = "2";
    public static final String UNKNOWN_GENDER = "3";

    private String identifier = IDENTIFIER;
    private String iconImgUrl = "";
    private String coverImgUrl = "";
    private String iconImgBlob = "";
    private String coverImgBlob = "";
    private String nickname = "";
    private String birthday = "";
    private String gender = UNKNOWN_GENDER;

    private ProfileManager manager;

    public Profile() {
        super(TABLE_NAME);
        initialize();
    }

    @Override
    protected void initialize() {
        int count = getCount();
        LOGD(TAG, "profile's count:"+count);
        if (count == 0) {
            LOGD(TAG, "insert default values");
            insert(getContentValues());
        }

        SQLiteDatabase rdb = getRDB();
        Cursor c = null;
        try {
            c = rdb.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            if (c.moveToNext()) {
                setIconImgUrl(c.getString(c.getColumnIndex(KEYS.ICON_IMG_URL)));
                setCoverImgUrl(c.getString(c.getColumnIndex(KEYS.COVER_IMG_URL)));
                setIconImgBlob(c.getString(c.getColumnIndex(KEYS.ICON_IMG_BLOB)));
                setCoverImgBlob(c.getString(c.getColumnIndex(KEYS.COVER_IMG_BLOB)));
                setNickname(c.getString(c.getColumnIndex(KEYS.NICKNAME)));
                setBirthday(c.getString(c.getColumnIndex(KEYS.BIRTHDAY)));
                String gender = c.getString(c.getColumnIndex(KEYS.GENDER));
                if (gender.equals(WOMAN)) {
                    gender = WOMAN;
                } else if (gender.equals(MAN)) {
                    gender = MAN;
                } else {
                    gender = UNKNOWN_GENDER;
                }
                setGender(gender);
            }
        } catch (IllegalStateException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        } catch (SQLiteException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    protected ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEYS.IDENTIFIER, getIdentifier());
        cv.put(KEYS.ICON_IMG_URL, getIconImgUrl());
        cv.put(KEYS.COVER_IMG_URL, getCoverImgUrl());
        cv.put(KEYS.ICON_IMG_BLOB, getIconImgBlob());
        cv.put(KEYS.COVER_IMG_BLOB, getCoverImgBlob());
        cv.put(KEYS.NICKNAME, getNickname());
        cv.put(KEYS.BIRTHDAY, getBirthday());
        cv.put(KEYS.GENDER, getGender());
        return cv;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ProfileManager getManager() {
        return new ProfileManager(this);
    }

    public String getIconImgUrl() {
        return ImageUtils.convertHttpsUrl(iconImgUrl);
    }

    public void setIconImgUrl(String iconImgUrl) {
        this.iconImgUrl = iconImgUrl;
    }

    public String getCoverImgUrl() {
        return ImageUtils.convertHttpsUrl(coverImgUrl);
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthday() {
        return birthday;
    }
    public String getBirthdayForApi() {
        try {
            if (getBirthday().isEmpty()) {
                return "null";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = sdf.parse(getBirthday());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int month = cal.get(Calendar.MONTH)+1;
            String monthStr = String.valueOf(month);
            if (month < 10) {
                monthStr = "0"+String.valueOf(month);
            }
            int day = cal.get(Calendar.DAY_OF_MONTH);
            String dayStr = String.valueOf(day);
            if (day < 10) {
                dayStr = "0"+String.valueOf(dayStr);
            }

            String birth = cal.get(Calendar.YEAR)+"/"+monthStr+"/"+dayStr;
            LOGD(TAG, "birthday for api:"+birth);
            return birth;
        } catch (ParseException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Gender
    public String getGender() {
        if (gender.isEmpty()) {
            gender = UNKNOWN_GENDER;
        }
        return gender;
    }

    public void setGender(@Gender String gender) {
        this.gender = gender;
    }

    public String getIconImgBlob() {
        return iconImgBlob;
    }

    public void setIconImgBlob(String iconImgBlob) {
        this.iconImgBlob = iconImgBlob;
    }

    public String getCoverImgBlob() {
        return coverImgBlob;
    }

    public void setCoverImgBlob(String coverImgBlob) {
        this.coverImgBlob = coverImgBlob;
    }

//    public Drawable getRandomIconDrawable(Context c) {
//        int profileResId = R.drawable.ic_mypage_green_square;
//        if (getGender().equals(Profile.MAN)) {
//            profileResId = R.drawable.ic_mypage_blue_square;
//        } else if (getGender().equals(Profile.WOMAN)) {
//            profileResId = R.drawable.ic_mypage_red_square;
//        }
//        Bitmap icon = BitmapFactory.decodeResource(c.getResources(), profileResId);
//        String iconData = ImageUtils.getBase64FromBitmap(icon);
//        setIconImgBlob(iconData);
//
//        return ContextCompat.getDrawable(c, profileResId);
//    }

    public static String getGenderForDisplay(Context context, String gender) {
        if (gender.equals(MAN)) {
            return context.getString(R.string.text_gender_man);
        } else if (gender.equals(WOMAN)) {
            return context.getString(R.string.text_gender_woman);
        } else {
            return "";
        }
    }
}
