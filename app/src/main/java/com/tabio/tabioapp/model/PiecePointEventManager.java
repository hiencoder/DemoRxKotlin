package com.tabio.tabioapp.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/17/16.
 */
public class PiecePointEventManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(PiecePointEventManager.class);

    private PiecePointEvent self;

    public PiecePointEventManager(PiecePointEvent piecePointEvent) {
        this.self = piecePointEvent;
    }

    @Override
    public boolean save(JSONObject json) {
        if (json == null) {
            return false;
        }
        try {
            if (isSafe(json, "event")) self.setName(json.getString("event"));
            if (isSafe(json, "date")) self.setDate(json.getString("date"));
            if (isSafe(json, "piece")) self.setPiece(json.getInt("piece"));
            if (isSafe(json, "point")) self.setPiece(json.getInt("point"));

            return true;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
