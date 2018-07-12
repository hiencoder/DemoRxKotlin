package com.tabio.tabioapp;

import android.content.Intent;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/24/16.
 */
public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {
    public static final String TAG = makeLogTag(InstanceIDListenerService.class);

    public InstanceIDListenerService() {
    }

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, GcmRegistrationService.class);
        startService(intent);
    }
}
