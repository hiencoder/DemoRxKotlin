package com.tabio.tabioapp.coordinate;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.tabio.tabioapp.item.ItemsActivity;
import com.tabio.tabioapp.item.SwipeableItemsActivity;
import com.tabio.tabioapp.me.MyMenu;
import com.tabio.tabioapp.model.Coordinate;
import com.tabio.tabioapp.ui.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class CoordinatesFragment extends BaseFragment implements CoordinateItemAdapter.OnCoordinateItemAdapterCallbacks {
    public static final String TAG = makeLogTag(CoordinatesFragment.class);

    private CoordinateItemAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private BaseAttacher attacher;

    private ApiRequest request;
    private ApiParams params;
    private int index = 0;
    private int total = 0;
    private static final int COUNT = 20;

    private MyMenu myMenu;

    @BindView(R.id.coordinates_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    public CoordinatesFragment() {
        this.request = new ApiRequest(getActivity());
    }

    public static CoordinatesFragment newInstance(ApiParams params) {
        CoordinatesFragment fragment = new CoordinatesFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        fragment.setArguments(args);
        return fragment;
    }

    public static CoordinatesFragment newInstance(ApiParams params, MyMenu myMenu) {
        CoordinatesFragment fragment = new CoordinatesFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        args.putSerializable("myMenu", myMenu);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.params = (ApiParams) getArguments().getSerializable("params");
        this.myMenu = (MyMenu) getArguments().getSerializable("myMenu");
        AppController.getInstance().decideTrack("570f2e8e99c3634a425af4e8");
        AppController.getInstance().sendGAScreen("コーディネート一覧");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coordinates, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        this.adapter = new CoordinateItemAdapter(getActivity(), this);
        this.recyclerView.setHasFixedSize(false);
        this.recyclerView.setLayoutManager(this.layoutManager);
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

//        if (context instanceof OnCoordinatesFragmentCallbacks) {
//            callbacks = (OnCoordinatesFragmentCallbacks) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnCoordinatesFragmentCallbacks");
//        }
    }

    private void reset() {
        this.adapter.getObjects().clear();
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
        if (index > 0 && this.adapter.getObjects().size() == total) {
            LOGD(TAG, "no data");
            return;
        }
        if (this.adapter.getObjects().size() == 0) {
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
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO
                        LOGE(TAG, "coordinates error: " + e.getMessage());
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
                            JSONArray product = result.getJSONArray("coordinate");
                            if (product.length() < 1) {
                                showNoDataView(R.string.text_coordinate);
                            } else {
                                hideNoDataView();
                            }
                            for (int i = 0; i < product.length(); i++) {
                                JSONObject json = product.getJSONObject(i);
                                Coordinate coordinate = new Coordinate(json);
                                adapter.add(coordinate);
                                refresh();
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void favorite(final Coordinate coordinate) {
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_COORDINATE);
        HashMap<String/*Key*/, List/*Arrayで送りたいやつ*/> dict = new HashMap<>();
        List<Integer> storeIds = Arrays.asList(coordinate.getCoordinateId());
        dict.put("coordinate", storeIds);
        params.put(coordinate.isFavorite() ? "delete" : "insert", dict);

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
                        LOGE(TAG, e.getMessage());
                        e.printStackTrace();
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
                                coordinate.setFavorite(!coordinate.isFavorite());
                                coordinate.setFavoriteCount(coordinate.getFavoriteCount() + (coordinate.isFavorite() ? +1 : -1));
                                if (myMenu != null && !coordinate.isFavorite()) {
                                    adapter.getObjects().remove(coordinate);
                                }
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
    public void onCoordinateItemClicked(Coordinate coordinate, int position) {
        Intent view = new Intent(getActivity(), SwipeableItemsActivity.class);
        view.putExtra("position", position);
        int[] productIds = new int[this.adapter.getObjects().size()];
        for (int i = 0; i < this.adapter.getObjects().size(); i++) {
            productIds[i] = this.adapter.getObjects().get(i).getProductId();
        }
        view.putExtra("ids", productIds);
        startActivity(view);
    }

    @Override
    public void onCoordinateFavoriteButtonClicked(Coordinate coordinate) {
        favorite(coordinate);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
