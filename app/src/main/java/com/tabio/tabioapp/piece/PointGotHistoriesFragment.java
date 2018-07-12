package com.tabio.tabioapp.piece;

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
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.me.MyMenu;
import com.tabio.tabioapp.model.PiecePointEvent;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.ui.RecyclerItemDividerDecoration;
import com.tabio.tabioapp.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/15/16.
 */
public class PointGotHistoriesFragment extends BaseFragment {
    public static final String TAG = makeLogTag(PointGotHistoriesFragment.class);

    private PointHistoryDataAdapter adapter;
    private BaseAttacher attacher;

    private ApiRequest request;
    private ApiParams params;
    private int index = 0;
    private int total = 0;
    private static final int COUNT = 20;

    private int totalPoint = 0;
    private String pointExpiresDate;

    private MyMenu myMenu;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    public PointGotHistoriesFragment() {
        this.request = new ApiRequest(getActivity());
    }

    public static PointGotHistoriesFragment newInstance() {
        PointGotHistoriesFragment fragment = new PointGotHistoriesFragment();
        return fragment;
    }

    public static PointGotHistoriesFragment newInstance(MyMenu myMenu) {
        PointGotHistoriesFragment fragment = new PointGotHistoriesFragment();
        Bundle args = new Bundle();
        args.putSerializable("myMenu", myMenu);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.params = new ApiParams(self, true, ApiRoute.GET_POINT_GOT_HISTORIES);
        this.myMenu = (MyMenu) getArguments().getSerializable("myMenu");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_point_got_histories, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new PointHistoryDataAdapter(getActivity(), this.totalPoint, this.pointExpiresDate);
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

    private void reset() {
        this.adapter.getObjects().clear();
        this.index = 0;
        this.total = 0;
        refresh();
    }

    public void reload(boolean reset) {
        if (reset) {
            reset();
        }
        search();
    }

    public void refresh() {
        if (this.adapter != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.setTotalPoint(totalPoint, pointExpiresDate);
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
                        LOGE(TAG, e.getMessage());
                        e.printStackTrace();
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
                            totalPoint = result.getInt("total_points");
                            String expires = result.getString("duration");
                            pointExpiresDate = DateUtils.getDateFromFormat("yyyy/MM/dd HH:mm:ss", expires, self.getLanguage());

                            JSONArray objects = result.getJSONArray("history");
                            for (int i=0; i<objects.length(); i++) {
                                JSONObject json = objects.getJSONObject(i);
                                PiecePointEvent event = new PiecePointEvent(json);
                                adapter.add(event);
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
