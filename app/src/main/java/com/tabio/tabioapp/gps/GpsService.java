package com.tabio.tabioapp.gps;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.GpsBus;
import com.tabio.tabioapp.model.Store;

import java.util.ArrayList;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/24/16.
 */
public class GpsService extends Service implements LocationListener {
    public static final String TAG = makeLogTag(GpsService.class);

    private LocationManager locationManager;

    private static final long MIN_TIME = 10000;// ms
    private static final float MIN_DISTANCE = 0;// meter
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    @Override
    public void onCreate() {
        super.onCreate();
        LOGD(TAG, "GPS: GpsService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGD(TAG, "GPS:Start Service on StarCommand");

        this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
//            final Criteria criteria = new Criteria();
//            criteria.setBearingRequired(false);    // 方位不要
//            criteria.setSpeedRequired(false);    // 速度不要
//            criteria.setAltitudeRequired(false);    // 高度不要
//            final String provider = locationManager.getBestProvider(criteria, true);
            final String provider = LocationManager.GPS_PROVIDER;
            LOGD(TAG, "locationProvider:" + provider);
            if (locationManager != null) {
                LOGD(TAG, "locationManager requestLocationUpdates");

                this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            } else {
                LOGD(TAG, "GPS:STOP SELF");
                if (this.locationManager != null) {
                    this.locationManager.removeUpdates(this);
                }
                stopSelf();
            }
        } catch (SecurityException e) {
            LOGE(TAG, e.getLocalizedMessage());
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        }

        LOGD(TAG, "GPS:isProviderEnabled?" + this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

//        return START_REDELIVER_INTENT;
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.locationManager != null) {
            try {
                this.locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                LOGE(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        LOGE(TAG, "GPS:" + "サービスが停止しました");
    }

    @Override
    public void onLocationChanged(Location location) {
        LOGD(TAG, "GPS:location changed:" + location.getLatitude() + ":" + location.getLongitude());
        try {
            Location lastKnownLocation = this.locationManager.getLastKnownLocation(location.getProvider());
            boolean isBetterLocation = isBetterLocation(location, lastKnownLocation);
            LOGD(TAG, "isBetterLocation:"+isBetterLocation);
            MyLocation myLocation = null;
            if (isBetterLocation) {
                myLocation = new MyLocation(location.getLatitude(), location.getLongitude());
            } else {
                myLocation = new MyLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            }
            GpsBus.get().post(myLocation);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
//        LOGD(TAG, "GPS:status changed:" + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        LOGD(TAG, "providerEnabled:" + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        LOGE(TAG, "providerDisabled:" + provider);
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GpsService.class.getName().equals(service.service.getClassName())) {
                LOGD(TAG, "GPSサービスが起動中です。");
                return true;
            }
        }
        LOGD(TAG, "GPSサービスは起動していません");
        return false;
    }


    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    public static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
