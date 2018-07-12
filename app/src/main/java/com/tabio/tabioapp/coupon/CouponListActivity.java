package com.tabio.tabioapp.coupon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.GcmObject;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Coupon;
import com.tabio.tabioapp.ui.BaseActivity;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class CouponListActivity extends BaseActivity implements CouponListFragment.CouponListCallbacks {
    public static final String TAG = makeLogTag(CouponListActivity.class);

    private CouponListFragment couponListFragment;
    private GcmObject gcmObject;

    public CouponListActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_list);
        AppController.getInstance().sendGAScreen("クーポン一覧");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.text_coupon_title));

        if (getIntent().getSerializableExtra("gcmObject") != null) {
            gcmObject = (GcmObject) getIntent().getSerializableExtra("gcmObject");
        }
        if (gcmObject != null) {
            if (!gcmObject.getIdentifier().isEmpty() && gcmObject.getIdentifier() != null) {
                float identifier = Float.parseFloat(gcmObject.getIdentifier());
                couponListFragment = CouponListFragment.newInstance(identifier);
            } else {
                couponListFragment = CouponListFragment.newInstance();
            }
            gcmObject = null;
        } else {
            couponListFragment = CouponListFragment.newInstance();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.coupon_list, couponListFragment, CouponListFragment.TAG)
                .commit();
    }

    @Override
    public void onCouponDetailButtonClicked(Coupon coupon) {
        CouponDetailDialog dialog = CouponDetailDialog.newInstance(coupon);
        dialog.show(getSupportFragmentManager(), CouponDetailDialog.TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LOGD(TAG, "clicked home button");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
