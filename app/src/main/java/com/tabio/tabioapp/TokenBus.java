package com.tabio.tabioapp;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.tabio.tabioapp.model.Me;

/**
 * Created by san on 9/8/16.
 */
public class TokenBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static TokenBus bus;

    public static TokenBus get() {
        if (bus == null) {
            bus = new TokenBus();
        }
        return bus;
    }

    public void post(final Me me) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(me);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    TokenBus.super.post(me);
                }
            });
        }
    }
}
