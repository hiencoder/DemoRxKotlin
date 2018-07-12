package com.tabio.tabioapp.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/18/16.
 */
public class MyIdActivity extends BaseActivity {
    public static final String TAG = makeLogTag(MyIdActivity.class);

    @BindView(R.id.tabioid)
    TextView tabioId;
    @BindView(R.id.pincode)
    TextView pinCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_id);
        AppController.getInstance().sendGAScreen("Tabio ID・PINメール保存");
        AppController.getInstance().decideTrack("570f309099c3634a425af50f");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.text_preference_title_savetabioidpin_save));
        ButterKnife.bind(this);

        this.tabioId.setText(self.getTabioId());
        this.pinCode.setText(self.getPinCode());
    }

    @OnClick(R.id.tabioidpin_send_button)
    void idPinSendButtonClicked() {
        Intent view = new Intent();
        view.setAction(Intent.ACTION_SEND);
        view.setType("text/plain");
        view.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.text_email_send_title));
        String body = getString(R.string.text_email_send_body, self.getTabioId(), self.getPinCode());
//        body += getString(R.string.text_account_profile_title_tabioId);
//        body += "\n"+self.getTabioId()+"\n \n";
//        body += getString(R.string.text_account_profile_title_pinCode);
//        body += "\n"+self.getPinCode();
        view.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(view);
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
