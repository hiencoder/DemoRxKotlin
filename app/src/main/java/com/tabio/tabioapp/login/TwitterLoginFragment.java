package com.tabio.tabioapp.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.util.ImageUtils;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import java.io.IOException;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/19/16.
 */
public class TwitterLoginFragment extends BaseFragment {
    public static final String TAG = makeLogTag(TwitterLoginFragment.class);

    private OnTwitterLoginCallbacks onTwitterLoginCallbacks;
    private TwitterAuthClient twitterAuthClient;

    public static TwitterLoginFragment newInstance(Bundle args, TwitterAuthClient twitterAuthClient) {
        TwitterLoginFragment fragment = new TwitterLoginFragment();
        fragment.twitterAuthClient = twitterAuthClient;
        fragment.setArguments(args);
        return fragment;
    }

    public TwitterLoginFragment() {
    }

    public interface OnTwitterLoginCallbacks {
        void onTwitterLoginSuccess(Bundle args, final String userId, final String userName, final String token, final String iconImgUrl);
        void onTwitterLoginFail(Bundle args);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnTwitterLoginCallbacks) {
            this.onTwitterLoginCallbacks = (OnTwitterLoginCallbacks) context;
        } else {
            LOGE(TAG, "must implement OnTwitterLoginCallbacks");
        }
    }

    private void destroyMySelf() {
        LOGD(TAG, "TwitterLoginFragmentDestroy");
        hideProgress();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(this)
                .commit();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        twitterAuthClient = new TwitterAuthClient();
        twitterAuthClient.authorize(getActivity(), new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                LOGD(TAG, "username:" + result.data.getUserName());
                LOGD(TAG, "userid:" + String.valueOf(result.data.getUserId()));
                LOGD(TAG, "token:" + result.data.getAuthToken().token.toString());
                final String userName = result.data.getUserName();
                final String userId = String.valueOf(result.data.getUserId());
                final String token = result.data.getAuthToken().token.toString();
                TwitterSession sessionData = result.data;
                Twitter.getApiClient(sessionData).getAccountService().verifyCredentials(true, false, new Callback<User>() {
                    @Override
                    public void success(final Result<User> result) {
                        final String twitterProfileIconImgUrl = result.data.profileImageUrl;
                        onTwitterLoginCallbacks.onTwitterLoginSuccess(getArguments(), userId, userName, token, twitterProfileIconImgUrl);
                        LOGD(TAG, twitterProfileIconImgUrl);
                        destroyMySelf();
                        hideProgress();
                    }

                    @Override
                    public void failure(TwitterException e) {
                        LOGE(TAG, e.getMessage());
                        if (onTwitterLoginCallbacks != null) {
                            onTwitterLoginCallbacks.onTwitterLoginFail(getArguments());
                            destroyMySelf();
                        }
                    }
                });
            }

            @Override
            public void failure(TwitterException e) {
                LOGE(TAG, e.getMessage());
                if (onTwitterLoginCallbacks != null) {
                    onTwitterLoginCallbacks.onTwitterLoginFail(getArguments());
                    destroyMySelf();
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.twitterAuthClient != null) {
            this.twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }
}
