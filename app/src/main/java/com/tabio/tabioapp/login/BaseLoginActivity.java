package com.tabio.tabioapp.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.me.MyBaseActivity;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.util.ImageUtils;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/9/16.
 */
abstract public class BaseLoginActivity extends MyBaseActivity implements
        FacebookLoginFragment.OnFacebookLoginCallbacks, TwitterLoginFragment.OnTwitterLoginCallbacks {
    public static final String TAG = makeLogTag(BaseLoginActivity.class);

    protected ApiRequest apiRequest;
    private TwitterAuthClient twitterAuthClient;

    protected static final String NEXT_ACTION = "NEXT_ACTION";
    protected static final int NEXT_ACTION_MIGRATE = 100;
    protected static final int NEXT_ACTION_UPDATE_PROFILE = 101;

    abstract protected void onMigrateSuccess(@Route.From String from);
    abstract protected void onMigrateFail(@Route.From String from, @Nullable String message, ApiError error);

    public BaseLoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.apiRequest = new ApiRequest(this);
        this.twitterAuthClient = new TwitterAuthClient();
    }

    protected void migrate(final String idInput, final String passwordInput, @Route.From final String from) {
        if (from == Route.EMAIL) {
            if (idInput.toString().isEmpty()) {
                LOGE(TAG, "id or email are empty");
                AppController.getInstance().showAlert(this, getString(R.string.error), getString(R.string.error_text_input_id));
                return;
            }
            if (passwordInput.isEmpty()) {
                LOGE(TAG, "password is empty");
                AppController.getInstance().showAlert(this, getString(R.string.error), getString(R.string.error_text_input_password));
                return;
            }
        }

        AppController.getInstance().showSynchronousProgress(this);
        ApiParams params = null;
        params = self.getManager().getMigrateParams(from, idInput, passwordInput);
//        if (from.equals(Route.EMAIL)) {
//        } else {
//            params = self.getManager().getMigrateParams(from);
//        }
        this.apiRequest.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        AppController.getInstance().showApiErrorAlert(BaseLoginActivity.this, ApiError.newNetworkErrorApiError());
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        LOGD(TAG, apiResponse.getParams().getUrl() + ":hasError?" + apiResponse.hasError());
                        if (apiResponse.hasError()) {
                            LOGE(TAG, apiResponse.getErrorMessage());
                            if (apiResponse.getErrorCode() == ApiError.CONNECT_ERROR) {
                                AppController.getInstance().showApiErrorAlert(BaseLoginActivity.this, apiResponse.getError());
                                onMigrateFail(from, apiResponse.getErrorMessage(), apiResponse.getError());
                                return;
                            } else {
                                onMigrateFail(from, apiResponse.getErrorMessage(), apiResponse.getError());
                                return;
                            }
                        } else {
                            try {
                                JSONObject result = apiResponse.getBody().getJSONObject("result");
                                if (self.getManager().save(result)) {
                                    self.setLogin(true);
                                    try {
                                        if (self.getManager().save()) {
                                            onMigrateSuccess(from);
                                            AppController.getInstance().dismissProgress();
                                        }
                                    } catch (Exception e) {
                                        LOGE(TAG, e.getMessage());
                                        e.printStackTrace();
                                        onMigrateFail(from, getString(R.string.text_error_account_migrate), null);
                                    }
                                }
                            } catch (JSONException e) {
                                LOGE(TAG, e.getMessage());
                                e.printStackTrace();
                                onMigrateFail(from, getString(R.string.text_error_account_migrate), null);
                            }
                        }
                    }
                });
    }

    protected void twitterAuthentication(Bundle args, boolean forAccountRegister) {
        args.putBoolean("forAccountRegister", forAccountRegister);
        getSupportFragmentManager()
                .beginTransaction()
                .add(TwitterLoginFragment.newInstance(args, this.twitterAuthClient),
                        TwitterLoginFragment.TAG)
                .commit();

    }

