package com.tabio.tabioapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.checkin.CheckinActivity;
import com.tabio.tabioapp.coordinate.CoordinatesCollectionActivity;
import com.tabio.tabioapp.coupon.CouponListActivity;
import com.tabio.tabioapp.item.ItemsActivity;
import com.tabio.tabioapp.item.SwipeableItemsActivity;
import com.tabio.tabioapp.item.review.ReviewCreateActivity;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.me.AccountEditActivity;
import com.tabio.tabioapp.me.MyActivity;
import com.tabio.tabioapp.me.MyIdActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.preference.EmailPasswordRegisterActivity;
import com.tabio.tabioapp.preference.EmailPasswordUpdateActivity;
import com.tabio.tabioapp.preference.FaqActivity;
import com.tabio.tabioapp.preference.LanguageSettingsActivity;
import com.tabio.tabioapp.preference.MigrationActivity;
import com.tabio.tabioapp.preference.NotificationSettingsActivity;
import com.tabio.tabioapp.preference.PreferencesActivity;
import com.tabio.tabioapp.store.StoreActivity;
import com.tabio.tabioapp.store.StoreFilter;
import com.tabio.tabioapp.store.StoresActivity;
import com.tabio.tabioapp.top.TopActivity;
import com.tabio.tabioapp.tutorial.TutorialActivity;
import com.tabio.tabioapp.web.WebActivity;

import java.io.Serializable;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/23/16.
 */
public class GcmObject implements Serializable {
    public static final String TAG = makeLogTag(GcmObject.class);

    public static final String SCREEN_ITEM = "1";//商品詳細
    public static final String SCREEN_WEB = "2";//WebView
    public static final String SCREEN_STORE = "3";//店舗詳細
    public static final String SCREEN_MYPAGE = "4";//マイページ
    public static final String SCREEN_ACCOUNT = "5";//アカウントプレビュー
    public static final String SCREEN_PREFERENCES = "6";//その他
    public static final String SCREEN_FAQ = "7";//FAQ
    public static final String SCREEN_CHECKIN = "8";
    public static final String SCREEN_MEMBERSHIP = "9";
    public static final String SCREEN_COORDINATES = "10";
    public static final String SCREEN_ITEMS = "11";
    public static final String SCREEN_STORES = "12";
    public static final String SCREEN_COUPONS = "13";
    public static final String SCREEN_EMAIL_LOGIN = "14";
    public static final String SCREEN_EMAIL_MIGRATE = "15";
    public static final String SCREEN_ONLINE_MIGRATE = "16";
    public static final String SCREEN_NOTIFICATION = "17";
    public static final String SCREEN_LANGUAGE = "18";
    public static final String SCREEN_CART = "19";
    public static final String SCREEN_TUTORIAL = "20";
    public static final String SCREEN_POST_REVIEW = "21";
    public static final String SCREEN_IDPIN_SAVE = "22";

    private String screenKey = "";
    private String identifier = "";
    private String notificationId = "";

    public GcmObject(String screenKey, @Nullable String identifier, @Nullable String notificationId) {
        this.screenKey = screenKey;
        if (identifier != null) this.identifier = identifier;
        if (notificationId != null) this.notificationId = notificationId;
    }

