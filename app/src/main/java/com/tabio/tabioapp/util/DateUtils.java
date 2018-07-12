package com.tabio.tabioapp.util;

import android.content.Context;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Profile;
import com.tabio.tabioapp.preference.LanguageSettingsActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/23/16.
 */
public class DateUtils {
    public static final String TAG = makeLogTag(DateUtils.class);

    public static String getWeekday(Context c, Calendar cal) {
        int[] weekDayStrings = new int[]{
                R.string.text_date_weekday_sunday_short,
                R.string.text_date_weekday_monday_short,
                R.string.text_date_weekday_tuesday_short,
                R.string.text_date_weekday_wednesday_short,
                R.string.text_date_weekday_thursday_short,
                R.string.text_date_weekday_friday_short,
                R.string.text_date_weekday_saturday_short
        };
        return c.getString(weekDayStrings[cal.get(Calendar.DAY_OF_WEEK)-1]);
    }

    public static String getDateFromFormat(String format, String date, String lang) {
        return getDateFromFormat(format, date, lang, Locale.JAPAN);
    }

    public static String getDateFromFormat(String format, String date, String lang, Locale locale) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
            Date d = sdf.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            if (lang.equals(LanguageSettingsActivity.LANG_JA)) {
                return cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH);
            } else {
                return cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
            }
        } catch (ParseException e) {
            LOGE(TAG, e.getMessage());
            return "";
        }
    }

    public static String getDateFromFormat2(Context c, String format, String date, String lang) {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date d = sdf.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);

            if (lang.equals(LanguageSettingsActivity.LANG_JA)) {
                String year = c.getString(R.string.text_date_year);
                String month = c.getString(R.string.text_date_month);
                String day = c.getString(R.string.text_date_day);
                return cal.get(Calendar.YEAR) + year + (cal.get(Calendar.MONTH) + 1) + month + cal.get(Calendar.DAY_OF_MONTH) + day;
            } else {
                return cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
            }
        } catch (ParseException e) {
            LOGE(TAG, e.getMessage());
            return "";
        }
    }

    public static String getDateFromFormat3(Context c, String format, String date, String lang) {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date d = sdf.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);

            if (lang.equals(LanguageSettingsActivity.LANG_JA)) {
                String year = c.getString(R.string.text_date_year);
                String month = c.getString(R.string.text_date_month);
                String day = c.getString(R.string.text_date_day);
                return cal.get(Calendar.YEAR) + year + (cal.get(Calendar.MONTH) + 1) + month + cal.get(Calendar.DAY_OF_MONTH) + day + " " +
                        cal.get(Calendar.HOUR_OF_DAY)+"時"+cal.get(Calendar.MINUTE)+"分"+cal.get(Calendar.SECOND)+"秒";
            } else {
                return cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR) +
                        cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND)+"";
            }
        } catch (ParseException e) {
            LOGE(TAG, e.getMessage());
            return "";
        }
    }

    public static Date getDateFromString(String format, String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date d = sdf.parse(date);
            return d;
        } catch (ParseException e) {
            return new Date();
        }
    }
}
