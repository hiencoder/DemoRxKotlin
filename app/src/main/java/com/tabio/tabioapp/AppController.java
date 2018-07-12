package com.tabio.tabioapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.api.decide.DecideApiParams;
import com.tabio.tabioapp.api.decide.DecideApiRequest;
import com.tabio.tabioapp.api.decide.DecideApiResponse;
import com.tabio.tabioapp.gps.MyLocation;
import com.tabio.tabioapp.model.DBHelper;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.top.TopActivity;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 11/11/15.
 */
public class AppController extends android.support.multidex.MultiDexApplication {

    private static final String TAG = makeLogTag(AppController.class);

    private static AppController instance;

    private Me self;

    public static Tracker tracker;

    private static DBHelper dbHelper;
    private static SQLiteDatabase rdb;
    private static SQLiteDatabase wdb;
    private String decideCsrfToken = "";
    private String decideCookie = "";
    private String decideUuid = "";
    private int decideCompTime = 0;// サーバ時間と端末時間の補完時間
    public boolean gotCompTime = false;
    private boolean isDecideLogin = false;

    private Handler handler;

    private OkHttpClient decideHttpClient;

    private OkHttpClient httpClient;

    private TwitterAuthConfig twitterAuthConfig;

    private static AlertDialog alertDialog;
    private static AlertDialog.Builder alertDialogBuilder;
    private static ProgressDialog progressDialog;
    private static ProgressDialog.Builder progressDialogBuilder;

    public SharedPreferences sharedPreferences;

    public static final float VISIT_SHOP_DISTANCE = 300;

    public static synchronized AppController getInstance() {
        return instance;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        this.self = Me.newInstance();
        this.handler = new Handler();
        this.sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", MODE_PRIVATE);
        this.twitterAuthConfig = new TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_SECRET);

        Picasso picasso = new Picasso.Builder(this).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                LOGE(TAG, exception.getMessage());
            }
        }).build();
        Picasso.setSingletonInstance(picasso);

        Fabric.with(this, new Crashlytics(), new Twitter(this.twitterAuthConfig));

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        getHttpClient();
        getAppDirectory();
        getAppMediaDirectory();
        GpsBus.get().register(this);
        GcmBus.get().register(this);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            CookieManager.getInstance().removeExpiredCookie();
//        }
//        CookieManager.getInstance().setAcceptCookie(true);
    }

    @Override
    public void onTerminate() {
        GpsBus.get().unregister(this);
        GcmBus.get().unregister(this);
        super.onTerminate();
    }

    public Me getSelf(boolean update) {
        if (update) {
            self = Me.newInstance();
        }
        return self;
    }

    public void setSelf(Me self) {
        this.self = self;
    }

    public OkHttpClient getHttpClient() {
        if (this.httpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(20, TimeUnit.SECONDS);
            builder.readTimeout(20, TimeUnit.SECONDS);
            builder.writeTimeout(20, TimeUnit.SECONDS);
            this.httpClient = builder.build();
        }
        return this.httpClient;
    }

    public OkHttpClient getDecideHttpClient() {
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        this.decideHttpClient = new OkHttpClient.Builder().addInterceptor(logging).cookieJar(cookieJar).build();
        return this.decideHttpClient;
    }

    public String getAppDirectory() {
        String directoryName = Environment.getExternalStorageDirectory().toString() + "/" + BuildConfig.APPLICATION_ID + "/";
        File dir = new File(directoryName);
        if (dir.exists()) {
            LOGD(TAG, "already " + directoryName + " exists(app directory)");
        } else {
            if (!dir.mkdirs()) {
                LOGE(TAG, "app directory can't make");
//                throw new RuntimeException("could not make directory");
            }
        }
        LOGD(TAG, "appDirectory:" + directoryName);
        return directoryName;
    }

    public String getAppMediaDirectory() {
        String directoryName = getAppDirectory() + "media/";
        File dir = new File(directoryName);
        if (dir.exists()) {
            LOGD(TAG, "already " + directoryName + " exists(media directory)");
        } else {
            if (!dir.mkdirs()) {
                LOGE(TAG, "media directory can't make");
//                throw new RuntimeException("could not make directory");
            }
        }
        LOGD(TAG, "appMediaDirectory:" + directoryName);
        return directoryName;
    }

    public synchronized DBHelper getDbHelper() {
        if (this.dbHelper == null) {
            this.dbHelper = DBHelper.getInstance(this);
        }
        return this.dbHelper;
    }

    public SQLiteDatabase getRDB() {
        if (this.rdb == null) {
            this.rdb = getDbHelper().getReadableDatabase();
        }
//        if (!this.rdb.isOpen()) {
//            this.rdb.close();
////                this.rdb = null;
//            this.rdb = getDbHelper().getReadableDatabase();
//        }
        return this.rdb;
    }

    // TODO: 落ちるときがある
    public synchronized SQLiteDatabase getWDB() {
        if (this.wdb == null) {
            this.wdb = getDbHelper().getWritableDatabase();
        }
//        if (!this.wdb.isOpen()) {
//            this.wdb.close();
////            this.wdb = null;
//            this.dbHelper = null;
//            this.wdb = getDbHelper().getWritableDatabase();
//        }
        return this.wdb;
    }


    synchronized public Tracker getGATracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setAppOptOut(false);
            analytics.setDryRun(false);
            analytics.setLocalDispatchPeriod(1000);
            tracker = analytics.newTracker(BuildConfig.GOOGLE_ANALYTICS_ID);
