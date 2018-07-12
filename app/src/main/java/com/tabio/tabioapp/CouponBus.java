package com.tabio.tabioapp;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.tabio.tabioapp.model.Coupon;


/**
 * Created by san on 7/27/16.
 */
public class CouponBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static CouponBus bus;

    public static CouponBus get() {
        if (bus == null) {
            bus = new CouponBus();
        }
        return bus;
    }

    public void post(final GcmObject couponGcmObject) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(couponGcmObject);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (couponGcmObject != null) {
                        CouponBus.super.post(couponGcmObject);
                    }
                }
            });
        }
    }
}
