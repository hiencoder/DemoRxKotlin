package com.tabio.tabioapp.terms;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.login.LoginActivity;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.me.MyBaseActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.util.ImageUtils;
import com.tabio.tabioapp.web.WebFragment;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class TermsAgreementActivity extends MyBaseActivity implements WebFragment.OnWebFragmentCallbacks {
    private static final String TAG = makeLogTag(TermsAgreementActivity.class);

    @BindView(R.id.agree_button)
    Button agreeButton;

    private ApiRequest apiRequest;
    private WebFragment webFragment;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        AppController.getInstance().sendGAScreen("会員規約同意");
        ButterKnife.bind(this);
        this.apiRequest = new ApiRequest(this);

        getSupportActionBar().setTitle(getString(R.string.text_terms_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppController.getInstance().decideTrack("570f287c99c3634a425af4a7");

        agreeButton.setEnabled(false);
        webFragment = (WebFragment) getSupportFragmentManager().findFragmentById(R.id.web_fragment);
        webFragment.setWebRequest(BuildConfig.EC_URL + ApiRoute.WV_TERMS);
        showNetworkProgress();
    }


    @Override
    public void onWebLoadFinished(String url, String title) {
        hideProgress();
        agreeButton.setEnabled(true);
    }

    @OnClick(R.id.agree_button)
    public void onAgreeButtonClicked() {
        LOGD(TAG, "clicked agree button");
        if (isLoading) {
            return;
        }
        isLoading = true;
        AppController.getInstance().showSynchronousProgress(this);
        this.apiRequest.run(
                new ApiParams(self, false, ApiRoute.REGISTER))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(registerCallback);
    }

    public Observer<ApiResponse> registerCallback = new Observer<ApiResponse>() {
        @Override
        public void onCompleted() {
            LOGD(TAG, "onCompleted");
        }

        @Override
        public void onError(final Throwable e) {
            LOGE(TAG, "ErrorHandling:" + e.getLocalizedMessage());
            isLoading = false;
            AppController.getInstance().showApiErrorAlert(TermsAgreementActivity.this, ApiError.newNetworkErrorApiError());
//            Toast.makeText(TermsAgreementActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(ApiResponse apiResponse) {
            hideProgress();
            try {
                LOGD(TAG, "onNext:" + apiResponse.getBody().toString(4));
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
            }
            if (apiResponse.hasError()) {
                isLoading = false;
                AppController.getInstance().showApiErrorAlert(TermsAgreementActivity.this, ApiError.newUndefinedApiError());
                return;
            }
            try {
                JSONObject result = apiResponse.getBody().getJSONObject("result");
                self = AppController.getInstance().getSelf(true);
                if (!self.getManager().save(result, true)) {
                    LOGE(TAG, "save error");
                    isLoading = false;
                    return;
                } else {
                    register();
                }
            } catch (JSONException e) {
                LOGE(TAG, e.getMessage());
                AppController.getInstance().showApiErrorAlert(TermsAgreementActivity.this, ApiError.newUndefinedApiError());
                isLoading = false;
                return;
            }
        }
    };

    private void register() {
        try {
            Bitmap icon = BitmapFactory.decodeResource(AppController.getInstance().getResources(), R.drawable.ic_mypage_green_square);
            String encodededProfilePic = ImageUtils.getBase64FromBitmap(icon);
            self.getProfile().setIconImgBlob(encodededProfilePic);
            self.getProfile().getManager().save();
            self.setReceiveNews(true);
            self.setLogin(true);
            self.getManager().save();
            updateProfile(null, null);
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
            isLoading = false;
            AppController.getInstance().showAlert(this, getString(R.string.error), getString(R.string.error_text_undefined), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AppController.getInstance().deleteSelfAndBackToTop();
                }
            });
        }
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
        String tag = getIntent().getStringExtra("tag");
        Intent view = null;
        if (tag.equals(MainActivity.TAG)) {
            view = new Intent(this, MainActivity.class);
            view.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            view = new Intent(TermsAgreementActivity.this, LoginActivity.class);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        startActivity(view);
        finish();
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
        AppController.getInstance().showAlert(this, getString(R.string.error), getString(R.string.error_text_undefined), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppController.getInstance().deleteSelfAndBackToTop();
            }
        });
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
    }

    @Override
    protected void onRefreshedMyInfoFailed() {
    }

    @Override
    public void onBackPressed() {
        backToTutorial();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backToTutorial();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
