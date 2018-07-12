package com.tabio.tabioapp.api;

import android.content.Context;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/20/16.
 */
public class ApiRequest {
    public static final String TAG = makeLogTag(ApiRequest.class);

    private Context context;

    public ApiRequest(Context context) {
        this.context = context;
    }

    public Observable<ApiResponse> run(final ApiParams params) {
        return Observable.create(new Observable.OnSubscribe<ApiResponse>() {
            @Override
            public void call(final Subscriber<? super ApiResponse> subscriber) {
                boolean forceRefreshToken = params.getUrl().equals(ApiRoute.REFRESH_TOKEN);

                updateTokenIfNeed(params, forceRefreshToken, subscriber);

                if (forceRefreshToken) {
                    return;
                }

                if (params.get("token") != null) {
                    params.put("token", AppController.getInstance().getSelf(false).getToken());
                }
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        params.getJson().toString());
                final Request request = new Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .url(BuildConfig.BASE_URL + params.getUrl())
                        .post(body)
                        .build();
                AppController.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        LOGE(TAG, "NetworkError?" + e.getLocalizedMessage());
                        ApiResponse apiResponse = new ApiResponse(ApiRequest.this, null, params);
                        ApiError networkError = ApiError.newNetworkErrorApiError();
                        apiResponse.setError(networkError);
                        subscriber.onNext(apiResponse);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        ApiResponse apiResponse = null;
                        try {
                            final JSONObject json = new JSONObject(response.body().string());
                            apiResponse = new ApiResponse(ApiRequest.this, json, params);
                            subscriber.onNext(apiResponse);
                        } catch (JSONException e) {
                            LOGD(TAG, params.toString());
                            LOGE(TAG, response.request().url().toString()+"  :"+e.getLocalizedMessage());
                            e.printStackTrace();
                            apiResponse = new ApiResponse(ApiRequest.this, null, params);
                            subscriber.onNext(apiResponse);
                        } finally {
                            if (apiResponse != null && apiResponse.hasError()) {
                                int code = apiResponse.getErrorCode();
//                                if (code == ApiError.CODE_99) {
//                                    AppController.getInstance().maintenanceMode();
//                                }
                            }
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }

    private void updateTokenIfNeed(ApiParams params, boolean forceRefreshToken, final Subscriber<? super ApiResponse> subscriber) {
        if (AppController.getInstance().getSelf(false).getManager().needRefreshToken() || forceRefreshToken) {
            LOGE(TAG, "トークンのリフレッシュが必要です。");
            try {
                JSONObject refreshParams = new JSONObject();
                refreshParams.put("token", params.getSelf().getRefreshToken());
                refreshParams.put("tabio_id", params.getSelf().getTabioId());
                refreshParams.put("language", params.getSelf().getLanguage());
                Request refreshReq = new Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .url(BuildConfig.BASE_URL + ApiRoute.REFRESH_TOKEN)
                        .post(RequestBody.create(
                                MediaType.parse("application/json"),
                                refreshParams.toString()
                        ))
                        .build();
                OkHttpClient http = new OkHttpClient();
                Response refreshResponse = http.newCall(refreshReq).execute();
                final JSONObject json = new JSONObject(refreshResponse.body().string());
                if (params.getSelf().getManager().save(json)) {
                    LOGD(TAG, "トークンのリフレッシュに成功しました");
                    params.put("token", params.getSelf().getToken());
                    if (forceRefreshToken && BuildConfig.DEBUG) {
                        ApiResponse apiResponse = new ApiResponse(ApiRequest.this, json, params);
                        subscriber.onNext(apiResponse);
                    }
                } else {
                    LOGE(TAG, "トークンのリフレッシュに失敗しました...");
                    if (forceRefreshToken && BuildConfig.DEBUG) {
                        ApiResponse apiResponse = new ApiResponse(ApiRequest.this, json, params);
                        subscriber.onNext(apiResponse);
                    }
                }
                LOGD(TAG, json.toString(4));

            } catch (JSONException e) {
                e.printStackTrace();
                LOGE(TAG, params.getUrl()+":"+e.getMessage());
                if (forceRefreshToken && BuildConfig.DEBUG) {
                    ApiResponse apiResponse = new ApiResponse(ApiRequest.this, null, params);
                    subscriber.onNext(apiResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
                LOGE(TAG, params.getUrl()+":"+e.getMessage());
                if (forceRefreshToken && BuildConfig.DEBUG) {
                    ApiResponse apiResponse = new ApiResponse(ApiRequest.this, null, params);
                    subscriber.onNext(apiResponse);
                }
            } catch (Exception e) {
                LOGE(TAG, params.getUrl()+":"+e.getMessage());
                e.printStackTrace();
                if (forceRefreshToken && BuildConfig.DEBUG) {
                    ApiResponse apiResponse = new ApiResponse(ApiRequest.this, null, params);
                    subscriber.onNext(apiResponse);
                }
            }
            if (forceRefreshToken && BuildConfig.DEBUG) {
                subscriber.onCompleted();
            }
        }
    }

}
