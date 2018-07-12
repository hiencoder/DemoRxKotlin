package com.tabio.tabioapp.preference;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.login.BaseLoginActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.top.TopActivity;
import com.tabio.tabioapp.util.StringUtils;
import com.tabio.tabioapp.web.WebActivity;

import org.json.JSONException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class MigrationActivity extends BaseLoginActivity {
    public static final String TAG = makeLogTag(MigrationActivity.class);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MIGRATION_IDPIN, MIGRATION_EMAILPASSWORD, MIGRATION_ONLINE})
    public @interface MigrationMode{}
    public static final int MIGRATION_IDPIN = 1;
    public static final int MIGRATION_EMAILPASSWORD = 2;
    public static final int MIGRATION_ONLINE = 3;

    private int migrationMode;

    @BindView(R.id.id_title)
    TextView idTitle;
    @BindView(R.id.password_title)
    TextView passwordTitle;
    @BindView(R.id.id_input)
    EditText idInput;
    @BindView(R.id.pin_input)
    EditText pinInput;
    @BindView(R.id.reset_password_button)
    Button resetPasswordButton;

    private boolean isMigrated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        int titleId = 0;//navi
        int idTitleId = 0;//tabioId or メールアドレス
        int passTitleId = 0;//PINコード or パスワード
        int idInputId = 0; // TabioIDを入力 or メールアドレスを入力
        int passInputId = 0; // PINコードを入力 or パスワードを入力
        this.migrationMode = getIntent().getIntExtra("mode", MIGRATION_IDPIN);
        switch (this.migrationMode) {
            case MIGRATION_IDPIN:
                AppController.getInstance().sendGAScreen("TabioID,ピンコードでログイン");
                titleId = R.string.text_preference_title_login_tabio;
                idTitleId = R.string.text_account_my_id;
                passTitleId = R.string.text_account_my_pin;
                idInputId = R.string.text_account_my_id_input;
                passInputId = R.string.text_account_my_pin_input;
                break;
            case MIGRATION_EMAILPASSWORD:
                AppController.getInstance().sendGAScreen("メールアドレス・パスワードでログイン");
                titleId = R.string.text_preference_title_login2_email;
                idTitleId = R.string.text_account_email;
                passTitleId = R.string.text_account_password;
                idInputId = R.string.text_account_email_input;
                passInputId = R.string.text_account_password_input;
                break;
            case MIGRATION_ONLINE:
                AppController.getInstance().sendGAScreen("オンラインストア会員連携/ログイン");
                titleId = R.string.text_preference_title_online2;
                idTitleId = R.string.text_account_email;
                passTitleId = R.string.text_account_password;
                idInputId = R.string.text_account_email_input;
                passInputId = R.string.text_account_password_input;
                resetPasswordButton.setVisibility(View.VISIBLE);
                break;
        }
        getSupportActionBar().setTitle(titleId);
        this.idTitle.setText(getString(idTitleId));
        this.passwordTitle.setText(getString(passTitleId));
        this.idInput.setHint(getString(idInputId));
        this.pinInput.setHint(getString(passInputId));
    }

    @OnClick(R.id.migrate_button)
    void migrateButtonClicked() {
        // バリデーション
        {
            if (this.idInput.getText().length() < 1) {
                int resId = 0;
                switch (this.migrationMode) {
                    case MIGRATION_IDPIN:
                        resId = R.string.error_text_input_tabioid;
                        break;
                    case MIGRATION_EMAILPASSWORD:
                        resId = R.string.error_text_input_email;
                        break;
                    case MIGRATION_ONLINE:
                        resId = R.string.error_text_input_email;
                        break;
                }
                AppController.getInstance().showAlert(this,getString(resId), null);
                return;
            }
            if (this.pinInput.getText().length() < 1) {
                int resId = 0;
                switch (this.migrationMode) {
                    case MIGRATION_IDPIN:
                        resId = R.string.error_text_input_pin;
                        break;
                    case MIGRATION_EMAILPASSWORD:
                        resId = R.string.error_text_input_password2;
                        break;
                    case MIGRATION_ONLINE:
                        resId = R.string.error_text_input_password2;
                        break;
                }
                AppController.getInstance().showAlert(this,getString(resId), null);
                return;
            }

            if (this.migrationMode == MIGRATION_EMAILPASSWORD || this.migrationMode == MIGRATION_ONLINE) {
                if (!StringUtils.isValidEmail(this.idInput.getText().toString())) {
                    AppController.getInstance().showAlert(this,getString(R.string.text_account_email_validation), null);
                    return;
                }
                if (!StringUtils.isValidPassword(this.pinInput.getText().toString())) {
                    AppController.getInstance().showAlert(this,getString(R.string.text_account_password_validation), null);
                    return;
                }
            }
        }
        migrate(this.idInput.getText().toString(), pinInput.getText().toString(), Route.EMAIL);
    }

    @Override
    protected void onMigrateSuccess(@Route.From String from) {
        LOGD(TAG, "onMigrateSuccess");
        AppController.getInstance().showSynchronousProgress(this);
        isMigrated = true;
        getUser(true);
    }

    @Override
    protected void onMigrateFail(@Route.From String from, @Nullable String message, ApiError error) {
        LOGD(TAG, "onMigrateFail:"+self.getStatus());
        self = AppController.getInstance().getSelf(true);
        LOGD(TAG, "status:"+self.getStatus());
        if (self.getStatus() == Me.LEAVED) {
            // 退会済みのユーザにログインしようとして、退会ステータスが返ってきてしまっているので、このままだとただ失敗しただけなのに、トップに飛ばされちゃう
            self.setStatus(Me.ACTIVE);
            try {
                self.getManager().save();
            } catch (Exception e) {
                e.printStackTrace();
                LOGE(TAG, e.getMessage());
            }
        }
        switch (from) {
            case Route.FACEBOOK:
                message = getString(R.string.text_account_login_facebook_fail);
                break;
            case Route.TWITTER:
                message = getString(R.string.text_account_login_twitter_fail);
                break;
            case Route.EMAIL:
                message = getString(R.string.text_error_account_migrate);
                break;
        }
        String title = getString(R.string.error);
        if (BuildConfig.DEBUG) {
            if (error != null) {
                int code = error.getCode();
                title += "[" + String.valueOf(code) + "]";
            }
        }
        isMigrated = false;
        AppController.getInstance().showAlert(this, title, message);
        hideProgress();
    }

    @OnClick(R.id.reset_password_button)
    void onResetPasswordButtonClicked() {
        AppController.getInstance().decideTrack("570f28d999c3634a425af4ab");
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url", BuildConfig.BASE_URL + ApiRoute.WV_RESET_PASSWORD);
        AppController.getInstance().sendGAScreen("パスワードを忘れた方はこちら");
        startActivity(intent);
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {

    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {

    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
        if (isMigrated) {
            AppController.getInstance().showAlert(this, getString(R.string.text_account_login_success), null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent view = new Intent(MigrationActivity.this, TopActivity.class);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LOGD(TAG, "clicked home button");
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
