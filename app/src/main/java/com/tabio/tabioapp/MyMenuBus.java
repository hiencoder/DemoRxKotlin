package com.tabio.tabioapp;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.tabio.tabioapp.me.MyMenu;

/**
 * Created by san on 4/17/16.
 */
public class MyMenuBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static MyMenuBus bus;

    public static MyMenuBus get(){
        if (bus == null) {
            bus = new MyMenuBus();
        }
        return bus;
    }

    public void post(final MyMenu myMenu) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(myMenu);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MyMenuBus.super.post(myMenu);
                }
            });
        }
    }
}
