package com.tabio.tabioapp.item;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;
import com.mugen.attachers.BaseAttacher;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.MyMenuBus;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.item.adapter.ItemDataAdapter;
import com.tabio.tabioapp.me.MyMenu;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.ui.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ItemsFragment extends BaseFragment implements ItemDataAdapter.OnItemDataAdapterCallbacks {
    public static final String TAG = makeLogTag(ItemsFragment.class);

    private ItemDataAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private BaseAttacher attacher;

    private ApiRequest request;
    private ApiParams params;
    private int index = 0;
    private int total = 0;
    private static final int COUNT = 20;

    private MyMenu myMenu;
    private String noDataTxt;
    private String noDataTxt2;

    @BindView(R.id.items_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    private static final int REQUEST_ITEM = 130;

    public ItemsFragment() {
        this.request = new ApiRequest(getActivity());
    }

    public static ItemsFragment newInstance(ApiParams params, @Nullable String noDataTxt, @Nullable String noDataTxt2) {
        ItemsFragment fragment = new ItemsFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        args.putString("noDataTxt", noDataTxt);
        args.putString("noDataTxt2", noDataTxt2);
        fragment.setArguments(args);
        return fragment;
    }

    public static ItemsFragment newInstance(ApiParams params, MyMenu myMenu, @Nullable String noDataTxt, @Nullable String noDataTxt2) {
        ItemsFragment fragment = new ItemsFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        args.putSerializable("myMenu", myMenu);
        args.putString("noDataTxt", noDataTxt);
        args.putString("noDataTxt2", noDataTxt2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HashMap<String, Object> apiMap = (HashMap<String, Object>) getArguments().getSerializable("params");
        this.params = new ApiParams(self, apiMap, ApiRoute.GET_ITEMS);
        this.myMenu = (MyMenu) getArguments().getSerializable("myMenu");
        this.noDataTxt = getArguments().getString("noDataTxt");
        this.noDataTxt2 = getArguments().getString("noDataTxt2");
        AppController.getInstance().sendGAScreen("アイテム一覧");
        AppController.getInstance().decideTrack("570f2e9c99c3634a425af4ea");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        this.adapter = new ItemDataAdapter(getActivity(), this);
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
//        if (context instanceof OnItemsFragmentCallbacks) {
//            callbacks = (OnItemsFragmentCallbacks) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnItemsFragmentCallbacks");
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
        hideNoDataView();
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
                        LOGE(TAG, "items error: " + e.getMessage());
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            if (apiResponse.hasError()) {
                                AppController.getInstance().showApiErrorAlert(getActivity(), apiResponse.getError());
                                hideProgress();
                                return;
                            }
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            total = result.getInt("total");
                            if (myMenu != null) {
                                myMenu.setObjectsCount(total);
                                MyMenuBus.get().post(myMenu);
                            }
                            LOGD(TAG, "total:" + total);
                            JSONArray product = result.getJSONArray("product");
                            if (product.length() < 1) {
                                if (noDataTxt.isEmpty()) {
                                    showNoDataView(R.string.text_item);
                                } else {
                                    showNoDataView(noDataTxt, noDataTxt2);
                                }
                            } else {
                                hideNoDataView();
                            }
                            for (int i = 0; i < product.length(); i++) {
                                JSONObject json = product.getJSONObject(i);
                                Item item = new Item(json);
                                adapter.add(item);
                                refresh();
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            AppController.getInstance().showApiErrorAlert(getActivity(), ApiError.newUndefinedApiError());
                        }
                    }
                });
    }

    @Override
    public void onItemClicked(Item item, int position) {
        Intent view = new Intent(getActivity(), SwipeableItemsActivity.class);
        view.putExtra("position", position);
        int[] productIds = new int[this.adapter.getObjects().size()];
        for (int i = 0; i < this.adapter.getObjects().size(); i++) {
            productIds[i] = this.adapter.getObjects().get(i).getProductId();
        }
        view.putExtra("ids", productIds);
        startActivityForResult(view, REQUEST_ITEM);
    }

    private void favorite(final Item item) {
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_ITEM);
        HashMap<String/*Key*/, List/*Arrayで送りたいやつ*/> dict = new HashMap<>();
        List<Integer> storeIds = Arrays.asList(item.getProductId());
        dict.put("product", storeIds);
        params.put(item.isFavorite() ? "delete" : "insert", dict);
        AppController.getInstance().sendGAEvent("Item","Favorite","search",(long)item.getProductId());

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
                                item.setFavorite(!item.isFavorite());
                                item.setFavoriteCount(item.getFavoriteCount() + (item.isFavorite() ? +1 : -1));
                                if (myMenu != null && !item.isFavorite() && myMenu.getMyMenuId() != MyMenu.MYMENU_ID_ITEM_READ_HISTORIES) {
                                    adapter.getObjects().remove(item);
                                }
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
    public void onItemFavoriteButtonClicked(final Item item) {
        favorite(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ITEM && resultCode == Activity.RESULT_OK && data != null) {
//            int productId = data.getIntExtra("productId", 0);
            List<Integer> productIds = data.getIntegerArrayListExtra("productIds");
            if (productIds != null) {
                for (int i=0; i<adapter.getObjects().size(); i++) {
                    Item object = this.adapter.getObjects().get(i);
                    if (productIds.contains(object.getProductId())) {
                        object.setFavorite(!object.isFavorite());
                        object.setFavoriteCount(object.getFavoriteCount()+(object.isFavorite()?+1:-1));
                        if (myMenu != null && !object.isFavorite() && myMenu.getMyMenuId() != MyMenu.MYMENU_ID_ITEM_READ_HISTORIES) {
                            adapter.getObjects().remove(object);
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        }
    }
}
