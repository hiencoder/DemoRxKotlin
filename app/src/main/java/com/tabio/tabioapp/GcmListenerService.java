package com.tabio.tabioapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.api.decide.DecideApiParams;
import com.tabio.tabioapp.api.decide.DecideApiRequest;
import com.tabio.tabioapp.api.decide.DecideApiResponse;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.model.Me;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/24/16.
 */
public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    public static final String TAG = makeLogTag(GcmListenerService.class);

    public GcmListenerService() {
        LOGD(TAG, "GcmListenerService");
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);

        LOGD(TAG, "GCM from:" + from);
        LOGD(TAG, "GCM data:" + data.toString());
        try {
            GcmObject gcmObject = null;
            String title = "";
            String message = "";

            String niftyKey = "com.nifty.Data";
            // DECIDEからのPUSHの場合
            if (data.get(niftyKey) != null) {
                JSONObject json = new JSONObject(data.getString(niftyKey));
                LOGD(TAG, "push json:" + json.toString(4));
                String notificationId = "";
                if (json.has("notificationId")) {
                    notificationId = json.getString("notificationId");
                }
                String screenKey = "";
                if (json.has("screen_key")) {
                    screenKey = json.getString("screen_key");
                }
                String identifier = "";
                if (json.has("identifier")) {
                    identifier = json.getString("identifier");
                }
                title = data.getString("title");
                message = data.getString("message");
                gcmObject = new GcmObject(screenKey, identifier, notificationId);
            } else {
                // 会員基盤からのPUSHの場合
                LOGD(TAG, "DECIDE以外:"+data.toString());
                if (data.getString("title") != null) {
                    title = data.getString("title");
                }
                if (data.getString("message") != null) {
                    message = data.getString("message");
                }
                String screenKey = "";
                if (data.getString("screen_key") != null) {
                    screenKey = data.getString("screen_key");
                }
                String identifier = "";
                if (data.getString("identifier") != null) {
                    identifier = data.getString("identifier");
                }
                gcmObject = new GcmObject(screenKey, identifier, null);
            }

            sendNotification(title, message, gcmObject);
        } catch (JSONException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendNotification(String title, String message, GcmObject gcmObject) {

        Me self = AppController.getInstance().getSelf(true);
        if (!self.isExists()) {
            return;
        }
        if (!self.isLogin() || self.isLeaved() || self.isSuspended()) {
            return;
        }
        if (gcmObject == null) {
            LOGD(TAG, "GCM:gcmObjectがnullです");
            return;
        }
        Intent view = new Intent(this, MainActivity.class);
        view.putExtra("gcmObject", gcmObject);
        view.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, view,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(largeIcon)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(message)
//                .setWhen(System.currentTimeMillis())
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        sendNotificationReceivedOk(gcmObject);

        if (gcmObject.getScreenKey().equals(GcmObject.SCREEN_COUPONS)) {
            CouponBus.get().post(gcmObject);
        }
    }

    private void sendNotificationReceivedOk(GcmObject gcmObject) {
        try {
            LOGD(TAG, "sendNotificationReceivedOk");
            if (gcmObject.getNotificationId() == null || gcmObject.getNotificationId().isEmpty()) {
                LOGE(TAG, "required notificationId:"+gcmObject.getNotificationId());
                return;
            }
            DecideApiParams params = new DecideApiParams(ApiRoute.DECIDE_PUSH_OPEN + gcmObject.getNotificationId());
            DecideApiRequest request = new DecideApiRequest(getApplicationContext());
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
                            LOGD(TAG, decideApiResponse.getBody().toString());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            LOGE(TAG, e.getMessage());
        }
    }
}