    public Intent getIntent(Context c) {
        Intent view = null;
        String pageId = "";
        if (!needOpenScreen()) {
            view = new Intent(c, TopActivity.class);
        } else {
            switch (getScreenKey()) {
                case SCREEN_ITEM:
                    pageId = "570f2f9a99c3634a425af4f6";
                    view = new Intent(c, SwipeableItemsActivity.class);
                    view.putExtra("ids", new int[]{Integer.valueOf(getIdentifier())});
                    break;
                case SCREEN_WEB:
                    pageId = "";
                    view = new Intent(c, WebActivity.class);
                    String url = getIdentifier();
                    if (url.indexOf("/sv/xwgn/") != -1) {
                        Me me = AppController.getInstance().getSelf(false);
                        url += "?data="+me.getTabioId();
                    }
                    view.putExtra("url", url);
                    break;
                case SCREEN_STORE:
                    pageId = "570f2de199c3634a425af4e2";
                    view = new Intent(c, StoreActivity.class);
                    view.putExtra("store_id", getIdentifier());
                    break;
                case SCREEN_MYPAGE:
                    pageId = "570f300999c3634a425af503";
                    view = new Intent(c, MyActivity.class);
                    break;
                case SCREEN_ACCOUNT:
                    pageId = "570f305499c3634a425af509";
                    view = new Intent(c, AccountEditActivity.class);
                    break;
                case SCREEN_PREFERENCES:
                    pageId = "570f307299c3634a425af50d";
                    view = new Intent(c, PreferencesActivity.class);
                    if (!getIdentifier().isEmpty()) {
                        view.putExtra("identifier", Integer.parseInt(getIdentifier()));
                    }
                    break;
                case SCREEN_FAQ:
                    pageId = "570f315d99c3634a425af529";
                    view = new Intent(c, FaqActivity.class);
                    break;
                case SCREEN_CHECKIN:
                    pageId = "570f2d2499c3634a425af4cc";
                    view = new Intent(c, CheckinActivity.class);
                    break;
                case SCREEN_MEMBERSHIP:
                    pageId = "570f295499c3634a425af4b4";
                    view = new Intent(c, MainActivity.class);
                    break;
                case SCREEN_COORDINATES:
                    pageId = "570f2ad099c3634a425af4c2";
                    view = new Intent(c, CoordinatesCollectionActivity.class);
                    break;
                case SCREEN_ITEMS:
                    pageId = "570f2e9c99c3634a425af4ea";
                    view = new Intent(c, ItemsActivity.class);
                    break;
                case SCREEN_STORES: {
                    pageId = "570f2dca99c3634a425af4de";
                    view = new Intent(c, StoresActivity.class);
                    Me self = AppController.getInstance().getSelf(false);
                    ApiParams params = new ApiParams(self, true, true, ApiRoute.GET_STORES);
                    params = new StoreFilter(c).addFilterParams(params);
                    params.put("search", 0);
                    view.putExtra("title", c.getString(R.string.text_store_result_title));
                    view.putExtra("params", params);
                    view.putExtra("showMap", false);
                    view.putExtra("showDistance", self.getLatitude() >= 0.0);
                    view.putExtra("url", ApiRoute.GET_STORES);
                    break;
                }
                case SCREEN_COUPONS:
                    pageId = "570f2a0899c3634a425af4be";
                    view = new Intent(c, CouponListActivity.class);
                    break;
                case SCREEN_EMAIL_LOGIN: {
                    Me self = AppController.getInstance().getSelf(false);
                    Route route = self.getManager().getRoute(Route.EMAIL);
                    if (route != null) {
                        LOGD(TAG, "route:" + route.getProviderId() + route.getSecurityKey());
                        view = new Intent(c, EmailPasswordUpdateActivity.class);
                    } else {
                        pageId = "570f309a99c3634a425af511";
                        AppController.getInstance().sendGAScreen("メールアドレス・パスワードでログイン");
                        view = new Intent(c, EmailPasswordRegisterActivity.class);
                    }
                    break;
                }
                case SCREEN_EMAIL_MIGRATE:
                    pageId = "570f30c299c3634a425af517";
                    view = new Intent(c, MigrationActivity.class);
                    view.putExtra("mode", MigrationActivity.MIGRATION_EMAILPASSWORD);
                    break;
                case SCREEN_ONLINE_MIGRATE:
                    pageId = "570f30af99c3634a425af513";
                    view = new Intent(c, MigrationActivity.class);
                    view.putExtra("mode", MigrationActivity.MIGRATION_ONLINE);
                    break;
                case SCREEN_NOTIFICATION:
                    pageId = "570f30cd99c3634a425af519";
                    view = new Intent(c, NotificationSettingsActivity.class);
                    break;
                case SCREEN_LANGUAGE:
                    pageId = "570f30d699c3634a425af51b";
                    view = new Intent(c, LanguageSettingsActivity.class);
                    break;
                case SCREEN_CART:
                    pageId = "570f2fef99c3634a425af500";
                    view = new Intent(c, WebActivity.class);
                    view.putExtra("url", BuildConfig.BASE_URL+ApiRoute.WV_CART);
                    break;
                case SCREEN_TUTORIAL:
                    pageId = "570f30e199c3634a425af51d";
                    view = new Intent(c, TutorialActivity.class);
                    view.putExtra("showLogo", false);
                    view.putExtra("nocheck", true);
                    break;
                case SCREEN_POST_REVIEW:
                    pageId = "570f2fc099c3634a425af4fc";
                    view = new Intent(c, ReviewCreateActivity.class);
                    view.putExtra("productId", Integer.parseInt(getIdentifier()));
                    break;
                case SCREEN_IDPIN_SAVE:
                    pageId = "570f309099c3634a425af50f";
                    view = new Intent(c, MyIdActivity.class);
                    break;
            }
        }
        view.putExtra("gcmObject", this);
        if (getNotificationId() != null && !getNotificationId().isEmpty()) {
            AppController.getInstance().decideTrack(pageId, getNotificationId());
        }
        return view;
    }

    public boolean needOpenScreen() {
        return !this.screenKey.isEmpty();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getScreenKey() {
        return screenKey;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String convertedMyPageIdentifier() {
        switch (this.identifier) {
            case "0":
                return "3";
            case "1":
                return "2";
            case "2":
                return "4";
            case "3":
                return "5";
            case "4":
                return "7";
            case "5":
                return "6";
            case "6":
                return "1";
            case "7":
                return "0";
            case "8":
                return "8";
            case "9":
                return "9";
        }
        return this.identifier;
    }

}
