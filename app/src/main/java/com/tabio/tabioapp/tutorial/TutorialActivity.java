package com.tabio.tabioapp.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.login.LoginActivity;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.terms.TermsAgreementActivity;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.util.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGV;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class TutorialActivity extends BaseActivity
        implements ViewPager.OnPageChangeListener {
    private static final String TAG = makeLogTag(TutorialActivity.class);

    public static final String TUTORIAL_PAGE2_FILENAME = "tutorial_2";
    public static final String TUTORIAL_PAGE3_FILENAME = "tutorial_3";
    public static final String TUTORIAL_PAGE4_FILENAME = "tutorial_4";
    public static final String[] TUTORIAL_PAGES =
            {TUTORIAL_PAGE2_FILENAME, TUTORIAL_PAGE3_FILENAME, TUTORIAL_PAGE4_FILENAME};

    public static final int TUTORIAL_STRING1_RESID = R.string.text_tutorial_1;
    public static final int TUTORIAL_STRING2_RESID = R.string.text_tutorial_2;
    public static final int TUTORIAL_STRING3_RESID = R.string.text_tutorial_3;
    public static final int TUTORIAL_STRING4_RESID = R.string.text_tutorial_4;
    public static final int[] TUTORIAL_STRINGS =
            {TUTORIAL_STRING1_RESID, TUTORIAL_STRING2_RESID,
                    TUTORIAL_STRING3_RESID, TUTORIAL_STRING4_RESID};

    private boolean showLogo = true;

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.footer)
    View footerView;
    @BindView(R.id.page_controls)
    LinearLayout pageControls;
    @BindView(R.id.description)
    TextView descriptionTextView;
    @BindView(R.id.button_start)
    Button startButton;
    @BindView(R.id.close_button)
    Button closeButton;
    @BindView(R.id.login_view)
    View loginButton;


    private TutorialPagerAdapter tutorialPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        AppController.getInstance().sendGAScreen("トップ");
        AppController.getInstance().decideTrack("570f285899c3634a425af4a5");
        ButterKnife.bind(this);

        this.showLogo = getIntent().getBooleanExtra("showLogo", true);
        tutorialPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager(), TUTORIAL_PAGES, showLogo);
        viewPager.setAdapter(tutorialPagerAdapter);
        setPageControlls();
        viewPager.addOnPageChangeListener(this);
        this.startButton.setVisibility(showLogo ? View.VISIBLE : View.GONE);
        this.loginButton.setVisibility(this.startButton.getVisibility());
        this.closeButton.setVisibility(showLogo ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (self.isLogin() && !getIntent().getBooleanExtra("nocheck", false)) {
                    Intent view = new Intent(getApplicationContext(), MainActivity.class);
                    view.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(view);
                    finish();
                }
            }
        });

    }

    private void setPageControlls() {
        for (int i = 0; i < (showLogo ? 4 : 3); i++) {
            ImageView pageControl = new ImageView(this, null, R.attr.pageControlStyle);
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(
                            getResources().getDimensionPixelSize(R.dimen.pagecontrol_width),
                            getResources().getDimensionPixelSize(R.dimen.pagecontrol_height));
            int leftMargin = getResources().getDimensionPixelSize(R.dimen.pagecontrol_left_margin);
            int rightMargin = getResources().getDimensionPixelSize(R.dimen.pagecontrol_right_margin);
            layoutParams.setMargins(leftMargin, 0, rightMargin, 0);
            pageControl.setLayoutParams(layoutParams);
            pageControl.setId(TUTORIAL_STRINGS[i]);
            pageControls.addView(pageControl);
        }
        setCurrentPage(0);
    }

    private void setCurrentPage(int page) {
        for (int i = 0; i < (showLogo ? 4 : 3); i++) {
            ImageView pageControl = (ImageView) findViewById(TUTORIAL_STRINGS[i]);
            pageControl.setImageResource(i == page ? R.drawable.pagecontrol_selected : R.drawable.pagecontrol);
        }
        descriptionTextView.setText(getResources().getString(TUTORIAL_STRINGS[showLogo ? page : (page + 1)]));
        if (showLogo) {
            footerView.setBackgroundColor(page == 0 ?
                    getResources().getColor(R.color.transparent) :
                    getResources().getColor(R.color.white));
        } else {
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(ViewUtils.getPixelFromDp(this, 16),//left
                    ViewUtils.getPixelFromDp(this, 20),//top
                    ViewUtils.getPixelFromDp(this, 16),//right
                    ViewUtils.getPixelFromDp(this, 14)//bottom
            );
            descriptionTextView.setLayoutParams(llp);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        LOGD(TAG, "page selected. current page is " + position);
        setCurrentPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @OnClick(R.id.button_start)
    public void start() {
        LOGD(TAG, "clicked start button");
        readTerms(MainActivity.TAG);
    }

    @OnClick(R.id.login_view)
    public void login() {
        LOGD(TAG, "clicked login button");
        readTerms(LoginActivity.TAG);
    }

    @OnClick(R.id.close_button)
    void onCloseButtonClicked() {
        finish();
    }

    public void readTerms(String tag) {
        Intent view = new Intent(getApplicationContext(), TermsAgreementActivity.class);
        view.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        view.putExtra("tag", tag);
        startActivity(view);
        finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
    }
}
