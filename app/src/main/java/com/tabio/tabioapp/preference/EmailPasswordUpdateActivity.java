package com.tabio.tabioapp.preference;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.me.MyBaseActivity;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.web.WebActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class EmailPasswordUpdateActivity extends MyBaseActivity {
    public static final String TAG = makeLogTag(EmailPasswordUpdateActivity.class);

    @BindView(R.id.email)
    TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password_update);
        AppController.getInstance().sendGAScreen("Email&Password連携済み画面");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.text_account_email_register);
        ButterKnife.bind(this);

        this.email.setText(self.getManager().getRoute(Route.EMAIL).getProviderId());
    }

    @OnClick(R.id.update_emailpassword_button)
    void onEmailPasswordUpdateButtonClicked() {
        Intent view = new Intent(this, WebActivity.class);
        view.putExtra("url", BuildConfig.BASE_URL+ApiRoute.WV_UPDATE_EMAIL);
        startActivity(view);
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
    }

    @Override
    protected void onRefreshedMyInfoFailed() {
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
