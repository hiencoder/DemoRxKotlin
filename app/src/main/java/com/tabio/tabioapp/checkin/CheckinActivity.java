package com.tabio.tabioapp.checkin;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.gps.MyLocation;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.store.CheckinFragment;
import com.tabio.tabioapp.store.StoreActivity;
import com.tabio.tabioapp.store.StoreFilter;
import com.tabio.tabioapp.store.StoreFilterActivity;
import com.tabio.tabioapp.store.StoresActivity;
import com.tabio.tabioapp.tutorial.ScreenTutorialFragment;
import com.tabio.tabioapp.ui.BaseStoreActivity;
import com.tabio.tabioapp.ui.RecyclerItemDividerDecoration;
import com.tabio.tabioapp.util.GpsUtils;
import com.tabio.tabioapp.util.MapSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class CheckinActivity extends BaseStoreActivity implements
        OnMapReadyCallback, CheckinBaseAdapter.OnCheckinBaseViewCallbacks, GoogleMap.InfoWindowAdapter,
        CheckinFragment.OnCheckinCallbacks {
    private static final String TAG = makeLogTag(CheckinActivity.class);

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    private ApiRequest request;

    private StoreFilter storeFilter;

    private CheckinBaseAdapter adapter;

    private List<Store> checkinableStores;
    private List<Store> nearbyStores;

    private static final int NEARBY_STORES_COUNT = 3;
    public static final int REQUEST_SEARCH_FILTER = 200;

    @BindView(R.id.base_view)
    RecyclerView baseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        AppController.getInstance().sendGAScreen("チェックイン");
        AppController.getInstance().decideTrack("570f2d2499c3634a425af4cc");
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(R.string.text_checkin_title);
        this.request = new ApiRequest(this);
        this.storeFilter = new StoreFilter(this);

        this.adapter = new CheckinBaseAdapter(this, this);
        this.checkinableStores = new ArrayList<>();
        this.nearbyStores = new ArrayList<>();
        this.baseList.setHasFixedSize(false);
        this.baseList.setLayoutManager(new LinearLayoutManager(this));
        this.baseList.addItemDecoration(new RecyclerItemDividerDecoration(this, R.drawable.line_divider));
        this.baseList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void initializedMap(SupportMapFragment map) {
        this.mapFragment = map;
        this.mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
       setMap(map);
    }

    private void setMap(GoogleMap map) {
        this.map = map;
        this.map.clear();
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                LOGD(TAG, "map clicked");
                Intent view = new Intent(CheckinActivity.this, MapActivity.class);
                String[] ids = new String[nearbyStores.size()];
                for (int i = 0; i < nearbyStores.size(); i++) {
                    ids[i] = nearbyStores.get(i).getStoreId();
                }
                view.putExtra("ids", ids);
                startActivity(view);
            }
        });

        MapSettings.setCurrentLocation(map, self.getLatitude(), self.getLongitude(), null);
        MapSettings.setStoreMarker(map, nearbyStores);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }


    private void updateAdapter() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                if (mapFragment != null && map != null) {
                    setMap(map);
                }
            }
        });
    }

    private boolean isShowingNoCheckinView = false;

    private void showNoCheckinView() {
        if (isShowingNoCheckinView) {
            LOGE(TAG, "already showing no checkinView");
            return;
        }
        isShowingNoCheckinView = true;
        this.adapter.getObjects().add(1, new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_NO_CHECKINABLE_STORE, ""));
        this.adapter.getObjects().add(2, new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_CHECKIN_HISTORIES, getString(R.string.text_checkin_menu_title_checkinHistories)));
        updateAdapter();
    }

    private void showMapView(boolean reloadAdapter) {
        this.adapter.getObjects().add(new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_MAP));
        if (reloadAdapter) {
            updateAdapter();
        }
    }

    private void showCheckinableStoresAndNeabyStoresView() {
        {
            // チェックイン可能店舗
            if (checkinableStores.size() > 0) {
                adapter.getObjects().add(new CheckinBaseViewModel(
                        CheckinBaseViewModel.VIEW_TYPE_TITLE_CAN_CHECKIN, getString(R.string.text_checkin_menu_title_checkinableStores)));
                for (Store store : checkinableStores) {
                    adapter.getObjects().add(new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_CHECKINABLE_STORE, store));
                }
                adapter.getObjects().add(new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_CHECKIN_HISTORIES, getString(R.string.text_checkin_menu_title_checkinHistories)));
            } else {
                showNoCheckinView();
            }
        }
        {
            // 近隣店舗
            adapter.getObjects().add(new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_GRAY_BG, ""));
            adapter.getObjects().add(new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_TITLE_NEARBY_STORES, getString(R.string.text_checkin_menu_title_nearbyStores)));
            for (Store store : nearbyStores) {
                adapter.getObjects().add(new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_NEARBY_STORE, store));
            }
            adapter.getObjects().add(new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_NEARBY_STORES, getString(R.string.text_checkin_menu_title_nearbyStores)));
            adapter.getObjects().add(new CheckinBaseViewModel(CheckinBaseViewModel.VIEW_TYPE_VIEW_GRAY_BG, ""));
        }
        updateAdapter();
    }

    @Override
    protected void readyForUseLocation(MyLocation currentLocation) {
        showMapView(true);
        getCheckinableStores();
    }

    @Override
    protected void cannotUseLocation() {
        // デフォルトの位置情報を出すだけ
        showMapView(false);
        showNoCheckinView();
    }

    private void getNearbyStores() {
        LOGD(TAG, "getNearbyStores");
        ApiParams params = new ApiParams(self, true, false, ApiRoute.GET_STORES);
        params.put("search", 0);
        params.put("index", 1);
        params.put("count", NEARBY_STORES_COUNT);
        params.put("latitude", currentLocation.getLatitude());
        params.put("longitude", currentLocation.getLongitude());

        showNetworkProgress();
        this.request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        AppController.getInstance().showApiErrorAlert(CheckinActivity.this, ApiError.newNetworkErrorApiError());
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        hideProgress();
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showApiErrorAlert(CheckinActivity.this, apiResponse.getError());
                            return;
                        }
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            JSONArray stores = result.getJSONArray("store");
                            for (int i = 0; i < stores.length(); i++) {
                                Store store = new Store(stores.getJSONObject(i));
                                nearbyStores.add(store);
                            }
                            showCheckinableStoresAndNeabyStoresView();
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            nearbyStores.clear();
                            AppController.getInstance().showApiErrorAlert(CheckinActivity.this, apiResponse.getError());
                        }
                    }
                });
    }

    private void getCheckinableStores() {
        LOGD(TAG, "getCheckinableStores");
        ApiParams params = new ApiParams(self, true, false, ApiRoute.GET_STORES);

        final List<Integer> storeIds = getCheckinableStoreIds(currentLocation);
        LOGD(TAG, "checkinable store ids:" + storeIds.toString());
        if (storeIds.size() > 0) {
            HashMap<String/*Key*/, List/*Arrayで送りたいやつ*/> dict = new HashMap<>();
            dict.put("id", storeIds);
            params.put("store", dict);
        } else {
            LOGE(TAG, "no checkinable stores id");
            getNearbyStores();
            return;
        }
        params.put("search", 0);
        params.put("index", 1);
        params.put("count", 10000);
        params.put("exclude", 1);
        params.put("latitude", currentLocation.getLatitude());
        params.put("longitude", currentLocation.getLongitude());
        showNetworkProgress();
        this.request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                               @Override
                               public void onCompleted() {
                               }

                               @Override
                               public void onError(Throwable e) {
                                   hideProgress();
                                   showNoCheckinView();
                               }

                               @Override
                               public void onNext(ApiResponse apiResponse) {
                                   hideProgress();
                                   if (apiResponse.hasError()) {
                                       showNoCheckinView();
                                       AppController.getInstance().showApiErrorAlert(CheckinActivity.this, apiResponse.getError());
                                       return;
                                   }
                                   try {
                                       JSONObject result = apiResponse.getBody().getJSONObject("result");
                                       JSONArray stores = result.getJSONArray("store");
                                       if (stores.length() < 1) {
                                           showNoCheckinView();
                                           getNearbyStores();
                                           return;
                                       }
                                       for (int i = 0; i < stores.length(); i++) {
                                           Store store = new Store(stores.getJSONObject(i));
                                           checkinableStores.add(store);
                                       }
                                       getNearbyStores();
                                   } catch (JSONException e) {
                                       LOGE(TAG, e.getMessage());
                                       e.printStackTrace();
                                       checkinableStores.clear();
                                       showNoCheckinView();
                                   }
                               }
                           }

                );
    }

    public void showStore(Store store) {
        LOGD(TAG, store.getNameWithBrand());
        Intent view = new Intent(this, StoreActivity.class);
        view.putExtra("store", store);
        startActivity(view);
    }

    @Override
    public void onCheckinClicked(Store store) {
        if (getSupportFragmentManager().findFragmentByTag(CheckinFragment.TAG) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentByTag(CheckinFragment.TAG))
                    .commit();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_content, CheckinFragment.newInstance(store), CheckinFragment.TAG)
                .commit();
    }

    @Override
    public void onCheckinSuccess(Store store) {
        LOGD(TAG, "onCheckinSuccess");
        AppController.getInstance().decideTrack("570f2da599c3634a425af4d8");
        if (adapter != null && adapter.getObjects() != null) {
            for (int i = 0; i < adapter.getObjects().size(); i++) {
                if (adapter.getObjects().get(i).getStore() != null) {
                    if (adapter.getObjects().get(i).getStore().equals(store)) {
                        adapter.getObjects().remove(i);
                        checkinableStores.remove(store);
                        LOGD(TAG, "checkinableStores:" + checkinableStores.size());
                        LOGD(TAG, "showNoCheckinView:" + isShowingNoCheckinView);
                        // TODO
                        if (checkinableStores.size() < 1) {
                            adapter.getObjects().remove(1);//チェックイン可能店舗のタイトル
                            adapter.getObjects().remove(i - 1);//チェックイン履歴
//                            for (CheckinBaseViewModel model : this.adapter.getObjects()) {
//                                int type = model.getType();
//                                if (type == CheckinBaseViewModel.VIEW_TYPE_TITLE_CAN_CHECKIN
//                                        || type == CheckinBaseViewModel.VIEW_TYPE_VIEW_CHECKIN_HISTORIES) {
//                                    this.adapter.getObjects().remove(model);
//                                }
//                            }
                            showNoCheckinView();
                        }
                        updateAdapter();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onCheckinFail(Store store) {
        LOGD(TAG, "onCheckinFail");
    }

    @Override
    public void onStoreClicked(Store store) {
        showStore(store);
    }

    @Override
    public void onFavoriteButtonClicked(final Store store) {
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_STORE);
        HashMap<String, List> dict = new HashMap<>();
        List<Integer> storeIds = Arrays.asList(Integer.valueOf(store.getStoreId()));
        dict.put("store", storeIds);
        params.put(store.isFavorite() ? "delete" : "insert", dict);

        showNetworkProgress();
        this.request.run(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showApiErrorAlert(CheckinActivity.this, apiResponse.getError());
                            hideProgress();
                            return;
                        }
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            int beforePiece = result.getInt("before_piece");
                            int afterPiece = result.getInt("after_piece");
                            int point = result.getInt("point");
                            int gotPiece = afterPiece - beforePiece;
                            if (!apiResponse.hasError()) {
                                store.setFavorite(!store.isFavorite());
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onHistoryButtonClicked() {
        Intent view = new Intent(this, StoresActivity.class);
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_CHECKIN_HISTORIES);
        AppController.getInstance().decideTrack("570f2dbe99c3634a425af4dc");
        if (currentLocation != null) {
            params.put("latitude", currentLocation.getLatitude());
            params.put("longitude", currentLocation.getLongitude());
        }
        view.putExtra("title", getString(R.string.text_checkin_menu_title_checkinHistories));
        view.putExtra("params", params);
        view.putExtra("showMap", false);
        view.putExtra("showDistance", currentLocation != null);
        view.putExtra("checkin", true);
        view.putExtra("url", ApiRoute.GET_CHECKIN_HISTORIES);
        AppController.getInstance().sendGAScreen("チェックイン履歴一覧");
        startActivity(view);
    }

    @Override
    public void onNearbyStoresButtonClicked() {
        Intent view = new Intent(this, StoresActivity.class);
        ApiParams params = new ApiParams(self, true, true, ApiRoute.GET_STORES);
        params.put("search", 0);
        view.putExtra("title", getString(R.string.text_checkin_menu_title_nearbyStores));
        view.putExtra("params", params);
        view.putExtra("showMap", true);
        view.putExtra("showDistance", currentLocation != null);
        view.putExtra("url", ApiRoute.GET_STORES);
        AppController.getInstance().sendGAScreen("近隣店舗一覧");
        AppController.getInstance().decideTrack("570f2dd399c3634a425af4e0");
        startActivity(view);
    }

    private void search() {
        Intent view = new Intent(this, StoresActivity.class);
        ApiParams params = new ApiParams(self, true, true, ApiRoute.GET_STORES);
        params = this.storeFilter.addFilterParams(params);
        params.put("search", 0);
        AppController.getInstance().decideTrack("570f2dca99c3634a425af4de");

        view.putExtra("title", getString(R.string.text_store_result_title));
        view.putExtra("params", params);
        view.putExtra("showMap", false);
        view.putExtra("showDistance", currentLocation != null);
        view.putExtra("url", ApiRoute.GET_STORES);
        view.putExtra("noDataTxt", getString(R.string.text_no_store_search));
        AppController.getInstance().sendGAScreen("店舗検索結果一覧");
        startActivity(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SEARCH_FILTER && resultCode == RESULT_OK) {
            if (data.getSerializableExtra("filter") != null) {
                this.storeFilter = (StoreFilter) data.getSerializableExtra("filter");
                search();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                AppController.getInstance().decideTrack("570f2dca99c3634a425af4de");
                Intent view = new Intent(this, StoreFilterActivity.class);
                view.putExtra("filter", this.storeFilter);
                startActivityForResult(view, REQUEST_SEARCH_FILTER);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.checkin, menu);
        return true;
    }
}
