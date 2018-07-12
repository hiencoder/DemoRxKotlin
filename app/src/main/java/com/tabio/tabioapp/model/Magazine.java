package com.tabio.tabioapp.model;


import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/19/15.
 */
public class Magazine implements Serializable {
    private static final String TAG = makeLogTag(Magazine.class);

    private int magazineId;
    private String name; // 雑誌名
    private String issue; // 号
    private String pubDate; // 発売日
    private int page; // 掲載ページ


    public Magazine(JSONObject json) {
        getManager().save(json);
    }

    public MagazineManager getManager() {
        return new MagazineManager(this);
    }

    public int getMagazineId() {
        return magazineId;
    }

    public void setMagazineId(int magazineId) {
        this.magazineId = magazineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

}
