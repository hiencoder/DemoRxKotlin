package com.tabio.tabioapp.preference;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.login.BaseLoginActivity;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.top.TopActivity;
import com.tabio.tabioapp.util.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class EmailPasswordRegisterActivity extends BaseLoginActivity {
    public static final String TAG = makeLogTag(EmailPasswordRegisterActivity.class);

    private ApiRequest request;

    @BindView(R.id.id_title)
    TextView idTitle;
    @BindView(R.id.id_input)
    EditText idInput;
    @BindView(R.id.password_title)
    TextView passwordTitle;
    @BindView(R.id.password_input)
    EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password_register);
        AppController.getInstance().sendGAScreen("Email&Password連携");
        AppController.getInstance().decideTrack("570f309a99c3634a425af511");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        this.request = new ApiRequest(this);

        getSupportActionBar().setTitle(R.string.text_account_email_register);
    }

    @OnTextChanged(R.id.id_input)
    void onIdTextChanged(CharSequence text) {
    }

    @OnTextChanged(R.id.password_input)
    void onPasswordTextChanged(CharSequence text) {
    }

    @OnClick(R.id.register_button)
    void onRegisterButtonClicked() {

        // バリデーション
        {
            if (this.idInput.getText().length() < 1) {
                AppController.getInstance().showAlert(this, getString(R.string.error_text_input_email), null);
                return;
            }

            if (!StringUtils.isValidEmail(this.idInput.getText().toString())) {
                AppController.getInstance().showAlert(this, getString(R.string.text_account_email_validation), null);
                return;
            }

            if (this.passwordInput.getText().length() < 1) {
                AppController.getInstance().showAlert(this, getString(R.string.error_text_input_password2), null);
                return;
            }

            if (!StringUtils.isValidPassword(this.passwordInput.getText().toString())) {
                AppController.getInstance().showAlert(this, getString(R.string.text_account_password_validation), null);
                return;
            }
        }

        // Email・Passwordで会員情報の更新
        ApiParams params = self.getManager().getUpdateProfileParams();
        params.put("email", this.idInput.getText().toString());
        params.put("password", this.passwordInput.getText().toString());
        params.put("icon", self.getProfile().getIconImgBlob());
        params.put("cover", self.getProfile().getCoverImgBlob());
        AppController.getInstance().showSynchronousProgress(this);
        this.request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        AppController.getInstance().showAlert(EmailPasswordRegisterActivity.this, getString(R.string.text_account_login_email_fail), null);
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showAlert(EmailPasswordRegisterActivity.this, getString(R.string.text_account_login_email_fail), null);
                        } else {
                            getUser(false);
                        }
                    }
                });
    }

    @Override
    protected void onMigrateSuccess(@Route.From String from) {
    }

    @Override
    protected void onMigrateFail(@Route.From String from, @Nullable String message, ApiError error) {
        AppController.getInstance().showAlert(EmailPasswordRegisterActivity.this, getString(R.string.text_account_login_fail), null);
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
        LOGD(TAG, "updateProfileSuccessed");
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
        AppController.getInstance().showAlert(this, getString(R.string.text_account_login_email_success), null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
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
