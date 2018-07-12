package com.tabio.tabioapp.coordinate;

import android.os.Bundle;
import android.view.MenuItem;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.ui.BaseActivity;

import java.util.HashMap;

import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/17/16.
 */
public class CoordinatesActivity extends BaseActivity {
    public static final String TAG = makeLogTag(CoordinatesActivity.class);

    private CoordinatesFragment coordinatesFragment;
    private ApiParams params;

    public CoordinatesActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinates);
        AppController.getInstance().sendGAScreen("コーディネート一覧");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.text_coordinates_title));
        ButterKnife.bind(this);

        HashMap<String, Object> map = (HashMap<String, Object>) getIntent().getSerializableExtra("params");
        this.params = new ApiParams(self, map, ApiRoute.GET_COORDINATE);

        this.coordinatesFragment = CoordinatesFragment.newInstance(this.params);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.coordinates_fragment, this.coordinatesFragment, CoordinatesFragment.TAG)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LOGD(TAG, "clicked home button");
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
