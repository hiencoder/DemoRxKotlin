package com.tabio.tabioapp.model;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/20/16.
 */
public class RouteManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(RouteManager.class);

    private Route self;

    public RouteManager(Route route) {
        this.self = route;
    }

    public boolean save() {
        if (!self.isExists()) {
            self.insert(self.getContentValues());
        } else {
            return self.update(Route.KEYS.FROM, new String[]{self.getFrom()}, self.getContentValues());
        }
        return true;
    }

    public boolean save(String providerId, String securityKey) {
        try {
            self.setProviderId(providerId);
            if (providerId.equals(Route.EMAIL)) {
                securityKey = "";
            }
            self.setSecurityKey(securityKey);
            return save();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean save(JSONObject json) {
        if (json == null) {
            return false;
        }
        try {
            if (self.getFrom().equals(Route.TWITTER)) {
                if (isSafe(json, "twitter_id")) {
                    self.setProviderId(json.getString("twitter_id"));
                } else {
                    self.getManager().destroy();
                }
            } else if (self.getFrom().equals(Route.FACEBOOK)) {
                if (isSafe(json, "facebook_id")) {
                    self.setProviderId(json.getString("facebook_id"));
                } else {
                    self.getManager().destroy();
                }
            } else if (self.getFrom().equals(Route.EMAIL)) {
                if (isSafe(json, "email")) {
                    self.setProviderId(json.getString("email"));
                } else {
                    self.getManager().destroy();
                }
            }
            return save();
        } catch (JSONException e) {
            LOGE(TAG, e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean destroy() {
        return self.destroy(Route.KEYS.FROM, new String[]{self.getFrom()});
    }
}
