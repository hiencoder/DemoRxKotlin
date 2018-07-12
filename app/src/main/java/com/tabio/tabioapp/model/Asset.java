package com.tabio.tabioapp.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/19/15.
 */
public class Asset implements Serializable {
    private static final String TAG = makeLogTag(Asset.class);

    private List<String> mainImgUrls;
    private List<Lineup> lineups;

    public Asset(JSONObject json) {
        this.mainImgUrls = new ArrayList<>();
        this.lineups = new ArrayList<>();
        getManager().save(json);
    }

    public AssetManager getManager() {
        return new AssetManager(this);
    }

    public List<String> getMainImgUrls() {
        return mainImgUrls;
    }

    public void setMainImgUrls(List<String> mainImgUrls) {
        this.mainImgUrls = mainImgUrls;
    }

    public List<Lineup> getLineups() {
        if (lineups == null) {
            return Arrays.asList();
        }
        return lineups;
    }

    public Lineup getCurrentLineup() {
        for (Lineup lineup : getLineups()) {
            if (lineup.isSelected()) {
                return lineup;
            }
        }
        return null;
    }

    public void setLignups(List<Lineup> lineups) {
        this.lineups = lineups;
    }

}
