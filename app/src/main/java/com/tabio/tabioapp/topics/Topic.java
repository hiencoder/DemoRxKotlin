package com.tabio.tabioapp.topics;

import com.tabio.tabioapp.util.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/14/16.
 */
public class Topic {
    public static final String TAG = makeLogTag(Topic.class);

    private String title;
    private String link;
    private String thumbnail;
    private String date;

    public Topic() {
    }

    public Topic(String date, String thumbnail, String link, String title) {
        this.date = date;
        this.thumbnail = thumbnail;
        this.link = link;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDate() {
        return date;
    }

    public String getDateForDisplay(String lang) {
        return DateUtils.getDateFromFormat("EEE, dd MMM yyyy HH:mm:ss z", getDate(), lang, Locale.ENGLISH);
    }

    public void setDate(String date) {
        this.date = date;
    }
}
