package com.tabio.tabioapp.checkin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.gps.MyLocation;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.ui.BaseStoreActivity;
import com.tabio.tabioapp.util.MapSettings;

import java.util.ArrayList;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class MapActivity extends BaseStoreActivity implements OnMapReadyCallback {
    public static final String TAG = makeLogTag(MapActivity.class);

    private List<Store> stores;
    private String currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        AppController.getInstance().sendGAScreen("マップ拡大");
        AppController.getInstance().decideTrack("570f2df899c3634a425af4e4");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map, mapFragment, CheckinBaseAdapter.TAG)
                .commit();

        if (getIntent().getStringExtra("currentLocation") != null) {
            currentLocation = getIntent().getStringExtra("currentLocation");
        }

        this.stores = new ArrayList<>();
        for (String id : getIntent().getStringArrayExtra("ids")) {
            this.stores.add(new Store(id));
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        setMap(map);
    }

    private void setMap(GoogleMap map) {
        map.clear();
        if (currentLocation != null && currentLocation.equals("store") && stores.size() == 1) {
            MapSettings.setCurrentLocation(map, self.getLatitude(), self.getLongitude(), new LatLng(stores.get(0).getLatitude(), stores.get(0).getLongitude()));
        } else {
            MapSettings.setCurrentLocation(map, self.getLatitude(), self.getLongitude(), null);
        }
        MapSettings.setStoreMarker(map, stores);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setTiltGesturesEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
    }

    @Override
    protected void readyForUseLocation(MyLocation currentLocation) {

    }

    @Override
    protected void cannotUseLocation() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LOGD(TAG, "clicked home button");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