//    @Override
//    public void onTwitterLoginSuccess(Bundle args) {
//        LOGD(TAG, "onTwitterLoginSuccess:" + self.getProfile().getNickname());
////        updateProfile(Route.TWITTER, args);
//        onMigrateSuccess(Route.TWITTER);
//    }

    @Override
    public void onTwitterLoginSuccess(final Bundle args, final String userId, final String userName, final String token, final String iconImgUrl) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "userId:"+userId+"\n userName:"+userName+"\n token:"+token+"\n icon:"+iconImgUrl, Toast.LENGTH_LONG).show();
        }
        boolean forAccountRegister = args.getBoolean("forAccountRegister");
        // 会員移行だった場合
        if (!forAccountRegister) {
            migrate(userId, token, Route.TWITTER);
            return;
        }

        // アカウント連携だった場合
        Picasso.with(this).load(iconImgUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                String encodedImageBlob = ImageUtils.getBase64FromBitmap(bitmap);
                ApiParams params = self.getManager().getUpdateProfileParamsFromTwitter(userId, userName, token, encodedImageBlob);
                params.put("icon", encodedImageBlob);
                LOGD(TAG, encodedImageBlob);
                updateProfile(Route.TWITTER, args, params);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                AppController.getInstance().dismissProgress();
                onTwitterLoginFail(args);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                AppController.getInstance().showSynchronousProgress(BaseLoginActivity.this);
            }
        });
    }

    @Override
    public void onTwitterLoginFail(Bundle args) {
        LOGE(TAG, "onTwitterLoginFail");
        String errorMessage = "";
        if (args.getBoolean("forAccountRegister")) {
            errorMessage = getString(R.string.text_account_login_twitter_fail2);
        } else {
            errorMessage = getString(R.string.text_account_login_twitter_fail);
        }
        AppController.getInstance().showAlert(this, getString(R.string.error), errorMessage);
    }

    protected void facebookAuthentication(Bundle args, boolean forAccountRegister) {
        args.putBoolean("forAccountRegister", forAccountRegister);
        getSupportFragmentManager()
                .beginTransaction()
                .add(FacebookLoginFragment.newInstance(args),
                        FacebookLoginFragment.TAG)
                .commit();
    }

    @Override
    public void onFacebookLoginSuccess(final Bundle args, final String userId, final String token, String iconImgUrl) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "userId:"+userId+"\n  token:"+token+"\n icon:"+iconImgUrl, Toast.LENGTH_LONG).show();
        }
        boolean forAccountRegister = args.getBoolean("forAccountRegister");

        // 会員移行だった場合
        if (!forAccountRegister) {
            migrate(userId, token, Route.FACEBOOK);
            return;
        }
        // アカウント連携だった場合
        Picasso.with(this).load(iconImgUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                String encodedImageBlob = ImageUtils.getBase64FromBitmap(bitmap);
                ApiParams params = self.getManager().getUpdateProfileParamsFromFacebook(userId, token, encodedImageBlob);
                params.put("icon", encodedImageBlob);
                updateProfile(Route.FACEBOOK, args, params);
                hideProgress();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                hideProgress();
                onFacebookLoginFail(args);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                showNetworkProgress();
            }
        });
    }

    @Override
    public void onFacebookLoginFail(Bundle args) {
        LOGE(TAG, "onFacebookLoginFail");
        String errorMessage = "";
        if (args.getBoolean("forAccountRegister")) {
            errorMessage = getString(R.string.text_account_login_facebook_fail2);
        } else {
            errorMessage = getString(R.string.text_account_login_facebook_fail);
        }
        AppController.getInstance().showAlert(this, getString(R.string.error), errorMessage);
    }

    @Override
    public void onFacebookLoginCancel(Bundle args) {
        LOGE(TAG, "onFacebookLoginCancel");
        AppController.getInstance().showAlert(this, getString(R.string.error), getString(R.string.text_account_login_facebook_fail));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.twitterAuthClient != null) {
            this.twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }
}
