package com.tabio.tabioapp.help;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.web.WebActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ScreenHelpActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    public static final String TAG = makeLogTag(ScreenHelpActivity.class);


    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.page_controls)
    LinearLayout pageControls;

    private int helpId;
    private HelpDataModel data;
    private HelpDataAdapter adapter;

    public static final int HELP_MAIN = 0;
    public static final int[] MAIN_HELP_LAYOUT_IDS = new int[]{
            R.layout.fragment_screen_help_basic2,
            R.layout.fragment_screen_help_basic,
            R.layout.fragment_screen_help_basic,
    };
    public static final int[] MAIN_HELP_BASIC_STRING_IDS = new int[]{
            0,
            R.string.text_main_help_description_2_1,
            R.string.text_main_help_description_3_1,
    };
    public static final int[] MAIN_HELP_IMAGE_RES_IDS = new int[]{
            R.drawable.help_membership_text_01,
            R.drawable.help_membership_text_02,
            R.drawable.help_membership_text_03,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getActionBarTitle());
        ButterKnife.bind(this);

        helpId = getIntent().getIntExtra("helpId", -1);
        if (helpId == -1) {
            throw new RuntimeException(this.toString()
                    + " must Intent extra helpId");
        }

        if (helpId == HELP_MAIN) {
            this.data = new HelpDataModel(helpId, MAIN_HELP_LAYOUT_IDS, MAIN_HELP_BASIC_STRING_IDS, MAIN_HELP_IMAGE_RES_IDS);
        }
        adapter = new HelpDataAdapter(getSupportFragmentManager(), data);
        this.viewPager.setAdapter(adapter);
        setPageControlls();
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void setPageControlls() {
        for (int i = 0; i < adapter.getCount(); i++) {
            ImageView pageControl = new ImageView(this, null, R.attr.pageControlStyle);
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(
                            getResources().getDimensionPixelSize(R.dimen.pagecontrol_width),
                            getResources().getDimensionPixelSize(R.dimen.pagecontrol_height));
            int leftMargin = getResources().getDimensionPixelSize(R.dimen.pagecontrol_left_margin);
            int rightMargin = getResources().getDimensionPixelSize(R.dimen.pagecontrol_right_margin);
            layoutParams.setMargins(leftMargin, 0, rightMargin, 0);
            pageControl.setImageResource(R.drawable.pagecontrol);
            pageControl.setLayoutParams(layoutParams);
            pageControl.setId(i + i);// iだけだと取得できない
            pageControls.addView(pageControl);
        }
        setCurrentPage(0);
    }

    private void setCurrentPage(int page) {
        for (int i = 0; i < adapter.getCount(); i++) {
            ImageView pageControl = (ImageView) findViewById(i + i);
            pageControl.setBackgroundResource(i == page ? R.drawable.pagecontrol_selected : R.drawable.pagecontrol);
            pageControl.setImageResource(i == page ? R.drawable.pagecontrol_selected : R.drawable.pagecontrol);
        }
    }

    private String getActionBarTitle() {
        switch (helpId) {
            case HELP_MAIN:
                return getString(R.string.text_main_help_title);
            default:
                return "";
        }
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


    class HelpDataModel {
        private int helpId;
        private int[] layoutIds;
        private int[] stringsIds;
        private int[] imageResIds;

        public HelpDataModel(int helpId, int[] layoutIds, int[] stringsIds, int[] imageResIds) {
            this.helpId = helpId;
            this.layoutIds = layoutIds;
            this.stringsIds = stringsIds;
            this.imageResIds = imageResIds;
        }

        public int[] getLayoutIds() {
            return layoutIds;
        }

        public int getHelpId() {
            return helpId;
        }

        public int[] getStringsIds() {
            return stringsIds;
        }

        public int[] getImageResIds() {
            return imageResIds;
        }
    }


    class HelpDataAdapter extends FragmentStatePagerAdapter {

        private HelpDataModel data;

        public HelpDataAdapter(FragmentManager fm, HelpDataModel data) {
            super(fm);
            this.data = data;
        }

        @Override
        public Fragment getItem(int position) {
            return ScreenHelpFragment.newInstance(this.data.getHelpId(), this.data.getLayoutIds(), this.data.getStringsIds(), this.data.getImageResIds(), position);
        }

        @Override
        public int getCount() {
            return data.getLayoutIds().length;
        }
    }
}
