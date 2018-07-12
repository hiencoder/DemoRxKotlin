package com.tabio.tabioapp.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/19/16.
 */
public class FacebookLoginFragment extends BaseFragment {
    public static final String TAG = makeLogTag(FacebookLoginFragment.class);

    private CallbackManager callbackManager;
    private OnFacebookLoginCallbacks onFacebookLoginCallbacks;

    public static final List FACEBOOK_PERMISSIONS = Arrays.asList("public_profile", /*"email",*/ "user_birthday");
    public static final String FACEBOOK_REQUEST_PARAMETERS = "id, email, gender, birthday";

    public static FacebookLoginFragment newInstance(Bundle args) {
        FacebookLoginFragment fragment = new FacebookLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public FacebookLoginFragment() {
    }

    public interface OnFacebookLoginCallbacks {
        void onFacebookLoginSuccess(Bundle args, final String userId, final String token, final String iconImgUrl);

        void onFacebookLoginFail(Bundle args);

        void onFacebookLoginCancel(Bundle args);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFacebookLoginCallbacks) {
            this.onFacebookLoginCallbacks = (OnFacebookLoginCallbacks) context;
        } else {
            LOGE(TAG, "must implement OnFacebookLoginCallbacks");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "Facebook Login Start");
        AppController.getInstance().showSynchronousProgress(getActivity());
        this.callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(this.callbackManager, this.facebookCallback);
        LoginManager.getInstance().logInWithReadPermissions(this, FACEBOOK_PERMISSIONS);
    }

    private void destroyMySelf() {
        LOGD(TAG, "FacebookLoginFragmentDestroy");
        hideProgress();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(this)
                .commit();
    }

    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            final String userId = loginResult.getAccessToken().getUserId();
            final String token = loginResult.getAccessToken().getToken();
            showNetworkProgress();
            GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(final JSONObject object, GraphResponse response) {
                            try {
                                LOGD(TAG, "facebook me object:" + object.toString(4));
                                LOGD(TAG, "response:" + response.getJSONObject().toString(4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            final String profilePicUrl = "https://graph.facebook.com/" + userId + "/picture?type=large";
                            onFacebookLoginCallbacks.onFacebookLoginSuccess(getArguments(), userId, token, profilePicUrl);
                            hideProgress();
                            destroyMySelf();
                        }
                    }

            );
            Bundle parameters = new Bundle();
            parameters.putString("fields", FACEBOOK_REQUEST_PARAMETERS);
            graphRequest.setParameters(parameters);
            graphRequest.executeAsync();
        }

        @Override
        public void onCancel() {
            LOGD(TAG, "cancel");
            if (onFacebookLoginCallbacks != null) {
                onFacebookLoginCallbacks.onFacebookLoginCancel(getArguments());
                destroyMySelf();
            }
//            loginFailed(Route.FACEBOOK);
            LOGE(TAG, "facebook auth cancel");
//            Toast.makeText(BaseLoginActivity.this, "facebook auth error", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException exception) {
            LOGD(TAG, "error");
            if (onFacebookLoginCallbacks != null) {
                onFacebookLoginCallbacks.onFacebookLoginFail(getArguments());
                destroyMySelf();
            }
//            loginFailed(Route.FACEBOOK);
            LOGE(TAG, "facebook auth error" + exception.getLocalizedMessage().toString());
//            Toast.makeText(BaseLoginActivity.this, "facebook auth error", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (this.callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
