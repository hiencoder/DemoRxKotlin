package com.tabio.tabioapp.preference;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.GcmObject;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.login.BaseLoginActivity;
import com.tabio.tabioapp.me.MyIdActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.top.TopActivity;
import com.tabio.tabioapp.web.WebActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class PreferencesActivity extends BaseLoginActivity {
    private static final String TAG = makeLogTag(PreferencesActivity.class);

    @BindView(R.id.accounts_title)
    TextView accountsTitle;
    @BindView(R.id.login_title)
    TextView loginTitle;
    @BindView(R.id.onlinelogin_title)
    TextView onlineTitle;
    @BindView(R.id.settings_title)
    TextView settingsTitle;
    @BindView(R.id.help_title)
    TextView helpTitle;

    @BindView(R.id.facebook_login_button)
    View facebookLoginButton;
    @BindView(R.id.facebook_migrate_button)
    View facebookMigrateButton;
    @BindView(R.id.twitter_login_button)
    View twitterLoginButton;
    @BindView(R.id.twitter_migrate_button)
    View twitterMigrateButton;

    @BindView(R.id.facebook_login_button_disable)
    View facebookLoginDisableButton;
    @BindView(R.id.twitter_login_button_disable)
    View twitterLoginDisableButton;

    @BindView(R.id.scrollview)
    NestedScrollView scrollView;

    private boolean isLoading = false;
    private boolean isMigrated = false;
    private GcmObject gcmObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        AppController.getInstance().sendGAScreen("その他一覧");
        AppController.getInstance().decideTrack("570f307299c3634a425af50d");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.text_faq_title_9));
        ButterKnife.bind(this);

        this.accountsTitle.setText(getString(R.string.text_preference_title_accounts));
        this.loginTitle.setText(getString(R.string.button_login));
        this.onlineTitle.setText(getString(R.string.text_preference_title_online2));
        this.settingsTitle.setText(getString(R.string.text_preference_title_settings));
        this.helpTitle.setText(getString(R.string.button_help));

        refreshView();

        scrollView.setSmoothScrollingEnabled(true);
        if (getIntent().getSerializableExtra("gcmObject") != null) {
            gcmObject = (GcmObject) getIntent().getSerializableExtra("gcmObject");
            final String identifier = gcmObject.getIdentifier();
            gcmObject = null;

            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    ViewTreeObserver observer = scrollView.getViewTreeObserver();
                    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            final int y;
                            if (identifier.equals("1")) {
                                y = 0;
                            } else if (identifier.equals("2")) {
                                y = 700;
                            } else if (identifier.equals("3")) {
                                y = 0;
                            } else if (identifier.equals("4")) {
                                y = 700;
                            } else {
                                y = 0;
                            }
                            scrollView.scrollTo(0, y);
                        }
                    });
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isLoading = false;
    }

    private void refreshView() {
        if (self.getManager().getRoute(Route.FACEBOOK) != null) {
            this.facebookLoginDisableButton.setVisibility(View.VISIBLE);
        }
        if (self.getManager().getRoute(Route.TWITTER) != null) {
            this.twitterLoginDisableButton.setVisibility(View.VISIBLE);
        }
        // Emailは登録済み画面があるため、必要ない
//        if (self.getManager().getRoute(Route.EMAIL) != null) {}
    }

    @OnClick(R.id.tabioidpin_save_button)
    void onTabioIdPinSaveButtonClicked() {
        Intent view = new Intent(this, MyIdActivity.class);
        startActivity(view);
    }

    @OnClick(R.id.facebook_login_button)
    void onFacebookLoginButtonClicked() {
        if (this.facebookLoginDisableButton.getVisibility() == View.VISIBLE) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;
        Bundle args = new Bundle();
        args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
        facebookAuthentication(args, true);
    }

    @OnClick(R.id.twitter_login_button)
    void onTwitterLoginButtonClicked() {
        if (this.twitterLoginDisableButton.getVisibility() == View.VISIBLE) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;
        Bundle args = new Bundle();
        args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
        twitterAuthentication(args, true);
    }

    @OnClick(R.id.email_password_register_button)
    void onEmailPasswordRegisterButtonClicked() {
        toEmailPasswordRegisterScreen();
    }

    @OnClick(R.id.tabioidpin_migrate_button)
    void onTabioIdPinMigrateButtonClicked() {
        Intent view = new Intent(this, MigrationActivity.class);
        view.putExtra("mode", MigrationActivity.MIGRATION_IDPIN);
        AppController.getInstance().sendGAScreen("TabioID,ピンコードでログイン");
        AppController.getInstance().decideTrack("570f30b799c3634a425af515");
        startActivity(view);
    }

    @OnClick(R.id.facebook_migrate_button)
    void onFacebookMigrateButtonClicked() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        Bundle args = new Bundle();
        args.putInt(NEXT_ACTION, NEXT_ACTION_MIGRATE);
        facebookAuthentication(args, false);
    }

    @OnClick(R.id.twitter_migrate_button)
    void onTwitterMigrateButtonClicked() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        Bundle args = new Bundle();
        args.putInt(NEXT_ACTION, NEXT_ACTION_MIGRATE);
        twitterAuthentication(args, false);
    }

    @OnClick(R.id.emailpassword_migrate_button)
    void onEmailPasswordMigrateButtonClicked() {
        Intent view = new Intent(this, MigrationActivity.class);
        view.putExtra("mode", MigrationActivity.MIGRATION_EMAILPASSWORD);
        startActivity(view);
    }

    @OnClick(R.id.online_migrate_button)
    void onOnlineMigrateButtonClicked() {
        AppController.getInstance().decideTrack("570f30af99c3634a425af513");
        AppController.getInstance().sendGAScreen("オンラインストア会員連携/ログイン");
        Intent view = new Intent(this, MigrationActivity.class);
        view.putExtra("mode", MigrationActivity.MIGRATION_ONLINE);
        startActivity(view);
    }

    @OnClick(R.id.notification_settings_button)
    void onNotificationSettingsButtonClicked() {
        Intent view = new Intent(this, NotificationSettingsActivity.class);
        startActivity(view);
    }

    @OnClick(R.id.language_settings_button)
    void onLanguageSettingsButtonClicked() {
        Intent view = new Intent(this, LanguageSettingsActivity.class);
        startActivity(view);
    }

    @OnClick(R.id.faqcontact_button)
    void onFaqContactButtonClicked() {
        AppController.getInstance().sendGAScreen("FAQ");
        AppController.getInstance().decideTrack("570f315d99c3634a425af529");
        Intent view = new Intent(this, FaqActivity.class);
        startActivity(view);
    }

    @OnClick({R.id.terms_button, R.id.policy_button, R.id.tokushou_button})
    void webButtonClicked(View v) {
        Intent view = new Intent(this, WebActivity.class);
        String url = "";
        switch (v.getId()) {
            case R.id.terms_button:
                AppController.getInstance().sendGAScreen("会員規約");
                AppController.getInstance().decideTrack("570f313b99c3634a425af523");
                url = ApiRoute.WV_TERMS;
                break;
            case R.id.policy_button:
                AppController.getInstance().sendGAScreen("情報セキュリティポリシー");
                AppController.getInstance().decideTrack("570f314499c3634a425af525");
                url = ApiRoute.WV_SECURITY_POLICY;
                break;
            case R.id.tokushou_button:
                AppController.getInstance().sendGAScreen("特定商取引法に基づく表示");
                AppController.getInstance().decideTrack("570f314e99c3634a425af527");
                url = ApiRoute.WV_SPECIFIC;
                break;
        }
        view.putExtra("url", BuildConfig.EC_URL + url);
        startActivity(view);
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
        int action = args.getInt(NEXT_ACTION);
        isLoading = false;
        if (action == NEXT_ACTION_UPDATE_PROFILE) {
            // アカウント連携完了
            if (from == Route.FACEBOOK) {
                AppController.getInstance().showAlert(this, getString(R.string.text_account_login_facebook_success), null);
            } else if (from == Route.TWITTER) {
                AppController.getInstance().showAlert(this, getString(R.string.text_account_login_twitter_success), null);
            } else if (from == Route.EMAIL) {
                AppController.getInstance().showAlert(this, getString(R.string.text_account_login_email_success), null);
            }
            refreshView();
        } else if (action == NEXT_ACTION_MIGRATE) {
//            Route route = self.getManager().getRoute(from);
//            migrate(route.getProviderId(), route.getSecurityKey(), from);
        }
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
        isLoading = false;
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
        if (isMigrated) {
            AppController.getInstance().showAlert(this, getString(R.string.text_account_login_success), null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent view = new Intent(PreferencesActivity.this, TopActivity.class);
                    view.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(view);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onRefreshedMyInfoFailed() {

    }

    @Override
    public void onMigrateSuccess(@Route.From String from) {
        AppController.getInstance().showSynchronousProgress(this);
        isLoading = false;
        isMigrated = true;
        getUser(true);
    }

    @Override
    public void onMigrateFail(@Route.From String from, @Nullable String message, ApiError error) {
        isLoading = false;
        isMigrated = false;
        AppController.getInstance().showAlert(this, getString(R.string.text_account_login_fail), null);
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