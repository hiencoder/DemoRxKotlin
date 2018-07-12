package com.tabio.tabioapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/24/16.
 */
public class GpsUtils {
    public static final String TAG = makeLogTag(GpsUtils.class);

    public static boolean allowAccessLocation(Context context, @Nullable LocationManager locationManager) {
        if (
                ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        &&
                        ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
            if (locationManager != null) {
                if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
