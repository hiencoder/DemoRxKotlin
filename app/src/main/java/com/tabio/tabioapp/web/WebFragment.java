package com.tabio.tabioapp.web;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.top.TopActivity;
import com.tabio.tabioapp.ui.BaseFragment;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebFragment extends BaseFragment {
    private static final String TAG = makeLogTag(WebFragment.class);

    @BindView(R.id.webview)
    WebView webView;

    private Unbinder unbinder;

    public OnWebFragmentCallbacks callbacks;

    public WebFragment() {
        // Required empty public constructor
    }

    public interface OnWebFragmentCallbacks {
        void onWebLoadFinished(String url, String title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showNetworkProgress();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                try {
//                    for (String cookie : CookieManager.getInstance().getCookie(BuildConfig.BASE_URL).split(";")) {
//                        LOGD(TAG, "Cookie:" + cookie.trim());
//                    }
//                } catch (NullPointerException e) {
//                    LOGE(TAG, e.getMessage());
//                }
                if (getActivity() != null) {
                    getActivity().setTitle(view.getTitle());
                    if (callbacks != null) {
                        callbacks.onWebLoadFinished(url, view.getTitle());
                    }
                    hideProgress();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                AppController.getInstance().showApiErrorAlert(getActivity(), ApiError.newNetworkErrorApiError());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LOGD(TAG, "link:" + url);
                if (url.equals("tabioapp://back")) {
                    getActivity().finish();
                    return false;
                } else if (url.equals("tabioapp://screen/membership")) {
                    Intent v = new Intent(getActivity(), TopActivity.class);
                    v.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(v);
                    getActivity().finish();
                    return false;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    public void setWebRequest(String urlStr) {
        LOGD(TAG, "load webview=" + urlStr);
        webView.loadUrl(urlStr);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnWebFragmentCallbacks) {
            callbacks = (OnWebFragmentCallbacks) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWebFragmentCallbacks");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
