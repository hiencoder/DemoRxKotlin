package com.tabio.tabioapp;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.tabio.tabioapp.gps.MyLocation;

/**
 * Created by pixie3 on 3/24/16.
 */
public class GcmBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static GcmBus bus;

    public static GcmBus get() {
        if (bus == null) {
            bus = new GcmBus();
        }
        return bus;
    }

    public void post(final String gcmToken) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(gcmToken);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (gcmToken != null && !gcmToken.isEmpty()) {
                        GcmBus.super.post(gcmToken);
                    }
                }
            });
        }
    }
}
