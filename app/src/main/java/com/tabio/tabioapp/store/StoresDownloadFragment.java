package com.tabio.tabioapp.store;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.ui.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/22/16.
 */
public class StoresDownloadFragment extends BaseFragment {
    public static final String TAG = makeLogTag(StoresDownloadFragment.class);

    private OnStoresDownloadFragmentCallbacks callbacks;

    public static StoresDownloadFragment newInstance() {
        Bundle args = new Bundle();
        StoresDownloadFragment fragment = new StoresDownloadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnStoresDownloadFragmentCallbacks {
        void onStoresDownloadFinish();
        void onStoresDownloadFail();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!self.isLogin()) {
            fail();
            return;
        }

        if (!self.haveStores()) {
            downloadStores();
            return;
        }

        // 最後に取得した時間が1時間以内だったら、チェックしない
        Store lastStore = new Store();
        if (lastStore.setLastOne()) {
            if (lastStore.wasUpdatedWithinOneHour()) {
                fail();
                return;
            }
        }

        checkUpdateStores(lastStore);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnStoresDownloadFragmentCallbacks) {
            this.callbacks = (OnStoresDownloadFragmentCallbacks) context;
        } else {
            LOGE(TAG, "must implement OnStoresDownloadFragmentCallbacks");
        }
    }

    private void checkUpdateStores(Store lastStore) {
        // 更新店舗があるかをチェックする
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_HAS_UPDATE_STORES);
        params.put("reference_date", lastStore.getUpdatedAt());
        ApiRequest request = new ApiRequest(getActivity());
        request.run(params)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            boolean needUpdate = result.getInt("update_flg") == 1 ? true : false;
                            // 更新店舗があったら、店舗を取得する
                            if (needUpdate) {
                                downloadStores();
                            } else {
                                success();
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            fail();
                        }
                    }
                });
    }

    private void downloadStores() {
//        showNetworkProgress();
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_STORES);
        params.put("search", 0);
        params.put("index", 1);
        params.put("count", 100000000);
        ApiRequest request = new ApiRequest(getActivity());
        AppController.getInstance().showSynchronousProgress(getActivity());
        request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
//                        hideProgress();
                        fail();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            int total = result.getInt("total");
                            LOGD(TAG, "store data count:"+total);
                            JSONArray stores = result.getJSONArray("store");
                            for (int i = 0; i < stores.length(); i++) {
                                new Store().getManager().save(stores.getJSONObject(i));
                            }
                            success();
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            fail();
                        }
                    }
                });
    }

    private void success() {
        LOGD(TAG, "StoresDownload success");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.onStoresDownloadFinish();
                }
            }
        });
        close();
    }

    private void fail() {
        LOGD(TAG, "StoresDownload fail");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) {
                    callbacks.onStoresDownloadFail();
                }
            }
        });
        close();
    }


    private void close() {
        try {
            AppController.getInstance().dismissProgress();
            getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(this)
                .commit();
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }

    }
}
