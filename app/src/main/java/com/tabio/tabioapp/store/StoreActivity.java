package com.tabio.tabioapp.store;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.blog.BlogsActivity;
import com.tabio.tabioapp.checkin.MapActivity;
import com.tabio.tabioapp.coordinate.CoordinatesActivity;
import com.tabio.tabioapp.item.SwipeableItemsActivity;
import com.tabio.tabioapp.model.Blog;
import com.tabio.tabioapp.model.Coordinate;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.util.MapSettings;
import com.tabio.tabioapp.web.WebActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 2/1/16.
 */
public class StoreActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, StoreDataAdapter.OnStoreDataAdapterCallbacks {
    public static final String TAG = makeLogTag(StoreActivity.class);

    private ApiRequest request;
    private StoreDataAdapter adapter;
    private Store store;
    private List<StoreViewModel> viewModels;

    private static final int BLOG_COUNT = 5;
    private static final int COORDINATE_COUNT = 4;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        AppController.getInstance().sendGAScreen("店舗詳細");
        AppController.getInstance().decideTrack("570f2de199c3634a425af4e2");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);
        this.request = new ApiRequest(this);

        if (getIntent().getSerializableExtra("store") != null) {
            this.store = (Store) getIntent().getSerializableExtra("store");
            getSupportActionBar().setTitle(this.store.getNameWithBrand());
        }

        this.viewModels = new ArrayList<>();
        this.recyclerView.setHasFixedSize(false);
        this.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        this.adapter = new StoreDataAdapter(this, this.store, viewModels, this, this);
        this.recyclerView.setAdapter(this.adapter);

        getStore();
    }

    private void showStoreInfo() {

        getSupportActionBar().setTitle(this.store.getNameWithBrand());
        viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_MAP));
        viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_BASIC_INFO));
        viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_TYPES));
        viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_ACTIONS));
        viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_ACCESS));
        this.adapter.updateStore(this.store, this.viewModels);

        getCoordinates();
    }

    private void getBlogs() {
        LOGD(TAG, "getBlogs");
        LOGD(TAG, "blogrss:" + this.store.getRssUrl());
        if (this.store.getRssUrl().equals("")) {
            viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_BLOG_TITLE));
            viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_NODATA,
                    getString(R.string.error_text_nodata,
                            getString(R.string.text_blog_title, store.getNameWithBrand()))));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.updateStore(store, viewModels);
                }
            });
            return;
        }
        showNetworkProgress();
        Request request = new Request.Builder()
                .addHeader("Content-Type", "text/xml")
                .url(this.store.getRssUrl())
                .get()
                .build();
        AppController.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
                hideProgress();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    store.setBlogs(Blog.getBlogs(response));
                    viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_BLOG_TITLE));
                    if (store.getBlogs().size() > 0) {
                        for (int i = 0; i < store.getBlogs().size(); i++) {
                            if (i < BLOG_COUNT) {// ここでの表示は5件なので
                                if (i == 0) {
                                    store.setBlogStartPosition(viewModels.size());
                                }
                                viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_BLOG));
                            }
                        }
                        viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_BLOG_MORE_BUTTON));
                    } else {
                        viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_NODATA,
                                getString(R.string.error_text_nodata,
                                        getString(R.string.text_blog_title, store.getNameWithBrand()))));
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.updateStore(store, viewModels);
                        }
                    });
                } catch (Exception e) {
                    LOGE(TAG, e.getMessage());
                    e.printStackTrace();
                } finally {
                    hideProgress();
                }
            }
        });
    }

    private void getCoordinates() {
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_COORDINATE);
        params.put("store", this.store.getStoreId());
        params.put("search", 0);
        params.put("index", 1);
        params.put("count", COORDINATE_COUNT);
        showNetworkProgress();
        this.request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.updateStore(store, viewModels);
                                getBlogs();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        LOGE(TAG, e.getMessage());
                        getBlogs();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            JSONArray jcoordinates = result.getJSONArray("coordinate");
                            viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_COORDINATE_TITLE));
                            if (jcoordinates.length() == 0) {
                                viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_NODATA,
                                        getString(R.string.error_text_nodata, getString(R.string.text_coordinate))));
                                return;
                            }
                            for (int i = 0; i < jcoordinates.length(); i++) {
                                if (i == 0) {
                                    store.setCoordinateStartPosition(viewModels.size());
                                }
                                Coordinate coordinate = new Coordinate(jcoordinates.getJSONObject(i));
                                StoreViewModel vm = new StoreViewModel(StoreViewModel.VIEW_TYPE_COORDINATE);
                                viewModels.add(vm);
                                store.getCoordinates().add(coordinate);
                            }
                            if (result.getInt("total") > COORDINATE_COUNT) {
                                viewModels.add(new StoreViewModel(StoreViewModel.VIEW_TYPE_COORDINATE_MORE_BUTTON));
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void getStore() {
        ApiParams params = new ApiParams(self, true, true, ApiRoute.GET_STORE);
        if (this.store != null) {
            params.put("store_id", Integer.parseInt(this.store.getStoreId()));
        } else {
            params.put("store_id", Integer.parseInt(getIntent().getStringExtra("store_id")));
        }
        showNetworkProgress();
        this.request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showStoreInfo();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showApiErrorAlert(StoreActivity.this, apiResponse.getError());
                            return;
                        }
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            store = new Store(result);
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            AppController.getInstance().showApiErrorAlert(StoreActivity.this, null);
                        }
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setInfoWindowAdapter(this);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent view = new Intent(StoreActivity.this, MapActivity.class);
                view.putExtra("currentLocation", "store");
                view.putExtra("ids", new String[]{store.getStoreId()});
                startActivity(view);
            }
        });
        MapSettings.setStoreMarker(map, this.store);
        MapSettings.setCurrentLocation(map, self.getLatitude(), self.getLongitude(), new LatLng(store.getLatitude(), store.getLongitude()));

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
//        return LayoutInflater.from(this).inflate(R.layout.map_info_window, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onGoogleMapOpenButtonClicked() {
        String escapedStoreName = "";
        try {
            escapedStoreName = URLEncoder.encode(store.getNameWithBrand(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String geo = "geo:0,0?q=" + store.getLatitude() + "," + store.getLongitude() + "(" + escapedStoreName + ")";
        Uri gmmIntentUri = Uri.parse(geo);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @Override
    public void onCallButtonClicked() {
        Uri uri = Uri.parse("tel:" + this.store.getTel());
        Intent i = new Intent(Intent.ACTION_DIAL, uri);
        startActivity(i);
    }

    @Override
    public void onStoreFavoriteButtonClicked() {
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_STORE);
        HashMap<String/*Key*/, List/*Arrayで送りたいやつ*/> dict = new HashMap<>();
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
    public void onCoordinateFavoriteButtonClicked(Coordinate coordinate, final int coordinatePosition) {
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_COORDINATE);
        HashMap<String, List> dict = new HashMap<>();
        List<Integer> coordinateIds = Arrays.asList(Integer.valueOf(coordinate.getCoordinateId()));
        dict.put("coordinate", coordinateIds);
        params.put(coordinate.isFavorite() ? "delete" : "insert", dict);

        showNetworkProgress();
        this.request.run(params).observeOn(AndroidSchedulers.mainThread())
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
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            int beforePiece = result.getInt("before_piece");
                            int afterPiece = result.getInt("after_piece");
                            int point = result.getInt("point");
                            int gotPiece = afterPiece - beforePiece;
                            if (!apiResponse.hasError()) {
                                Coordinate c = store.getCoordinates().get(coordinatePosition);
                                c.setFavorite(!c.isFavorite());
                                c.setFavoriteCount(c.getFavoriteCount() + (c.isFavorite() ? +1 : -1));
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
    public void onCoordinateItemClicked(Coordinate coordinate, int position) {
        Intent view = new Intent(this, SwipeableItemsActivity.class);
        view.putExtra("position", position);
        int[] productIds = new int[this.store.getCoordinates().size()];
        for (int i = 0; i < this.store.getCoordinates().size(); i++) {
            productIds[i] = this.store.getCoordinates().get(i).getProductId();
        }
        view.putExtra("ids", productIds);
        startActivity(view);
    }

    @Override
    public void onCoordinatesMoreButtonClicked() {
        Intent view = new Intent(this, CoordinatesActivity.class);
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_COORDINATE);
        params.put("search", 0);
        params.put("store", this.store.getStoreId());
        view.putExtra("params", params);
        startActivity(view);
    }

    @Override
    public void onBlogItemClicked(Blog blog) {
        Intent view = new Intent(this, WebActivity.class);
        view.putExtra("url", blog.getLink());
        startActivity(view);
    }

    @Override
    public void onBlogsMoreButtonClicked() {
        Intent view = new Intent(this, BlogsActivity.class);
        view.putExtra("store", this.store);
        startActivity(view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LOGD(TAG, "clicked home button");
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
