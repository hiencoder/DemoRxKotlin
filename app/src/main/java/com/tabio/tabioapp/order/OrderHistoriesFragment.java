package com.tabio.tabioapp.order;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.tabio.tabioapp.me.MyMenu;
import com.tabio.tabioapp.model.Order;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.ui.RecyclerItemDividerDecoration;
import com.tabio.tabioapp.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class OrderHistoriesFragment extends BaseFragment implements OrderHistoryListDataAdapter.OnOrderHistoryListDataAdapterCallbacks {
    public static final String TAG = makeLogTag(OrderHistoriesFragment.class);

    private OrderHistoryListDataAdapter adapter;
    private BaseAttacher attacher;

    private ApiRequest request;
    private ApiParams params;
    private int index = 0;
    private int total = 0;
    private static final int COUNT = 20;

    private MyMenu myMenu;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    public OrderHistoriesFragment() {
        this.request = new ApiRequest(getActivity());
    }

    public static OrderHistoriesFragment newInstance(ApiParams params) {
        OrderHistoriesFragment fragment = new OrderHistoriesFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        fragment.setArguments(args);
        return fragment;
    }

    public static OrderHistoriesFragment newInstance(ApiParams params, MyMenu myMenu) {
        OrderHistoriesFragment fragment = new OrderHistoriesFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        args.putSerializable("myMenu", myMenu);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HashMap<String, Object> map = (HashMap<String, Object>) getArguments().getSerializable("params");
        this.params = new ApiParams(self, map, (String) map.get("url"));
        this.myMenu = (MyMenu) getArguments().getSerializable("myMenu");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_histories, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new OrderHistoryListDataAdapter(getActivity(), this);
        this.recyclerView.setHasFixedSize(false);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.recyclerView.addItemDecoration(new RecyclerItemDividerDecoration(getActivity(), R.drawable.line_divider));
        this.recyclerView.setAdapter(this.adapter);
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
    }


    public static final int ORDER_REQUEST = 101;
    @Override
    public void onOrderHistoryItemClicked(Order order) {
        Intent view = new Intent(getActivity(), OrderHistoryActivity.class);
        view.putExtra("order", order);
//        startActivityForResult(view, ORDER_REQUEST);
        getActivity().startActivityFromFragment(this, view, ORDER_REQUEST);
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
        if (this.index > 0 && this.adapter.getObjects().size() == this.total) {
            LOGD(TAG, "no data");
            return;
        }
        if (this.adapter.getObjects().size() == 0) {
            reset();
        }
        this.index ++;
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
                        LOGE(TAG, "order history error:"+e.getMessage());
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            if (apiResponse.hasError()) {
                                LOGE(TAG, apiResponse.getErrorMessage());
                                AppController.getInstance().showApiErrorAlert(getActivity(), apiResponse.getError());
                                return;
                            }
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            JSONArray objects = result.getJSONArray("history");
                            if (objects.length() < 1) {
                                showNoDataView(R.string.text_orderHistory);
                            } else {
                                hideNoDataView();
                            }
                            for (int i=0; i<objects.length(); i++) {
                                Order order = new Order(objects.getJSONObject(i));
                                adapter.add(order);
                            }
                            total = result.getInt("total");
                            if (myMenu != null) {
                                myMenu.setObjectsCount(total);
                                MyMenuBus.get().post(myMenu);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ORDER_REQUEST && resultCode == Activity.RESULT_OK) {
            reset();
            search();
        }
    }
}
