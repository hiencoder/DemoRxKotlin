package com.tabio.tabioapp;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.tabio.tabioapp.gps.MyLocation;

/**
 * Created by pixie3 on 3/24/16.
 */
public class GpsBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static GpsBus bus;

    public static GpsBus get() {
        if (bus == null) {
            bus = new GpsBus();
        }
        return bus;
    }

    public void post(final MyLocation myLocation) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(myLocation);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    GpsBus.super.post(myLocation);
                }
            });
        }
    }
}
