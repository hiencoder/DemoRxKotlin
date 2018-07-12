package com.tabio.tabioapp.store;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.mugen.Mugen;
import com.mugen.MugenCallbacks;
import com.mugen.attachers.BaseAttacher;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.MyMenuBus;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.checkin.MapActivity;
import com.tabio.tabioapp.me.MyMenu;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.ui.RecyclerItemDividerDecoration;
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
import butterknife.Unbinder;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class StoresFragment extends BaseFragment implements OnMapReadyCallback, StoreListDataAdapter.OnStoreListDataAdapterCallbacks {
    public static final String TAG = makeLogTag(StoresFragment.class);

    private StoreListDataAdapter adapter;
    private BaseAttacher attacher;
    private boolean showMap = false;
    private boolean checkin = false;

    private String noDataTxt = "";
    private ApiRequest request;
    private ApiParams params;
    private int index = 0;
    private int total = 0;
    private static final int COUNT = 20;

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    private MyMenu myMenu;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    public StoresFragment() {
        this.request = new ApiRequest(getActivity());
    }

    public static StoresFragment newInstance(ApiParams params, boolean showMap, boolean checkin, String noDataTxt) {
        StoresFragment fragment = new StoresFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        args.putBoolean("showMap", showMap);
        args.putBoolean("checkin", checkin);
        args.putString("noDataTxt", noDataTxt);
        fragment.setArguments(args);
        return fragment;
    }

    public static StoresFragment newInstance(ApiParams params, boolean showMap, boolean checkin, MyMenu myMenu, String noDataTxt) {
        StoresFragment fragment = new StoresFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        args.putBoolean("showMap", showMap);
        args.putBoolean("checkin", checkin);
        args.putSerializable("myMenu", myMenu);
        args.putString("noDataTxt", noDataTxt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.params = (ApiParams) getArguments().getSerializable("params");
        this.showMap = getArguments().getBoolean("showMap", false);
        this.checkin = getArguments().getBoolean("checkin", false);
        this.myMenu = (MyMenu) getArguments().getSerializable("myMenu");
        this.noDataTxt = getArguments().getString("noDataTxt", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stores, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new StoreListDataAdapter(getActivity(), showMap, checkin, this);
        this.recyclerView.setHasFixedSize(false);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.recyclerView.addItemDecoration(new RecyclerItemDividerDecoration(getContext(), R.drawable.line_divider));
        this.recyclerView.setAdapter(adapter);
        this.attacher = Mugen.with(this.recyclerView, new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                LOGD(TAG, "loadMore");
                search();
                attacher.setLoadMoreEnabled(false);
            }

            @Override
            public boolean isLoading() {
                return false;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return false;
            }
        }).start();
        this.attacher.setLoadMoreOffset(6);

        search();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        if (context instanceof OnStoresFragmentCallbacks) {
//            callbacks = (OnStoresFragmentCallbacks) context;
//
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnStoresFragmentCallbacks");
//        }
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
        LOGD(TAG, "setMap:"+adapter.getStores().size());

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                LOGD(TAG, "map clicked");
                Intent view = new Intent(getActivity(), MapActivity.class);
                String[] ids = new String[adapter.getStores().size()];
                for (int i = 0; i < adapter.getStores().size(); i++) {
                    ids[i] = adapter.getStores().get(i).getStoreId();
                }
                view.putExtra("ids", ids);
                startActivity(view);
            }
        });
        MapSettings.setCurrentLocation(map, self.getLatitude(), self.getLongitude(), null);
        MapSettings.setStoreMarker(map, adapter.getStores());
    }

    @Override
    public void onActionButtonClicked(final Store store) {
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_STORE);
        HashMap<String/*Key*/, List/*Arrayで送りたいやつ*/> dict = new HashMap<>();
        List<Integer> storeIds = Arrays.asList(Integer.valueOf(store.getStoreId()));
        dict.put("store", storeIds);
        params.put(store.isFavorite()?"delete":"insert", dict);

        showNetworkProgress();
        this.request.run(params)
            .observeOn(Schedulers.newThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe(new Observer<ApiResponse>() {
                @Override
                public void onCompleted() {
                    hideProgress();
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    LOGE(TAG, e.getMessage());
                    hideProgress();
                }

                @Override
                public void onNext(ApiResponse apiResponse) {
                    try {
                        JSONObject result = apiResponse.getBody().getJSONObject("result");
                        int beforePiece = result.getInt("before_piece");
                        int afterPiece = result.getInt("after_piece");
                        int point = result.getInt("point");
                        int gotPiece = afterPiece - beforePiece;
                        if (!apiResponse.hasError()) {
                            store.setFavorite(!store.isFavorite());
                            if (myMenu != null && !store.isFavorite()) {
                                adapter.getStores().remove(store);
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        LOGE(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
    }

    @Override
    public void onStoreItemClicked(Store store) {
        Intent view = new Intent(getActivity(), StoreActivity.class);
        view.putExtra("store", store);
        startActivity(view);
    }

    private void reset() {
        this.adapter.getStores().clear();
        this.index = 0;
        this.total = 0;
        refresh();
    }

    public void reload(ApiParams params, boolean reset) {
        if (reset) {
            reset();
        }
        this.params = params;
        search();
    }

    public void refresh() {
        if (this.adapter != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    attacher.setLoadMoreEnabled(true);
                }
            });
        }
    }

    private void search() {
        if (index > 0 && this.adapter.getStores().size() == total) {
            LOGD(TAG, "no data");
            return;
        }
        if (this.adapter.getStores().size() == 0) {
            reset();
        }
        this.index++;
        this.params.put("count", COUNT);
        this.params.put("index", this.index);
        showNetworkProgress();
        this.request.run(this.params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        refresh();
                        hideProgress();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mapFragment != null && map != null) {
                                    setMap(map);
                                }
                            }
                        });

                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO
                        LOGE(TAG, "stores error: " + e.getMessage());
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            if (apiResponse.hasError()) {
                                AppController.getInstance().showApiErrorAlert(getActivity(), apiResponse.getError());
                                return;
                            }
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            total = result.getInt("total");
                            if (myMenu != null) {
                                myMenu.setObjectsCount(total);
                                MyMenuBus.get().post(myMenu);
                            }
                            LOGD(TAG, "total:"+total);
                            JSONArray objects = result.getJSONArray(checkin?"history":"store");
                            if (objects.length() < 1) {
                                if (noDataTxt.isEmpty()) {
                                    showNoDataView(R.string.text_store);
                                } else {
                                    showNoDataView(getString(R.string.text_no_store_search));
                                }
                            } else {
                                hideNoDataView();
                            }
                            for (int i = 0; i < objects.length(); i++) {
                                JSONObject json = objects.getJSONObject(i);
                                Store store = new Store(json);
                                adapter.add(store);
                                refresh();
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}