package com.tabio.tabioapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tabio.tabioapp.util.StringUtils;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 5/15/16.
 */
public class MaintenanceActivity extends AppCompatActivity {
    public static final String TAG = makeLogTag(MaintenanceActivity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getInstance().sendGAScreen("メンテナンス");
        AppController.getInstance().decideTrack("570f312699c3634a425af521");
        setContentView(R.layout.activity_maintenance);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
