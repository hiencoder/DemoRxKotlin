package com.tabio.tabioapp.store;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.api.decide.DecideApiParams;
import com.tabio.tabioapp.api.decide.DecideApiRequest;
import com.tabio.tabioapp.api.decide.DecideApiResponse;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.preference.LanguageSettingsActivity;
import com.tabio.tabioapp.ui.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/23/16.
 */
public class CheckinFragment extends BaseFragment {
    public static final String TAG = makeLogTag(CheckinFragment.class);

    @BindView(R.id.checkin)
    ImageView checkinAction;

    private Unbinder unbinder;

    private OnCheckinCallbacks callbacks;

    private Store store;

    public static CheckinFragment newInstance(Store store) {
        Bundle args = new Bundle();
        args.putSerializable("store", store);
        CheckinFragment fragment = new CheckinFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnCheckinCallbacks {
        void onCheckinSuccess(Store store);

        void onCheckinFail(Store store);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.store = (Store) getArguments().getSerializable("store");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin_success, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!self.getLanguage().equals(LanguageSettingsActivity.LANG_JA)) {
            checkinAction.setImageResource(R.drawable.checkin_action_en);
        }

        ApiParams params = new ApiParams(self, true, ApiRoute.SEND_CHECKIN);
        params.put("store_id", store.getStoreId());
        showNetworkProgress();
        ApiRequest request = new ApiRequest(getActivity());
        request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, e.getMessage());
                        hideProgress();
                        fail();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        hideProgress();
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showApiErrorAlert(getActivity(), apiResponse.getError());
                            fail();
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                checkinAction.setVisibility(View.VISIBLE);
                            }
                        });
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                success();
                            }
                        }, 2000);
                    }
                });
        /*
        DecideApiRequest request = new DecideApiRequest(getActivity());
        DecideApiParams params = new DecideApiParams(ApiRoute.DECIDE_SHOP_VISIT+store.getStoreId()+"/");
        params.put("distance", "0"); // チェックインの時は0で送る
        showNetworkProgress();
        request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<DecideApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, "error:"+e.getMessage());
                        hideProgress();
                        fail();
                    }

                    @Override
                    public void onNext(DecideApiResponse decideApiResponse) {
                    }
                });
                */
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnCheckinCallbacks) {
            callbacks = (OnCheckinCallbacks) context;
        } else {
            LOGE(TAG, "must implement OnCheckinCallbacks");
        }
    }

    private void success() {
        LOGD(TAG, "onCheckinSuccess success");
        AppController.getInstance().sendGAEvent("Store", "Checkin", "", Long.valueOf(store.getStoreId()));
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.onCheckinSuccess(store);
                }
            }
        });
        close();
    }

    private void fail() {
        LOGD(TAG, "onCheckinFail fail");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.onCheckinFail(store);
                }
            }
        });
        close();
    }

    private void close() {
        try {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
