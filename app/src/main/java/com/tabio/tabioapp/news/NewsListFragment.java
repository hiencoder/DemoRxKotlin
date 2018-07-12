package com.tabio.tabioapp.news;


import android.content.Context;
import android.os.Bundle;
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
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.me.MyMenu;
import com.tabio.tabioapp.model.News;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.ui.RecyclerItemDividerDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class NewsListFragment extends BaseFragment implements NewsDataAdapter.OnNewsDataAdapterCallbacks {
    public static final String TAG = makeLogTag(NewsListFragment.class);

    private NewsDataAdapter adapter;
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

    public NewsListFragment() {
        this.request = new ApiRequest(getActivity());
    }

    public static NewsListFragment newInstance(ApiParams params) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        args.putSerializable("params", params);
        fragment.setArguments(args);
        return fragment;
    }

    public static NewsListFragment newInstance(ApiParams params, MyMenu myMenu) {
        NewsListFragment fragment = new NewsListFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new NewsDataAdapter(getActivity(), this);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.recyclerView.setHasFixedSize(false);
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
                        LOGE(TAG, "news list error:"+e.getMessage());
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
                            total = result.getInt("total");
                            if (myMenu != null) {
                                myMenu.setObjectsCount(total);
                                MyMenuBus.get().post(myMenu);
                            }
                            JSONArray objects = result.getJSONArray("information");
                            if (objects.length() < 1) {
                                showNoDataView(R.string.text_news);
                            } else {
                                hideNoDataView();
                            }
                            for (int i=0; i<objects.length(); i++) {
                                News news = new News(objects.getJSONObject(i));
                                adapter.add(news);
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onNewsItemClicked(News news, int position) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