//            tracker.enableExceptionReporting(true);
//            tracker.enableAdvertisingIdCollection(true);
//            tracker.enableAutoActivityTracking(true);
        }
        return tracker;
    }

    public void sendGAScreen(String screenName) {
        getGATracker().setScreenName(screenName);
        getGATracker().send(new HitBuilders.ScreenViewBuilder().build());
        getGATracker().setScreenName(null);
    }

    public void sendGAEvent(String category, String action, String label, long value) {
        getGATracker().send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    public String getDecideCsrfToken() {
        return decideCsrfToken;
    }

    public void setDecideCsrfToken(String decideCsrfToken) {
        this.decideCsrfToken = decideCsrfToken;
    }

    public void setDecideCookie(String decideCookie) {
        if (decideCookie == null) {
            return;
        }
        if (decideCookie.equals("")) {
            return;
        }
        this.decideCookie = decideCookie;
    }

    public String getDecideCookie() {
        return this.decideCookie;
    }

    public String getDecideUuid() {
        return decideUuid;
    }

    public void setDecideUuid(String decideUuid) {
        this.decideUuid = decideUuid;
    }

    public boolean isDecideLogin() {
        return isDecideLogin;
    }

    public void setDecideLogin(boolean decideLogin) {
        isDecideLogin = decideLogin;
        gotCompTime = true;
    }

    public int getDecideCompTime() {
        return decideCompTime;
    }

    public void setDecideCompTime(int decideCompTime) {
        this.decideCompTime = decideCompTime;
    }

    @Subscribe
    public void onLocationChanged(MyLocation location) {
        LOGD(TAG, "onLocationChanged");
        if (location == null) {
            return;
        }
        if (location.getLatitude() <= 0.0 || location.getLongitude() <= 0.0) {
            return;
        }
        updateMyLocation(location);


        SQLiteDatabase rdb = AppController.getInstance().getRDB();
        Cursor c = null;
        List<HashMap> objects = new ArrayList<>();
        try {
            c = rdb.rawQuery("SELECT " + Store.KEYS.STORE_ID + "," + Store.KEYS.LATITUDE + "," + Store.KEYS.LONGITUDE + " FROM " + Store.TABLE_NAME, null);
            while (c.moveToNext()) {
                String storeId = c.getString(c.getColumnIndex(Store.KEYS.STORE_ID));
                double slatitude = Double.valueOf(c.getString(c.getColumnIndex(Store.KEYS.LATITUDE)));
                double slongitude = Double.valueOf(c.getString(c.getColumnIndex(Store.KEYS.LONGITUDE)));

                float[] results = new float[3];
                Location.distanceBetween(slatitude, slongitude, location.getLatitude(), location.getLongitude(), results);
                float distance = results[0];
//                LOGD(TAG, "distance:"+distance);
                if (distance <= VISIT_SHOP_DISTANCE) {
                    HashMap<String, Object> object = new HashMap<>();
                    object.put("distance", (int) distance);
                    object.put("storeId", storeId);
                    objects.add(object);
                }
            }
        } catch (SQLiteException e) {
            LOGE(TAG, e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
            // TODO:v1.0では店舗訪問イベントは使用しない
            /*
            for (HashMap<String, Object> object : objects) {
//                LOGD(TAG, "object:"+object.get("distance"));
                DecideApiRequest decideApiRequest = new DecideApiRequest(this);
                DecideApiParams params = new DecideApiParams(ApiRoute.DECIDE_SHOP_VISIT + object.get("storeId"));
                params.put("distance", String.valueOf(object.get("distance")) + "m");
                LOGD(TAG, "Decide shop visit");
                decideApiRequest.run(params)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(Schedulers.newThread())
                        .subscribe(new Observer<DecideApiResponse>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(DecideApiResponse decideApiResponse) {

                            }
                        });
            }
            */
        }
    }

    public void updateMyLocation(MyLocation location) {
        LOGD(TAG, "updateMyLocation:" + location.getLatitude() + ":" + location.getLongitude());
        if (location == null) {
            return;
        }
        if (location.getLatitude() <= 0.0 || location.getLongitude() <= 0.0) {
            return;
        }

        try {
            float[] results = new float[3];
            Location.distanceBetween(self.getLatitude(), self.getLongitude(), location.getLatitude(), location.getLongitude(), results);
            float distance = results[0];
            if (distance < 30) {
                LOGE(TAG, "30m以内の移動のため、無視");
                return;
            }

            self.setLatitude(location.getLatitude());
            self.setLongitude(location.getLongitude());
            boolean result = self.getManager().save();
            LOGD(TAG, "update self result:" + result);

            updateDeviceInfo(self);
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    @Subscribe
    public void onGcmTokenUpdate(String gcmToken) {
        LOGD(TAG, "onGcmTokenUpdate:" + gcmToken);
        self.setDeviceId(gcmToken);
        try {
            boolean result = self.getManager().save();
            LOGD(TAG, "update self result:" + result);
            updateDeviceInfo(self);
        } catch (RuntimeException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showSynchronousProgress(Context context) {
        showProgress(context, false, null, null, null, null, null, null);
    }

    public static void showApiErrorAlert(Context context, final ApiError error) {
        showApiErrorAlert(context, error, null);
    }

    public static void showApiErrorAlert(Context context, ApiError error, DialogInterface.OnClickListener onClickListener) {
        if (error == null) {
            error = new ApiError(null);
        }
        final int code = error.getCode();
        LOGE(TAG, "error code:" + code);
        String titleAfterChara = "";
        if (BuildConfig.DEBUG) {
            titleAfterChara = " [" + String.valueOf(code) + "]";
        }
        final String title = context.getString(R.string.error) + titleAfterChara;
        final String message = error.getMessage();
        if (code == ApiError.CODE_99) {
            AppController.getInstance().maintenanceMode();
        } else if (code == ApiError.CODE_4 || code == ApiError.CODE_5) {
            AppController.getInstance().updateToken();
        } else {
            showProgress(context, true, title, message, null, null, onClickListener, null);
        }
    }


    public static void showAlert(Context context, String title, String message) {
        showProgress(context, true, title, message, null, null, null, null);
    }

    public static void showAlert(Context context, String title, String message, DialogInterface.OnClickListener positiveListener) {
        showProgress(context, true, title, message, null, null, positiveListener, null);
    }

    public static void showAlert(Context context, String title, String message, String positiveButtonTitle, String negativeButtonTitle,
                                 DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        showProgress(context, true, title, message, positiveButtonTitle, negativeButtonTitle, positiveListener, negativeListener);
    }


    public static void showProgress(
            final Context context, final boolean cancelable,
            final String title, final String message,
            final String positiveButtonTitle, final String negativeButtonTitle,
            final DialogInterface.OnClickListener onPositiveButtonClickListener,
            final DialogInterface.OnClickListener onNegativeButtonListener) {

        dismissProgress();
        if (!cancelable) {
            showProgressDialog(context);
            return;
        }

        if (alertDialog != null && alertDialog.isShowing()) {
            LOGE(TAG, "alertDialog is showing or null");
            return;
        }
        AppController.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {

//                    AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(
//                            context,cancelable,title,message,
//                            positiveButtonTitle,negativeButtonTitle,
//                            onPositiveButtonClickListener,onNegativeButtonListener);
//                    if (((AppCompatActivity)context).getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.ALERT_TAG) != null) {
//                        dialogFragment.show(dialogFragment.getActivity().getSupportFragmentManager(), AlertDialogFragment.ALERT_TAG);
//                    }

                    AlertDialog.Builder builder = alertDialogBuilder = new AlertDialog.Builder(context);
                    if (title != null) {
                        builder.setTitle(title);
                    }
                    if (message != null) {
                        builder.setMessage(message);
                    }
                    builder.setCancelable(false);
                    builder.setPositiveButton(positiveButtonTitle == null ?
                                    context.getString(R.string.button_ok) : positiveButtonTitle,
                            onPositiveButtonClickListener != null ? onPositiveButtonClickListener : null);

                    if (onNegativeButtonListener != null) {
                        builder.setNegativeButton(negativeButtonTitle, onNegativeButtonListener);
                    }
                    AppController.getInstance().alertDialog = builder.create();
                    if (!AppController.getInstance().alertDialog.isShowing()) {
                        AppController.getInstance().alertDialog.show();
                    }
                } catch (WindowManager.BadTokenException e) {
                    LOGE(TAG, "WindowManager.BadTokenException:" + e.getMessage());
                } catch (NullPointerException e) {
                    LOGE(TAG, "NullPointerException:" + e.getMessage());
                } catch (Exception e) {
                    LOGE(TAG, "Exception:" + e.getMessage());
                }
            }
        });
    }

    public static AlertDialog getAlertDialog() {
        return alertDialog;
    }

    public static void dismissProgress() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            LOGE(TAG, e.getMessage());
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        }

    }


    public static void showProgressDialog(final Context context) {
        if (AppController.getInstance().progressDialog != null &&
                AppController.getInstance().progressDialog.isShowing()) {
            LOGE(TAG, "progressDialog is showing or null");
            return;
        }
        AppController.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    ProgressDialog progressDialog = AppController.getInstance().progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("Loading.....");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                } catch (WindowManager.BadTokenException e) {
                    LOGE(TAG, "WindowManager.BadTokenException:" + e.getMessage());
                } catch (NullPointerException e) {
                    LOGE(TAG, "NullPointerException:" + e.getMessage());
                } catch (Exception e) {
                    LOGE(TAG, "Exception:" + e.getMessage());
                }
            }
        });
    }


    public void deleteSelfAndBackToTop() {
        LOGE(TAG, "deleteSelfAndBackToTop");
        getSelf(true);
        try {
            boolean result = self.getProfile().getManager().destroy();
            LOGE(TAG, "destroy profile:"+result);
            for (Route route : self.getRoutes()) {
                if (route.isExists()) {
                    result = route.getManager().destroy();
                    LOGE(TAG, "destroy route"+route.getFrom()+":"+result);
                }
            }
            result = self.getManager().destroy();
            LOGE(TAG, "destroy self:"+result);
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
//        sharedPreferences.edit().clear().apply();
        Intent view = new Intent(getApplicationContext(), TopActivity.class);
        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(view);
    }

    public void maintenanceMode() {
        Intent view = new Intent(this, MaintenanceActivity.class);
        view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(view);
    }

    public void updateToken() {
        ApiParams params = new ApiParams(self, true, ApiRoute.REFRESH_TOKEN);
        AppController.getInstance().showSynchronousProgress(this);

        ApiRequest request = new ApiRequest(this);
        request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        AppController.getInstance().dismissProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, e.getMessage());
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(getApplicationContext(), "トークンの更新に失敗しました(network?)", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (apiResponse.hasError()) {
                            LOGE(TAG, apiResponse.getErrorMessage());
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(getApplicationContext(), "トークンの更新に失敗しました:"+apiResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(getApplicationContext(), "トークンを更新しました", Toast.LENGTH_SHORT).show();
                        }
                        AppController.getInstance().getSelf(true);
                        TokenBus.get().post(self);
                    }
                });
    }

    public Handler getHandler() {
        return handler;
    }

    private void updateDeviceInfo(Me me) {
        if (!me.isLogin()) {
            return;
        }

        try {

            ApiParams params = new ApiParams(me, true, ApiRoute.DEVICE_REGISTER);
            params.put("latitude", me.getLatitude());
            params.put("longitude", me.getLongitude());
            params.put("device_id", me.getDeviceId());

            ApiRequest request = new ApiRequest(this);
            request.run(params)
                    .observeOn(Schedulers.newThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Observer<ApiResponse>() {
                        @Override
                        public void onCompleted() {
                            LOGD(TAG, "device info updated!");
                        }

                        @Override
                        public void onError(Throwable e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(ApiResponse apiResponse) {
                            // resultはnullなので何もしない
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void decideTrack(String pageId) {
        decideTrack(pageId, null);
    }

    public void decideTrack(String pageId, @Nullable String notificationId) {
//        if (!AppController.getInstance().isDecideLogin()) {
//            return;
//        }
        DecideApiRequest request = new DecideApiRequest(this);
        DecideApiParams params = new DecideApiParams(ApiRoute.DECIDE_TRACKING);
        params.put("page_id", pageId);
        if (notificationId != null && !notificationId.isEmpty()) {
            params.put("notification_id", notificationId);
        }
        request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<DecideApiResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(DecideApiResponse decideApiResponse) {
                    }
                });


    }
}
