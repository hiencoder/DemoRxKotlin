package com.tabio.tabioapp.model;

import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/14/16.
 */
public class News extends AppModel implements Serializable {
    public static final String TAG = makeLogTag(News.class);

    private int classId;
    private String date;
    private String message;

    public News(JSONObject json) {
        getManager().save(json);
    }

    public NewsManager getManager() {
        return new NewsManager(this);
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
