package com.tabio.tabioapp.item.review;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.me.MyMenu;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Review;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.ui.RecyclerItemDividerDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ReviewsFragment extends BaseFragment implements ReviewDataAdapter.OnReviewDataAdapterCallbacks {
    public static final String TAG = makeLogTag(ReviewsFragment.class);

    private ReviewDataAdapter adapter;
    private BaseAttacher attacher;
    private boolean myReviews = false;
    private boolean hideItemView = true;

    private ApiRequest request;
    private ApiParams params;
    private int index = 0;
    private int total = 0;
    private static final int COUNT = 20;

    private MyMenu myMenu;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    public ReviewsFragment() {
        this.request = new ApiRequest(getActivity());
    }

    public static ReviewsFragment newInstance(boolean myReviews, boolean hideItemView, ApiParams params) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putBoolean("myReviews", myReviews);
        args.putBoolean("hideItemView", hideItemView);
        args.putSerializable("params", params);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReviewsFragment newInstance(boolean myReviews, boolean hideItemView, ApiParams params, MyMenu myMenu) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putBoolean("myReviews", myReviews);
        args.putBoolean("hideItemView", hideItemView);
        args.putSerializable("params", params);
        args.putSerializable("myMenu", myMenu);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getInstance().sendGAScreen("レビュー一覧");
        AppController.getInstance().decideTrack("570f2fab99c3634a425af4f8");
        this.params = (ApiParams) getArguments().getSerializable("params");
        this.myReviews = getArguments().getBoolean("myReviews");
        this.hideItemView = getArguments().getBoolean("hideItemView");
        this.myMenu = (MyMenu) getArguments().getSerializable("myMenu");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new ReviewDataAdapter(getActivity(), this.myReviews, this.hideItemView, this);
        this.recyclerView.setHasFixedSize(false);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (this.myReviews) {
            this.recyclerView.addItemDecoration(new RecyclerItemDividerDecoration(getActivity(), R.drawable.review_divider));
        }
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
//        if (context instanceof OnReviewsFragmentCallbacks) {
//            callbacks = (OnReviewsFragmentCallbacks) context;
//
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnReviewsFragmentCallbackss");
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
        if (this.index > 0 && this.adapter.getObjects().size() == total) {
            return;
        }
        if (this.adapter.getObjects().size() == 0) {
            reset();
        }
        this.index ++;
        this.params.put("count", COUNT);
        this.params.put("index", this.index);
        showNetworkProgress();
        this.request.run(params)
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
                        LOGE(TAG, "reviews error:"+e.getMessage());
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showApiErrorAlert(getActivity(), apiResponse.getError());
                            return;
                        }
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            JSONArray objects = result.getJSONArray("review");
                            total = result.getInt("total");
                            if (myMenu != null) {
                                myMenu.setObjectsCount(total);
                                MyMenuBus.get().post(myMenu);
                            }
                            if (objects.length() < 1) {
                                showNoDataView(R.string.text_review);
                            } else {
                                hideNoDataView();
                            }
                            for (int i=0; i<objects.length(); i++) {
                                JSONObject json = objects.getJSONObject(i);
                                Review object = new Review(json);
                                adapter.add(object);
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
    public void onReviewItemClicked(Review review) {
        Intent view = new Intent(getActivity(), ReviewActivity.class);
//        view.putExtra("product", item.getProductId());
        view.putExtra("review", review);
        view.putExtra("myReview", this.myReviews);
        startActivity(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
