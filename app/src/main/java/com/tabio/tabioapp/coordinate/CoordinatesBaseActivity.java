package com.tabio.tabioapp.coordinate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.item.SwipeableItemsActivity;
import com.tabio.tabioapp.model.Coordinate;
import com.tabio.tabioapp.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/2/16.
 */
public abstract class CoordinatesBaseActivity extends BaseActivity implements CoordinateCallbacks.OnCoordinateCardViewCallbacks {
    public static final String TAG = makeLogTag(CoordinatesBaseActivity.class);

    private List<Coordinate> coordinates;

    protected ApiRequest apiRequest;

    protected int index = 0;
    protected int total = 0;
    public static final int COUNT = 20;

    public static final String COORDINATE_SKIP_MIN = "COORDINATE_SKIP_MIN";
    public static final String COORDINATE_SKIP_MAX = "COORDINATE_SKIP_MAX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.apiRequest = new ApiRequest(this);
        this.coordinates = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        this.index = 0;

    }

    @Override
    public void onCoordinateCardItemClicked(View view, Coordinate coordinate, int position) {
        int id = view.getId();
        if (id == R.id.favorite_button) {
            // お気に入り
            LOGD(TAG, "favorite button clicked");
//            this.collectionView.discardTop(3);
            favorite(coordinate);
        } else if (id == R.id.share_button) {
            // シェア
            LOGD(TAG, "share button clicked");
            Intent v = new Intent();
            v.setAction(Intent.ACTION_SEND);
            v.setType("text/plain");
            v.putExtra(Intent.EXTRA_TEXT, coordinate.getShareurl());
            startActivity(v);
        }
    }

    public void showItem(List<Coordinate> coordinates, int position) {
        Intent view = new Intent(this, SwipeableItemsActivity.class);
        view.putExtra("position", position);
        int[] productIds = new int[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            productIds[i] = coordinates.get(i).getProductId();
        }
        view.putExtra("ids", productIds);
        for (int pid : productIds) {
            LOGD(TAG, "pid:"+pid);
        }
        startActivity(view);
    }

    abstract protected void onLoadFinishedCoordinatesData(List<Coordinate> coordinates);

    protected void refreshCoordinatesData(@Nullable String jan) {
        refreshCoordinatesData(jan, 0, 0);
    }

    protected void refreshCoordinatesData(@Nullable String jan, int skipMin, int skipMax) {
        if (index > 0 && this.coordinates.size() == total) {
            LOGD(TAG, "no data");
            return;
        }
        if (this.coordinates.size() == 0) {
//            reset();
        }
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_COORDINATE);
        this.index ++;
        params.put("index", this.index);
        params.put("count", COUNT);
        params.put("search", 0);
        if (jan != null) {
            params.put("jan", jan);
        } else {
//            int skipMax = this.preferences.getInt(COORDINATE_SKIP_MAX, 0);
//            if (skipMax > 0 && index <= 1) {
//                params.put("skip_max", skipMax);
//            }
        }
//        params.put("skip_min", this.preferences.getInt(COORDINATE_SKIP_MIN, 0));
        if (skipMax > 0) {
            params.put("skip_max", skipMax);
        }
        if (skipMin > 0) {
            params.put("skip_min", skipMin);
        }
        showNetworkProgress();
        this.apiRequest.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
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
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showApiErrorAlert(CoordinatesBaseActivity.this, apiResponse.getError());
                            hideProgress();
                            return;
                        }
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            total = result.getInt("total");
                            LOGD(TAG, "total:"+total);
                            final JSONArray objects = result.getJSONArray("coordinate");
                            try {
                                final List<Coordinate> list = new ArrayList<Coordinate>();
                                for (int i = 0; i < objects.length(); i++) {
                                    list.add(new Coordinate(objects.getJSONObject(i)));
                                }
                                coordinates.addAll(list);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onLoadFinishedCoordinatesData(list);
                                    }
                                });
                            } catch (JSONException e) {
                                LOGE(TAG, e.getMessage());
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    protected abstract void favoriteFinished(Coordinate coordinate);

    protected void favorite(final Coordinate coordinate) {
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_COORDINATE);
        HashMap<String, List> dict = new HashMap<>();
        List<Integer> coordinateIds = Arrays.asList(Integer.valueOf(coordinate.getCoordinateId()));
        dict.put("coordinate", coordinateIds);
        params.put(coordinate.isFavorite() ? "delete" : "insert", dict);
        if (!coordinate.isFavorite()) {
            AppController.getInstance().sendGAEvent("Coordinate","Favorite",coordinate.getStoreName(),(long) coordinate.getCoordinateId());
        }

//        showNetworkProgress();
        this.apiRequest.run(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
//                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
//                        hideProgress();
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
                                coordinate.setFavorite(!coordinate.isFavorite());
                                coordinate.setFavoriteCount(coordinate.getFavoriteCount() + (coordinate.isFavorite() ? +1 : -1));
                                favoriteFinished(coordinate);
                            }

                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

}
