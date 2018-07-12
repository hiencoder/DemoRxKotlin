package com.tabio.tabioapp.model;

import com.tabio.tabioapp.util.DateUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/17/16.
 */
public class PiecePointEvent extends AppModel implements Serializable {
    public static final String TAG = makeLogTag(PiecePointEvent.class);

    private String name;
    private String date;
    private int piece;

    public PiecePointEvent(JSONObject json) {
        getManager().save(json);
    }

    public PiecePointEventManager getManager() {
        return new PiecePointEventManager(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateForDisplay(String lang) {
        return DateUtils.getDateFromFormat("yyyy/MM/dd HH:mm:ss", getDate(), lang);
    }

    public int getPiece() {
        return piece;
    }

    public String getPieceForDisplay() {
        try {
            String result = String.format("%1$,3d", getPiece());
            return result + " ";
        } catch (NumberFormatException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return String.valueOf(getPiece());
        }
    }

    public void setPiece(int piece) {
        this.piece = piece;
    }

}
