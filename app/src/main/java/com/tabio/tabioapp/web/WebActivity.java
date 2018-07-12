package com.tabio.tabioapp.web;

import android.os.Bundle;
import android.view.MenuItem;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.GcmObject;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.ui.BaseActivity;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/3/15.
 */
public class WebActivity extends BaseActivity implements WebFragment.OnWebFragmentCallbacks {
    private static final String TAG = makeLogTag(WebActivity.class);

    private String requestUrlStr;
    private WebFragment webFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webFragment = (WebFragment) getSupportFragmentManager().findFragmentById(R.id.web_fragment);
//        showNetworkProgress();
        requestUrlStr = getIntent().getStringExtra("url");
        if (getIntent().getBooleanExtra("cart", false)) {
            requestUrlStr += "&sid="+self.getTabioId();
        }
        if (requestUrlStr != null) {
            webFragment.setWebRequest(requestUrlStr);
        }
    }

    @Override
    public void onWebLoadFinished(String url, String title) {
//        hideProgress();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
