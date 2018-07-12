package com.tabio.tabioapp.api.decide;

import android.content.Context;
import android.os.Build;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.prefs.Preferences;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.framed.Header;
import rx.Observable;
import rx.Subscriber;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/4/16.
 */
public class DecideApiRequest {
    public static final String TAG = makeLogTag(DecideApiRequest.class);

    private Context context;

    public static final String SIGNATURE_HEADER = "X-DECIDE-SIGNATURE";
    public static final String TOKEN_HEADER = "X-DECIDE-TOKEN";

    /**********************************
     * シグネチャ定数※変更不可
     **********************************/
    private static final int TIME_STEP = 30;
    private static final int T0 = 132265816;
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final int OTP_LEN = 8;
    private static int[] DIGIT_POWER = new int[]{1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    private static final String DECIDE_HOST = BuildConfig.DECIDE_URL;

    private Me me;

    public DecideApiRequest(Context context) {
        this.context = context;
        this.me = AppController.getInstance().getSelf(false);
    }

    public Observable<DecideApiResponse> run(final DecideApiParams apiParams) {
        return Observable.create(new Observable.OnSubscribe<DecideApiResponse>() {
            @Override
            public void call(final Subscriber<? super DecideApiResponse> subscriber) {
                {

                    // デバイストークンがない場合はリクエストしない
                    if (me.getDeviceId().isEmpty()) {
                        LOGE(TAG, "device id is empty");
                        return;
                    }
                    if (me.getTabioId() == null || me.getTabioId().isEmpty()) {
                        LOGE(TAG, "tabio id is empty");
                        return;
                    }

                    if (!AppController.getInstance().gotCompTime) {
                        // 時間取得
                        try {
                            LOGD(TAG, "DECIDE API CALL:" + BuildConfig.DECIDE_URL + BuildConfig.DECIDE_VERSION + "/" + ApiRoute.DECIDE_GET_TIME);
                            Request timeReq = new Request.Builder()
                                    .url(BuildConfig.DECIDE_URL + BuildConfig.DECIDE_VERSION + "/" + ApiRoute.DECIDE_GET_TIME)
                                    .addHeader("Content-Type", "application/json;charset=utf-8")
                                    .get()
                                    .build();
                            Response timeResponse = AppController.getInstance().getDecideHttpClient().newCall(timeReq).execute();
                            JSONObject json = new JSONObject(timeResponse.body().string());
                            LOGD(TAG, "decide get time:" + json.toString(4));
                            JSONObject result = json.getJSONObject("result");
                            int timestamp = result.getInt("timestamp");
                            int deviceTimeStamp = (int) (System.currentTimeMillis() / 1000);
                            int compTime = timestamp - deviceTimeStamp;
                            AppController.getInstance().setDecideCompTime(compTime);

                        } catch (IOException e) {
                            LOGE(TAG, "gettime error:" + e.getMessage());
                            e.printStackTrace();
                            DecideApiResponse apiResponse = new DecideApiResponse(DecideApiRequest.this, null, apiParams);
                            subscriber.onError(new Throwable(apiResponse.getErrorMessage()));
                        } catch (JSONException e) {
                            LOGE(TAG, "gettime json error:" + e.getMessage());
                            e.printStackTrace();
                            DecideApiResponse apiResponse = new DecideApiResponse(DecideApiRequest.this, null, apiParams);
                            subscriber.onError(new Throwable(apiResponse.getErrorMessage()));
                        } catch (Exception e) {
                            LOGE(TAG, "gettime:" + e.getMessage());
                        }
                    }
                }

                String signature = generateSignature();

                // ログイン
                {
                    if (!AppController.getInstance().isDecideLogin()) {
                        LOGD(TAG, "DECIDE ログインします");
                        try {
                            String url = ApiRoute.DECIDE_LOGIN;

                            String url2 = "";
                            DecideApiParams params = new DecideApiParams(url);
                            url += me.getTabioId();
                            params.put("app[device_token]", me.getDeviceId());
                            params.put("app[os_name]", "android");
                            params.put("app[os_version]", String.valueOf(Build.VERSION.RELEASE));
                            url2 = params.addQueryStrings(url2);
                            try {
                                url += URLEncoder.encode(url2, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            Request loginRequest = new Request.Builder()
                                    .url(BuildConfig.DECIDE_URL + BuildConfig.DECIDE_VERSION + "/" + url)
                                    .addHeader("Content-Type", "application/json;charset=utf-8")
                                    .addHeader(SIGNATURE_HEADER, signature)
                                    .get()
                                    .build();
                            Response loginResponse = AppController.getInstance().getDecideHttpClient().newCall(loginRequest).execute();
                            JSONObject json = new JSONObject(loginResponse.body().string());
                            LOGD(TAG, "decide loginResponse:" + json.toString(4));
                            JSONObject result = json.getJSONObject("result");
                            AppController.getInstance().setDecideLogin(result.has("csrf_token"));
                            if (result.has("csrf_token")) {
                                LOGD(TAG, "Decide ログイン成功");
                                AppController.getInstance().setDecideUuid(json.getString("uuid"));
                                AppController.getInstance().setDecideCsrfToken(result.getString("csrf_token"));
                                AppController.getInstance().setDecideCookie(loginResponse.header("Set-Cookie"));
                                String csrfToken = result.getString("csrf_token");
                                LOGD(TAG, "csrcToken:"+csrfToken);
//                                me.setDecideCsrfToken(csrfToken);
//                                boolean save = me.getManager().save();
//                                LOGD(TAG, "decideLoginSave:" + save);
//                                me = new Me();
                            }


                        } catch (IOException e) {
                            LOGE(TAG, e.getMessage());
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                        } catch (Exception e) {
                            LOGE(TAG, e.getMessage());
                        }
                    }
                }


                if (!AppController.getInstance().isDecideLogin()) {
                    return;
                }

                Request request = null;
                String url = apiParams.getUrl();
                url = BuildConfig.DECIDE_URL + BuildConfig.DECIDE_VERSION + "/" + url;
                LOGD(TAG, "DECIDE URL:" + url);

                Request.Builder requestBuilder = new Request.Builder();
                requestBuilder.url(url);
                requestBuilder.addHeader("Content-Type", "application/json;charset=utf-8");
                requestBuilder.addHeader(SIGNATURE_HEADER, signature);
                requestBuilder.addHeader(TOKEN_HEADER, AppController.getInstance().getDecideCsrfToken());

                apiParams.put("customer_id", Long.valueOf(me.getTabioId()));
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        apiParams.getPostParams().toString());
                if (apiParams.getUrl().matches(".*permission.*")) {
                    requestBuilder.put(body);
                } else {
                    requestBuilder.post(body);
                }

                LOGD(TAG, "Cookie:" + AppController.getInstance().getDecideCookie());
                request = requestBuilder.addHeader("Cookie", AppController.getInstance().getDecideCookie()).build();

                AppController.getInstance().getDecideHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        LOGE(TAG, "NetworkError?" + e.getMessage());
                        subscriber.onError(new Throwable(context.getString(R.string.error_text_connection)));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            final JSONObject json = new JSONObject(response.body().string());
                            DecideApiResponse apiResponse = new DecideApiResponse(DecideApiRequest.this, json, apiParams);
                            subscriber.onNext(apiResponse);
                            subscriber.onCompleted();
                        } catch (JSONException e) {
                            LOGE(TAG, "json error:" + e.getMessage());
                            e.printStackTrace();
                            DecideApiResponse apiResponse = new DecideApiResponse(DecideApiRequest.this, null, apiParams);
                            subscriber.onError(new Throwable(apiResponse.getErrorMessage()));
                        } catch (Exception e) {
                            LOGD(TAG, "erro:" + e.getMessage());
                            DecideApiResponse apiResponse = new DecideApiResponse(DecideApiRequest.this, null, apiParams);
                            subscriber.onError(new Throwable(apiResponse.getErrorMessage()));
                        }
                    }
                });
            }
        });
    }


    private String generateSignature() {
        int compTime = AppController.getInstance().getDecideCompTime();
        LOGD(TAG, "comptime: " + String.valueOf(compTime));
        int unixTime = (int) (System.currentTimeMillis() / 1000) + compTime;
        LOGD(TAG, "unixTime: " + String.valueOf(unixTime));
        int messageTime = (unixTime - T0) / TIME_STEP;
        LOGD(TAG, "messageTime: " + String.valueOf(messageTime));
        String message = String.format("%016X", messageTime);
        LOGD(TAG, "message: " + message);
        Mac mac;
        SecretKeySpec key = new SecretKeySpec(BuildConfig.DECIDE_APPLICATION_KEY.getBytes(), HMAC_ALGO);
        try {
            mac = Mac.getInstance(HMAC_ALGO);
            mac.init(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] hashVal = mac.doFinal(message.getBytes());
        int offset = hashVal[hashVal.length - 1] & 0xf;
        int bin = ((int) (hashVal[offset] & 0x7f) << 24) |
                ((int) (hashVal[offset + 1] & 0xff) << 16) |
                ((int) (hashVal[offset + 2] & 0xff) << 8) |
                (int) (hashVal[offset + 3] & 0xff);

        String fmt = "%0" + OTP_LEN + "d";
        return String.format(fmt, bin % DIGIT_POWER[OTP_LEN]);
    }
}
