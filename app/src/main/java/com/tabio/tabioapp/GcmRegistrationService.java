package com.tabio.tabioapp;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/24/16.
 */
public class GcmRegistrationService extends IntentService {
    public static final String TAG = makeLogTag(GcmRegistrationService.class);

    public GcmRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(BuildConfig.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            LOGD(TAG, "GCM Token:"+token);
            GcmBus.get().post(token);
        } catch (Exception e) {
            LOGE(TAG, "Failed to complete token refresh "+e.getMessage());
        }
    }
}
