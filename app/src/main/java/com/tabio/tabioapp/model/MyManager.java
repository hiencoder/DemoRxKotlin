package com.tabio.tabioapp.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.util.ImageUtils;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/8/16.
 */
public class MyManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(MyManager.class);

    private Me self;

    public MyManager(Me self) {
        this.self = self;
    }

    public boolean needRefreshToken() {
        if (self.getTokenExpires().isEmpty() || self.getTokenExpires().equals("")) {
            LOGE(TAG, "token refresh doesn't need because token expires is nothing.");
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date expiresDate = sdf.parse(self.getTokenExpires());
            Date now = new Date();
//            LOGD(TAG, "expiresDate:"+expiresDate);
//            LOGD(TAG, "nowDate    :"+now);
            int compare = expiresDate.compareTo(now);
            if (compare == 0 || compare < 0) {
                return true;
            }
        } catch (ParseException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean save() throws Exception {
        return self.update(Me.KEYS.IDENTIFIER, new String[]{Me.IDENTIFIER}, self.getContentValues());
    }

    @Override
    public boolean save(JSONObject json) {
        return save(json, false);
    }

    public boolean save(JSONObject json, boolean savePiece) {
        try {

            json = json.has("result") ? json.getJSONObject("result") : json;

            if (isSafe(json, "tabio_id")) self.setTabioId(json.getString("tabio_id"));
            if (isSafe(json, "pin_code")) self.setPinCode(json.getString("pin_code"));
            if (isSafe(json, "token")) self.setToken(json.getString("token"));
            if (isSafe(json, "token_duration"))
                self.setTokenExpires(json.getString("token_duration"));
            if (isSafe(json, "refresh_token"))
                self.setRefreshToken(json.getString("refresh_token"));
            if (isSafe(json, "appl_url")) self.setAppUrl(json.getString("appl_url"));
            if (isSafe(json, "barcode")) self.setBarcodeUrl(json.getString("barcode"));
            if (savePiece && isSafe(json, "piece")) self.setPiece(json.getInt("piece"));
            if (isSafe(json, "piece_duration"))
                self.setPieceExpires(json.getString("piece_duration"));
            if (isSafe(json, "point")) self.setPoint(json.getInt("point"));
            if (isSafe(json, "point_duration"))
                self.setPointExpires(json.getString("point_duration"));
            if (isSafe(json, "information"))
                self.setReceiveNews(json.getInt("information") == 1 ? true : false);
            if (isSafe(json, "mailmaga"))
                self.setReceiveMailMagazine(json.getInt("mailmaga") == 1 ? true : false);
            if (isSafe(json, "status")) {
                try {
                    int status = json.getInt("status");
                    if (status == Me.ACTIVE) {
                        status = Me.ACTIVE;
                    } else if (status == Me.SUSPENDED) {
                        status = Me.SUSPENDED;
                    } else if (status == Me.LEAVED) {
                        status = Me.LEAVED;
                    } else {
                        status = Me.ACTIVE;
                    }
                    self.setStatus(status);
                } catch (JSONException e) {
                    LOGE(TAG, e.getLocalizedMessage());
                }
            }


            boolean result = true;
            try {
                result = save();
                LOGD(TAG, "save me:" + result);
                if (!result) {
                    LOGE(TAG, "can't save self");
                    return false;
                }
            } catch (Exception e) {
                return false;
            }

            result = self.getProfile().getManager().save(json);
            LOGD(TAG, "save profile:" + result);
            if (!result) {
                LOGE(TAG, "can't save profile");
                // プロフィールはJSONに含まれない場合もあるので、falseは返さない
//                return false;
            }

            self.setRoutes(new ArrayList<Route>());
            for (String from : Route.FROM_LIST) {
                Route route = getRoute(from);
                if (route != null && route.isExists()) {
                    result = route.getManager().save(json);
                    LOGD(TAG, "save route:" + result);
                    if (!result) {
                        LOGE(TAG, "can't save route:" + route.getName());
                        // ルートはJSONに含まれない場合もあるので、falseは返さない
//                        return false;
                    }
                } else {
                    /* DBに存在しない場合 */
                    if (isSafe(json, "twitter_id")) {
                        Route twiRoute = new Route(Route.TWITTER);
                        if (!twiRoute.getManager().save(json)) {
                            return false;
                        }
                    }
                    if (isSafe(json, "facebook_id")) {
                        Route fbRoute = new Route(Route.FACEBOOK);
                        if (!fbRoute.getManager().save(json)) {
                            return false;
                        }
                    }
                    if (isSafe(json, "email")) {
                        Route mailRoute = new Route(Route.EMAIL);
                        if (!mailRoute.getManager().save(json)) {
                            return false;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean destroy() {
        try {
            self.destroy(Me.KEYS.IDENTIFIER, new String[]{Me.IDENTIFIER});
            self.getProfile().getManager().destroy();
            for (Route route : self.getRoutes()) {
                if (route != null && route.isExists()) {
                    route.getManager().destroy();
                }
            }
//            LOGD(TAG, "Destroy Me");
            return true;
        } catch (Exception e) {
//            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Route getRoute(@Route.From String from) {
        int count = self.getCount();
        if (count < 1) {
            return null;
        }

        Route route = new Route(from);
        if (route == null) {
            return null;
        }
        return route.isExists() ? route : null;
    }

    public ApiParams getUpdateProfileParamsFromFacebook(String userId, String token, String iconImgBlob) {
        ApiParams params = getUpdateProfileParams();
        params.put("facebook_id", userId);
        params.put("facebook_token", token);
        params.put("icon", iconImgBlob);
        params.put("cover", self.getProfile().getCoverImgBlob());
        return params;
    }

    public ApiParams getUpdateProfileParamsFromTwitter(String userId, String userName, String token, String iconImgBlob) {
        ApiParams params = getUpdateProfileParams();
        params.put("twitter_id", userId);
        params.put("twitter_token", token);
        params.put("nickname", userName);
        params.put("icon", iconImgBlob);
        params.put("cover", self.getProfile().getCoverImgBlob());
        return params;
    }

    public ApiParams getUpdateProfileParams() {
        self = AppController.getInstance().getSelf(true);
        ApiParams params = new ApiParams(self, true, ApiRoute.UPDATE_USER);

        params.put("nickname", self.getProfile().getNickname().isEmpty() ? "null" : self.getProfile().getNickname());
        params.put("birthday", self.getProfile().getBirthdayForApi().isEmpty() ? "null" : self.getProfile().getBirthdayForApi());
        params.put("sex", self.getProfile().getGender());
        params.put("information", self.isReceiveNews() ? 1 : 0);
        params.put("mailmaga", self.isReceiveMailMagazine() ? 1 : 0);
        return params;
    }

//    public ApiParams getMigrateParams(@Route.From String from) {
//        Route route = getRoute(from);
//        return getMigrateParams(from, route.getProviderId(), route.getSecurityKey());
//    }

    public ApiParams getMigrateParams(@Route.From String from, String idInput, String passwordInput) {
        ApiParams params = new ApiParams(self, true, ApiRoute.MIGRATION);
        params.put("route", from);
        params.put("user_id", idInput);
        params.put("security_key", passwordInput);
        return params;
    }
}
