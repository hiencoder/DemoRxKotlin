package com.tabio.tabioapp.me;

import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;

import java.util.HashMap;

/**
 * Created by san on 3/18/16.
 */
public class MyParameterHelper {

    /**
     * 会員移行のためのパラメータを取得する
     */
    public static HashMap<String, Object> getMigrationParams(Me me, @com.tabio.tabioapp.model.Route.From int from, String userId, String securityKey) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("route", String.valueOf(from));
        params.put("user_id", userId);
        params.put("security_key", securityKey);
        params = addDefaultParams(me, params);
        return params;
    }

    /**
     * プロフィール更新のためのパラメータを取得する
     *
     * tabio_id:String
     * token:String
     * icon:String
     * cover:String
     * nickname:String
     * birthday:String(yyyy/mm/dd hh:mm:ss)
     * sex:String
     * twitter_id:String
     * facebook_id:String
     * email:String
     * information:int
     * mailmaga:int
     * language:String
     *
     * @return
     */
    public static HashMap<String, Object> getProfileUpdateParams(Me me) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("icon", me.getProfile().getIconImgBlob() == null ?
                "null" : me.getProfile().getIconImgBlob());
        params.put("cover", me.getProfile().getCoverImgBlob() == null ?
                "null" : me.getProfile().getCoverImgBlob());
        params.put("nickname", me.getProfile().getNickname());
        params.put("birthday", "null");//yyyy/mm/dd hh:mm:ss
        params.put("sex", me.getProfile().getGender() == null ?
                "null" : me.getProfile().getGender());//TODO

//        if (MyManagerHelper.getRoute(Route.TWITTER, me) != null) {
//            String twitterId = MyManagerHelper.getRoute(Route.TWITTER, me).getProviderId();
//            if (twitterId != null && !twitterId.equals("null")) {
//                params.put("twitter_id", twitterId);
//            }
//        }
//        if (MyManagerHelper.getRoute(Route.FACEBOOK, me) != null) {
//            String facebookId = MyManagerHelper.getRoute(Route.FACEBOOK, me).getProviderId();
//            if (facebookId != null && !facebookId.equals("null")) {
//                params.put("facebook_id", facebookId);
//            }
//        }
//        if (MyManagerHelper.getRoute(Route.EMAIL, me) != null) {
//            String email = MyManagerHelper.getRoute(Route.EMAIL, me).getProviderId();
//            if (email != null && !email.equals("null")) {
//                params.put("email", email);
//            }
//        }
//        params.put("password", ""); // never use this
        params.put("information", me.isReceiveNews() ? 1 : 0);
        params.put("mailmaga", me.isReceiveMailMagazine() ? 1 : 0);
        params = addDefaultParams(me, params);
        return params;
    }








    public static HashMap<String, Object> addDefaultParams(Me me, HashMap<String, Object> params) {
        params.put("tabio_id", me.getTabioId());
        params.put("token", me.getToken());
        params = addLanguageParams(me, params);
        return params;
    }

    public static HashMap<String, Object> addLanguageParams(Me me, HashMap<String, Object> params) {
        params.put("language", me.getLanguage());
        return params;
    }
}
