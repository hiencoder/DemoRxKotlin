package com.tabio.tabioapp.util;

import android.content.Context;

import com.google.android.gms.ads.internal.client.ThinAdSizeParcel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Store;

import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;

/**
 * Created by san on 4/6/16.
 */
public class MapSettings {

    public static final double DEFAULT_LATITUDE = 35.658781;
    public static final double DEFAULT_LONGITUDE = 139.705258;

    public static void setStoreMarker(GoogleMap map, Store store) {
        LatLng latlng = new LatLng(store.getLatitude(), store.getLongitude());
        Marker marker = map.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_checkin_red_map)).title(store.getNameWithBrand()));
        marker.showInfoWindow();
    }

    public static void setStoreMarker(GoogleMap map, List<Store> stores) {
        if (stores == null) {
            return;
        }
        for (Store store : stores) {
            setStoreMarker(map, store);
        }
    }

    public static void setCurrentLocation(GoogleMap map, double currentLatitude, double currentLongitude, LatLng targetLatLng) {
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);

        // 距離がない時はTabioの東京支店をデフォルトで出す
        if (currentLatitude <= 0.0 && currentLongitude <= 0.0) {
            currentLatitude = DEFAULT_LATITUDE;
            currentLongitude = DEFAULT_LONGITUDE;
        }
        LatLng currentLocationLatlng = new LatLng(currentLatitude, currentLongitude);
        // 現在地
        {
            Marker currentMarker = map.addMarker(new MarkerOptions().position(currentLocationLatlng).icon((BitmapDescriptorFactory.fromResource(R.drawable.ic_map_gps_gray))));
            currentMarker.showInfoWindow();
        }
        float zoom = 13.f;
        float tilt = 0.0f;
        float bearing = 0.0f;
        CameraPosition pos = new CameraPosition(targetLatLng == null ? currentLocationLatlng : targetLatLng, zoom, tilt, bearing);
        CameraUpdate camera = CameraUpdateFactory.newCameraPosition(pos);
        map.moveCamera(camera);
    }
}
