package com.tabio.tabioapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.gps.GpsService;
import com.tabio.tabioapp.gps.MyLocation;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.store.StoresDownloadFragment;
import com.tabio.tabioapp.tutorial.ScreenTutorialFragment;
import com.tabio.tabioapp.tutorial.ScreenTutorialItemFragment;
import com.tabio.tabioapp.util.GpsUtils;

import java.util.ArrayList;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/25/16.
 */
abstract public class BaseStoreActivity extends BaseActivity implements
        StoresDownloadFragment.OnStoresDownloadFragmentCallbacks, LocationListener, ScreenTutorialFragment.OnScreenTutorialFragmentCallbacks {
    public static final String TAG = makeLogTag(BaseStoreActivity.class);

    // 位置情報が許可されているか
    protected boolean allowLocationAccess = false;

    // ストア情報がダウンロードされているか
    protected boolean haveStores = false;

    protected LocationManager locationManager;
    protected MyLocation currentLocation;

    private static final int REQUEST_LOCATION_PERMISSION = 123;
    public static final float CAN_CHECKIN_DISTANCE = 600;
    private static final long MIN_TIME = 10000;// ms
    private static final float MIN_DISTANCE = 100;// meter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getStores();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void createLocationManager() {
        if (this.locationManager == null) {
            this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
    }

    private void startGpsRequest() {
        try {
            LOGD(TAG, "locationManager requestLocationUpdates");
            if (BuildConfig.DEBUG) {
                Toast.makeText(this, "※開発版しか表示されません\n" +
                        "GPSを起動しました。", Toast.LENGTH_SHORT).show();
            }
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        } catch (SecurityException e) {
            LOGE(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        hideProgress();
        LOGD(TAG, "GPS:location changed in BaseStoreActivity:" + location.getLatitude() + ":" + location.getLongitude());
        try {
            Location lastKnownLocation = null;
            if (locationManager != null) {
                this.locationManager.getLastKnownLocation(location.getProvider());
            }
            boolean isBetterLocation = GpsService.isBetterLocation(location, lastKnownLocation);
            LOGD(TAG, "isBetterLocation:" + isBetterLocation);
            if (isBetterLocation) {
                currentLocation = new MyLocation(location.getLatitude(), location.getLongitude());
            } else {
                currentLocation = new MyLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            }
            AppController.getInstance().updateMyLocation(currentLocation);
            if (BuildConfig.DEBUG) {
                Toast.makeText(this, "※開発版しか表示されません\n" +
                        "位置情報が取得できました", Toast.LENGTH_SHORT).show();
            }
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
            if (!isReadyForUseLocation) {
                readyForUseLocation(currentLocation);
                isReadyForUseLocation = true;
            }
        } catch (SecurityException e) {

            e.printStackTrace();
            currentLocation = null;
            cannotUseLocation();
        } finally {
            if (locationManager != null) {
                locationManager = null;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private boolean isReadyForUseLocation = false;

    abstract protected void readyForUseLocation(MyLocation currentLocation);

    abstract protected void cannotUseLocation();

    private boolean didShowLocationTutorial() {
        String key = getString(R.string.didShowCheckinTutorial);
        boolean didShowLocationTutorial = preferences.getBoolean(key, false);
        return didShowLocationTutorial;

    }

    protected void showTutorial() {
        String key = getString(R.string.didShowCheckinTutorial);
        if (getSupportFragmentManager().findFragmentByTag(ScreenTutorialFragment.TAG) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentByTag(ScreenTutorialFragment.TAG))
                    .commit();
        }
        preferences.edit().putBoolean(key, true).commit();
        String[] fileNames = new String[]{"checkin_tutorial"};
        int[] stringResIds = new int[]{R.string.text_checkin_description_description};
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.drawer_layout, ScreenTutorialFragment.newInstance(fileNames, stringResIds), ScreenTutorialFragment.TAG)
                .commit();
    }

    @Override
    public void onScreenTutorialClosed() {
        requestLocationPermission();
    }

    protected void requestLocationPermission() {
        this.allowLocationAccess = GpsUtils.allowAccessLocation(this, null);
        LOGD(TAG, "allowLocationAccess:" + allowLocationAccess);
        if (allowLocationAccess) {
            // 位置情報が許可されている場合、サービスを起動する
            startLocationServiceIfLocationAllowd();

            createLocationManager();
            // GPSは許可されているが、GPSがONかどうか
            if (!GpsUtils.allowAccessLocation(this, locationManager)) {
                // GPSが無効になっている
                AppController.getInstance().showAlert(BaseStoreActivity.this, getString(R.string.error), getString(R.string.text_location_authorize_deny));
                cannotUseLocation();
            } else {
                // 全部OKの場合は、onLocationChangeが制御する
                showNetworkProgress();
                startGpsRequest();
            }
        } else {
            // 位置情報の許可ダイアログを表示する
            if (didShowLocationTutorial()) {
                LOGD(TAG, "位置情報きょかダイアログ");
                boolean shouldShowRequestPermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
                if (shouldShowRequestPermissionRationale) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            } else {
                showTutorial();
            }
        }
    }

    protected List<Integer> getCheckinableStoreIds(MyLocation location) {
        if (location == null) {
            return new ArrayList<>();
        }
        showNetworkProgress();
        List<Integer> storeIds = new ArrayList<>();
        SQLiteDatabase rdb = AppController.getInstance().getRDB();
        Cursor c = null;
        try {
            c = rdb.rawQuery("SELECT " + Store.KEYS.STORE_ID + "," + Store.KEYS.LATITUDE + "," + Store.KEYS.LONGITUDE + " FROM " + Store.TABLE_NAME, null);
            while (c.moveToNext()) {
                String storeId = c.getString(c.getColumnIndex(Store.KEYS.STORE_ID));
                double slatitude = Double.valueOf(c.getString(c.getColumnIndex(Store.KEYS.LATITUDE)));
                double slongitude = Double.valueOf(c.getString(c.getColumnIndex(Store.KEYS.LONGITUDE)));

                float[] results = new float[3];
                Location.distanceBetween(slatitude, slongitude, location.getLatitude(), location.getLongitude(), results);
                float distance = results[0];
//                LOGD(TAG, "distance:"+distance);
                if (distance <= CAN_CHECKIN_DISTANCE) {
                    storeIds.add(Integer.parseInt(storeId));
                }
            }
        } catch (SQLiteException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        for (int i = 0; i < storeIds.size(); i++) {
            LOGD(TAG, "can checkin storeID:" + storeIds.get(i));
        }
        hideProgress();
        return storeIds;
    }

    protected void getStores() {
        if (getSupportFragmentManager().findFragmentByTag(StoresDownloadFragment.TAG) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentByTag(StoresDownloadFragment.TAG))
                    .commit();
        }
        showNetworkProgress();
        StoresDownloadFragment storesDownloadFragment = StoresDownloadFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(storesDownloadFragment, StoresDownloadFragment.TAG)
                .commit();
    }

    @Override
    public void onStoresDownloadFinish() {
        hideProgress();
        haveStores = self.haveStores();
        LOGD(TAG, "have stores:" + haveStores);
        requestLocationPermission();
    }

    @Override
    public void onStoresDownloadFail() {
        hideProgress();
        haveStores = self.haveStores();
        LOGD(TAG, "have stores:" + haveStores);
        requestLocationPermission();
    }

    private void startLocationServiceIfLocationAllowd() {
        // 位置情報が許可されている場合、サービスを起動する
//        if (!GpsService.isRunning(this) && GpsUtils.allowAccessLocation(this, locationManager)) {
//            stopService(new Intent(this, GpsService.class));
//            startService(new Intent(this, GpsService.class));
//        }
//        LOGD(TAG, "GPSService:" + GpsService.isRunning(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            LOGD(TAG, "request permissionsResult");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            } else {
                cannotUseLocation();
            }
        }
    }
}
