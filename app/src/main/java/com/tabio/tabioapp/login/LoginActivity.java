package com.tabio.tabioapp.login;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.about.AboutAccountActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.util.StringUtils;
import com.tabio.tabioapp.web.WebActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class LoginActivity extends BaseLoginActivity {
    public static final String TAG = makeLogTag(LoginActivity.class);

    @BindView(R.id.id_input)
    EditText idInput;
    @BindView(R.id.password_input)
    EditText passwordInput;
    @BindView(R.id.login_button)
    Button loginButton;
    @BindView(R.id.reset_password_button)
    Button resetPasswordButton;
    @BindView(R.id.about_account_button)
    Button aboutAccountButton;
    @BindView(R.id.login_title)
    TextView loginFormTitle;
    @BindView(R.id.sns_login_title)
    TextView snsLoginFormTitle;
    @BindView(R.id.facebook_login_button)
    ImageButton facebookLoginButton;
    @BindView(R.id.twitter_login_button)
    ImageButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppController.getInstance().sendGAScreen("ログイン");
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.button_login);

        loginFormTitle.setText(getString(R.string.text_login_form_title));
        snsLoginFormTitle.setText(getString(R.string.text_login_form_sns_title));
        resetPasswordButton.setPaintFlags(resetPasswordButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        aboutAccountButton.setPaintFlags(aboutAccountButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        LOGD(TAG, "icon:" + self.getProfile().getIconImgBlob());
        LOGD(TAG, "cover:" + self.getProfile().getCoverImgBlob());
    }

    @OnTextChanged(R.id.id_input)
    public void onIdInputTextChanged(CharSequence text) {
        LOGD(TAG, "id input text changed. changed text=" + text);
    }

    @OnTextChanged(R.id.password_input)
    public void onPasswordInputTextChanged(CharSequence text) {
        LOGD(TAG, "password input text changed. changed text=" + text);
    }

    @OnClick(R.id.login_button)
    public void onLoginButtonClicked() {
        if (isShowingNetworkProgress()) {
            return;
        }
        if (idInput.getText().toString().isEmpty()) {
            LOGE(TAG, "id or email are empty");
            AppController.getInstance().showAlert(this, getString(R.string.error), getString(R.string.error_text_input_id));
            return;
        }
        if (passwordInput.getText().toString().isEmpty()) {
            LOGE(TAG, "password is empty");
            AppController.getInstance().showAlert(this, getString(R.string.error), getString(R.string.error_text_input_password));
            return;
        }
        LOGD(TAG, "clicked login button");
        migrate(this.idInput.getText().toString(), this.passwordInput.getText().toString(), Route.EMAIL);
    }

    @OnClick(R.id.reset_password_button)
    public void onResetPasswordButtonClicked() {
        if (isShowingNetworkProgress()) {
            return;
        }
        LOGD(TAG, "clicked reset password button");
        AppController.getInstance().decideTrack("570f28d999c3634a425af4ab");
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url", BuildConfig.BASE_URL + ApiRoute.WV_RESET_PASSWORD);
        AppController.getInstance().sendGAScreen("パスワードを忘れた方はこちら");
        startActivity(intent);
    }

    @OnClick(R.id.about_account_button)
    public void onAboutAccountButtonClicked() {
        LOGD(TAG, "clicked about account button");
        AppController.getInstance().decideTrack("570f294499c3634a425af4b2");
        Intent intent = new Intent(this, AboutAccountActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.facebook_login_button)
    public void onFacebookLoginButtonClicked() {
        LOGD(TAG, "clicked facebook login button");
        if (isShowingNetworkProgress()) {
            return;
        }
        Bundle args = new Bundle();
        args.putInt(NEXT_ACTION, NEXT_ACTION_MIGRATE);
        facebookAuthentication(args, false);
    }

    @Override
    public void onFacebookLoginFail(Bundle args) {
        super.onFacebookLoginFail(args);
    }

    @Override
    public void onFacebookLoginCancel(Bundle args) {
        super.onFacebookLoginCancel(args);
    }

    @OnClick(R.id.twitter_login_button)
    public void onTwitterLoginButtonClicked() {
        if (isShowingNetworkProgress()) {
            return;
        }
        LOGD(TAG, "clicked twitter login button");
        Bundle args = new Bundle();
        args.putInt(NEXT_ACTION, NEXT_ACTION_MIGRATE);
        twitterAuthentication(args, false);
    }

    @Override
    public void onTwitterLoginFail(Bundle args) {
        super.onTwitterLoginFail(args);
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
        try {
            self.setReceiveNews(true);
            self.setLogin(true);
            self.getManager().save();
            self = AppController.getInstance().getSelf(true);
            updateProfile(null, null);
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        }
    }

    @Override
    protected void onRefreshedMyInfoFailed() {
    }

    @Override
    protected void onMigrateSuccess(@Route.From String from) {
        AppController.getInstance().showSynchronousProgress(this);
        getUser(true);
    }

    @Override
    protected void onMigrateFail(@Route.From String from, @Nullable String message, ApiError error) {
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
        AppController.getInstance().showAlert(this, title, message);
        hideProgress();
    }


    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
        if (self.isLogin()) {
            Intent view = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(view);
            finish();
        }
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
        hideProgress();
        AppController.getInstance().dismissProgress();
    }

    @Override
    public void onBackPressed() {
        backToTutorial();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backToTutorial();
        }
        return super.onOptionsItemSelected(item);
    }
}
