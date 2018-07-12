package com.tabio.tabioapp.me;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.api.decide.DecideApiParams;
import com.tabio.tabioapp.api.decide.DecideApiRequest;
import com.tabio.tabioapp.api.decide.DecideApiResponse;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.piece.PieceAnimView;
import com.tabio.tabioapp.preference.EmailPasswordRegisterActivity;
import com.tabio.tabioapp.preference.EmailPasswordUpdateActivity;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/14/16.
 */
public abstract class MyBaseActivity extends BaseActivity {
    public static final String TAG = makeLogTag(MyBaseActivity.class);

    private ApiRequest request;

    abstract protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args);

    abstract protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args);

    abstract protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank);

    abstract protected void onRefreshedMyInfoFailed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.request = new ApiRequest(this);
    }

    protected void getUser(final boolean savePiece) {
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_USER);
        params.put("device_os", "android");
        showNetworkProgress();

        this.request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, e.getMessage());
                        onRefreshedMyInfoFailed();
                        AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, ApiError.newNetworkErrorApiError());
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (apiResponse.hasError()) {
                            onRefreshedMyInfoFailed();
                            AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, apiResponse.getError());
                            return;
                        }
                        try {
                            JSONObject json = apiResponse.getBody().getJSONObject("result");
                            Me me = AppController.getInstance().getSelf(false);
                            final int oldPiece = me.getPiece();
                            final int oldPoint = me.getPoint();
                            final int newPiece = json.getInt("piece");
                            final int newPoint = json.getInt("point");
                            final int oldRank;
                            oldRank = oldPiece / PieceAnimView.DEFAULT_RANK_MAX_PIECE_NUM;
                            final int newRank;
                            newRank = newPiece / PieceAnimView.DEFAULT_RANK_MAX_PIECE_NUM;
                            LOGD(TAG, "oldPiece:" + oldPiece);
                            LOGD(TAG, "oldPoint:" + oldPoint);
                            LOGD(TAG, "newPiece:" + newPiece);
                            LOGD(TAG, "newPoint:" + newPoint);
                            LOGD(TAG, "oldRank:" + oldRank);
                            LOGD(TAG, "newRank:" + newRank);
                            if (me.getManager().save(json, savePiece)) {
                                try {
                                    String iconBlob = getBlobFromUrl(MyBaseActivity.this, me.getProfile().getIconImgUrl());
                                    if (iconBlob != null) {
                                        LOGD(TAG, "アイコンのサイズ:"+iconBlob.length());
                                        me.getProfile().setIconImgBlob(iconBlob);
                                    }
                                } catch (IOException e) {
                                    LOGE(TAG, e.getMessage());
                                    onRefreshedMyInfoFailed();
                                    return;
                                }

                                try {
                                    String coverBlob = getBlobFromUrl(MyBaseActivity.this, me.getProfile().getCoverImgUrl());
                                    if (coverBlob != null) {
                                        LOGD(TAG, "カバーのサイズ:"+coverBlob.length());
                                        me.getProfile().setCoverImgBlob(coverBlob);
                                    }
                                } catch (IOException e) {
                                    LOGE(TAG, e.getMessage());
                                    onRefreshedMyInfoFailed();
                                    return;
                                }

                                boolean result = me.getProfile().getManager().save();
                                LOGD(TAG, "profile get user:"+result);
                                self = AppController.getInstance().getSelf(true);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        getNavigationHeaderView();
                                        onRefreshedMyInfo(oldPiece, newPiece, oldPoint, newPoint, oldRank, newRank);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, ApiError.newUndefinedApiError());
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            onRefreshedMyInfoFailed();
                        } catch (Exception e) {
                            AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, ApiError.newUndefinedApiError());
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            onRefreshedMyInfoFailed();
                        }

                    }
                });
    }

    protected void updateDecideProfile() {
        DecideApiParams params = new DecideApiParams(ApiRoute.DECIDE_NOTIFY);
        params.put("mailmaga_type", self.isReceiveMailMagazine() ? "html" : "");
        params.put("can_push", self.isReceiveNews() ? true : false);
        LOGD(TAG, "csrf_token:" + AppController.getInstance().getDecideCsrfToken());
        DecideApiRequest request = new DecideApiRequest(this);
        request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<DecideApiResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(DecideApiResponse decideApiResponse) {

                    }
                });
    }

    protected void updateProfile(@Nullable final @Route.From String from, @Nullable final Bundle args) {
        self = AppController.getInstance().getSelf(true);
        ApiParams params = self.getManager().getUpdateProfileParams();
        params.put("icon", self.getProfile().getIconImgBlob());
        params.put("cover", self.getProfile().getCoverImgBlob());
        updateProfile(from, args, params);
    }

    protected void updateProfile(@Nullable final @Route.From String from, @Nullable final Bundle args, ApiParams params) {
        self = AppController.getInstance().getSelf(true);
        AppController.getInstance().showSynchronousProgress(this);

        LOGD(TAG, "BEGIN UPDATE PROFILE ==========================");
        try {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String val = entry.getValue().toString();
                if (val.length() > 50) {
                    val = val.substring(0, 50);
                }
                LOGD(TAG, entry.getKey() + ":" + val);
            }
        } catch (Exception e) {
            LOGE(TAG, "map error:"+e.getMessage());
            e.printStackTrace();
            AppController.getInstance().dismissProgress();
            onUpdateProfileFailed(from, args);
            return;
        }

        LOGD(TAG, "END   UPDATE PROFILE ==========================");
        this.request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        updateDecideProfile();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, "update profile network error?"+e.getMessage());
                        AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, ApiError.newNetworkErrorApiError().newNetworkErrorApiError());
                        onUpdateProfileFailed(from, args);
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (apiResponse.hasError()) {
                            LOGE(TAG, "api error:"+apiResponse.getErrorMessage());
                            AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, apiResponse.getError());
                            onUpdateProfileFailed(from, args);
                            return;
                        }
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            LOGD(TAG, "updateUser:"+result.toString(4));
                            String iconBlob = getBlobFromUrl(MyBaseActivity.this, self.getProfile().getIconImgUrl());
                            if (iconBlob != null) {
                                self.getProfile().setIconImgBlob(iconBlob);
                            }
                            String coverBlob = getBlobFromUrl(MyBaseActivity.this, self.getProfile().getCoverImgUrl());
                            if (coverBlob != null) {
                                self.getProfile().setCoverImgBlob(coverBlob);
                            }
                            if (self.getManager().save(result)) {
                                try {
                                    self = AppController.getInstance().getSelf(true);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            AppController.getInstance().dismissProgress();
                                            getNavigationHeaderView();
                                            onUpdateProfileSuccessed(from, args);
                                        }
                                    });
                                } catch (Exception e) {
                                    LOGE(TAG, "error final:"+e.getMessage());
                                    AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, null);
                                    onUpdateProfileFailed(from, args);
                                }

                            } else {
                                LOGE(TAG, "save error");
                                AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, null);
                                onUpdateProfileFailed(from, args);
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, "json error:"+e.getMessage());
                            AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, null);
                            onUpdateProfileFailed(from, args);
                        } catch (IOException e) {
                            LOGE(TAG, "io error:"+e.getMessage());
                            AppController.getInstance().showApiErrorAlert(MyBaseActivity.this, null);
                            onUpdateProfileFailed(from, args);
                        }
                    }
                });
    }

    private String getBlobFromUrl(Context context, String url) throws IOException {
        if (url != null && !url.isEmpty() && !url.equals("null")) {
//            Bitmap btp = Picasso.with(context).load(url).get();
            LOGE(TAG, "get blob from url:"+url);
            Bitmap btp = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
            String blob = ImageUtils.getBase64FromBitmap(btp);
            return blob;
        }
        return null;
    }

    protected void toEmailPasswordRegisterScreen() {
        Route route = self.getManager().getRoute(Route.EMAIL);
        if (route != null) {
            LOGD(TAG, "route:" + route.getProviderId() + route.getSecurityKey());
            Intent view = new Intent(this, EmailPasswordUpdateActivity.class);
            startActivity(view);
        } else {
            AppController.getInstance().sendGAScreen("メールアドレス・パスワードでログイン");
            AppController.getInstance().decideTrack("570f30c299c3634a425af517");
            Intent view = new Intent(this, EmailPasswordRegisterActivity.class);
            startActivity(view);
        }
    }
}
