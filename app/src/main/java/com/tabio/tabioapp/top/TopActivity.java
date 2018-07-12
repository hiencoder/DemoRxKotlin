package com.tabio.tabioapp.top;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.igaworks.IgawCommon;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.GcmObject;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.tutorial.TutorialActivity;
import com.tabio.tabioapp.ui.BaseActivity;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class TopActivity extends BaseActivity {
    private static final String TAG = makeLogTag(TopActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IgawCommon.startApplication(this);
        getWindow().setWindowAnimations(0);
        setContentView(R.layout.activity_top);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IgawCommon.startSession(this);
        showNetworkProgress();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                validateMyStatus();
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IgawCommon.endSession();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void validateMyStatus() {
        self = AppController.getInstance().getSelf(true);
        LOGD(TAG, "login:"+self.isLogin());
        if (!self.isLogin()) {
            startTutorial();
            return;
        }
//        decideLogin();
        startMain();
    }

    private void startMain() {
        Intent view = new Intent(this, MainActivity.class);
        view.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(view);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void startTutorial() {
        Intent view = new Intent(this, TutorialActivity.class);
        view.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(view);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
    }
}
