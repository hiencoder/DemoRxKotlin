package com.tabio.tabioapp.api.cart;

import android.content.Context;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/12/16.
 */
public class CartApiRequest {
    public static final String TAG = makeLogTag(CartApiRequest.class);

    private Context context;

    public CartApiRequest(Context context) {
        this.context = context;
    }

    public Observable<CartApiResponse> run(final  CartApiParams params) {
        return Observable.create(new Observable.OnSubscribe<CartApiResponse>() {
            @Override
            public void call(final Subscriber<? super CartApiResponse> subscriber) {
//                RequestBody body = new FormBody
                /*
                this.put("tabio_id", self.getTabioId());
        this.put("sid", self.getTabioId());
        this.put("shop_id", 0);
                 */
//                FormBody form = FormBody.create(
//                        MediaType.parse("x-www-form-urlencoded"), ""
//                );
                RequestBody formBody = new FormBody.Builder()
                        .add("command", params.getCommand())
                        .add("params", params.getProductsJson().toString())
                        .build();
                Request request = new Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .url(params.getUrl())
                        .post(formBody)
                        .build();
                AppController.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // network errorなど
                        LOGE(TAG, "NetworkError?" + e.getLocalizedMessage());
                        subscriber.onError(new Throwable(context.getString(R.string.error_text_connection)));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            final JSONObject json = new JSONObject(response.body().string());
                            CartApiResponse apiResponse = new CartApiResponse(CartApiRequest.this, json, params);
                            subscriber.onNext(apiResponse);
                        } catch (JSONException e) {
                            LOGE(TAG, params.getUrl()+" : "+e.getLocalizedMessage());
                            e.printStackTrace();
                            CartApiResponse apiResponse = new CartApiResponse(CartApiRequest.this, null, params);
                            subscriber.onError(new Throwable(apiResponse.getErrorMessage()));
                        } finally {
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }
}
