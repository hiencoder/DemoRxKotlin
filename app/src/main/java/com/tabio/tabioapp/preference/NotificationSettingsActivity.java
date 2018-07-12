package com.tabio.tabioapp.preference;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.api.decide.DecideApiParams;
import com.tabio.tabioapp.api.decide.DecideApiRequest;
import com.tabio.tabioapp.api.decide.DecideApiResponse;
import com.tabio.tabioapp.me.MyBaseActivity;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class NotificationSettingsActivity extends MyBaseActivity {
    private static final String TAG = makeLogTag(NotificationSettingsActivity.class);

    @BindView(R.id.notification_push_title)
    TextView pushTitle;
    @BindView(R.id.notification_mail_title)
    TextView mailTitle;
    @BindView(R.id.push_switch)
    SwitchCompat pushSwitch;
    @BindView(R.id.mail_switch)
    SwitchCompat mailSwitch;

    int switchChangeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        AppController.getInstance().sendGAScreen("プッシュ通知・メール設定");
        AppController.getInstance().decideTrack("570f30cd99c3634a425af519");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.text_preference_title_settings);
        ButterKnife.bind(this);

        this.pushTitle.setText(getString(R.string.text_notification_title_push));
        this.mailTitle.setText(getString(R.string.text_notification_title_mail));

        updateSwitchStatus();
        AppController.getInstance().showSynchronousProgress(this);
        getUser(true);
    }

    @OnCheckedChanged({R.id.push_switch, R.id.mail_switch})
    void onSwitchChanged(CompoundButton v, boolean isChecked) {
        switchChangeCount ++;
        String which = "";
        if (v.getId() == R.id.push_switch) {
            if (self.isReceiveNews() == isChecked) {
                return;
            }
            self.setReceiveNews(isChecked);
            which = "news";
        } else if (v.getId() == R.id.mail_switch) {
            if (self.isReceiveMailMagazine() == isChecked) {
                return;
            }
            self.setReceiveMailMagazine(isChecked);
            which = "mail";
        }
        LOGD(TAG, "switchChanged:"+isChecked);
        try {
            self.getManager().save();
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
        Bundle args = new Bundle();
        args.putString("which", which);
        args.putBoolean("showAlert", switchChangeCount>2?true:false);
        updateProfile(null, args);
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
        boolean showAlert = args.getBoolean("showAlert", false);
        if (showAlert) {
            String which = args.getString("which");
            AppController.getInstance().showAlert(this, getString(which.equals("news")?R.string.text_account_push_change:R.string.text_account_mailmagazine_change), null);
        }
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
        AppController.getInstance().dismissProgress();
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
        AppController.getInstance().dismissProgress();
        updateSwitchStatus();
    }

    @Override
    protected void onRefreshedMyInfoFailed() {
    }

    private void updateSwitchStatus() {
        LOGD(TAG, "push:"+self.isReceiveNews());
        LOGD(TAG, "mail:"+self.isReceiveMailMagazine());
        pushSwitch.setChecked(self.isReceiveNews());
        mailSwitch.setChecked(self.isReceiveMailMagazine());
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
