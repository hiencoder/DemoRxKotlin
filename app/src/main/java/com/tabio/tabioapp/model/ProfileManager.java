package com.tabio.tabioapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/20/16.
 */
public class ProfileManager extends ModelManager implements Serializable {
    public static final String TAG = makeLogTag(ProfileManager.class);

    private Profile self;

    public ProfileManager(Profile profile) {
        this.self = profile;
    }

    public boolean save() {
        if (!self.isExists()) {
            self.insert(self.getContentValues());
        } else {
            return self.update(Profile.KEYS.IDENTIFIER, new String[]{Profile.IDENTIFIER}, self.getContentValues());
        }
        return true;
    }

    @Override
    public boolean save(JSONObject json) {
        if (json == null) {
            return false;
        }
        try {
            if (isSafe(json, "icon")) {
                self.setIconImgUrl(json.getString("icon"));
            } else {
                self.setIconImgUrl("");
                self.setIconImgBlob("");
            }
            if (isSafe(json, "cover")) {
                self.setCoverImgUrl(json.getString("cover"));
            } else {
                self.setCoverImgUrl("");
                self.setCoverImgBlob("");
            }
            if (isSafe(json, "nickname")) {
                self.setNickname(json.getString("nickname"));
            } else {
                self.setNickname("");
            }
            if (isSafe(json, "birthday")) {
                self.setBirthday(json.getString("birthday"));
            } else {
                self.setBirthday("");
            }
            if (isSafe(json, "sex")) {
                String gender = json.getString("sex");
                if (gender.equals(Profile.MAN)) {
                    gender = Profile.MAN;
                } else if (gender.equals(Profile.WOMAN)) {
                    gender = Profile.WOMAN;
                } else {
                    gender = Profile.UNKNOWN_GENDER;
                }
                self.setGender(gender);
            } else {
                self.setGender(Profile.UNKNOWN_GENDER);
            }

            boolean result = save();
            return result;
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean destroy() {
        return self.destroy(Profile.KEYS.IDENTIFIER, new String[]{Profile.IDENTIFIER});
    }
}
