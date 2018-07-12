package com.tabio.tabioapp.me;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.GcmObject;
import com.tabio.tabioapp.MyMenuBus;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.item.ItemsFragment;
import com.tabio.tabioapp.login.BaseLoginActivity;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.preference.PreferencesActivity;
import com.tabio.tabioapp.util.CameraUtils;
import com.tabio.tabioapp.util.ImageUtils;
import com.tabio.tabioapp.me.MyMenu.MyMenuId;
import com.tabio.tabioapp.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class MyActivity extends BaseLoginActivity implements MyAccountsDialog.OnMyAccountsDialogCallbacks {
    private static final String TAG = makeLogTag(MyActivity.class);

    @BindView(R.id.cover)
    ImageView cover;
    @BindView(R.id.icon)
    CircleImageView icon;
    @BindView(R.id.nickname)
    TextView nickname;
    @BindView(R.id.account_edit_button)
    Button accountEditButton;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    private List<MyMenu> myMenus;
    private MyMenuPagerAdapter adapter;
    private GcmObject gcmObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        AppController.getInstance().sendGAScreen("マイページ");
        AppController.getInstance().decideTrack("570f300999c3634a425af503");
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(R.string.text_mypage_title);
        ((CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar)).setTitleEnabled(false);

        this.myMenus = new ArrayList<>();
        MyMenuBus.get().register(this);
        setupTabLayout();
        reloadMyInfo();
        getUser(false);
        chkMyAccounts();

        if (getIntent().getSerializableExtra("gcmObject") != null) {
            this.gcmObject = (GcmObject) getIntent().getSerializableExtra("gcmObject");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        self = AppController.getInstance().getSelf(true);
        reloadMyInfo();
    }

    private void chkMyAccounts() {
        boolean didSkipFirstTime = this.preferences.getBoolean("didSkipFirstTime", false);
        if (!didSkipFirstTime) {
            this.preferences.edit().putBoolean("didSkipFirstTime", true).commit();
            return;
        }
        String[] froms = new String[]{Route.FACEBOOK,Route.TWITTER,Route.EMAIL};
        for (String from : froms) {
            Route route = self.getManager().getRoute(from);
            if (route != null) {
                return;
            }
        }
        MyAccountsDialog.newInstance().show(getSupportFragmentManager(), MyAccountsDialog.TAG);
    }

    private void reloadMyInfo() {
        this.nickname.setText(self.getNickname());
        setProfileIcon(this, self.getProfile().getIconImgUrl(), icon);
        setProfileCover(this, self.getProfile().getCoverImgUrl(), cover);
        getNavigationHeaderView();
    }

    @OnClick(R.id.account_edit_button)
    void onAccountEditButtonClicked() {
        Intent intent = new Intent(this, AccountEditActivity.class);
        startActivity(intent);
    }

    private void setupTabLayout() {
        LOGD(TAG, "menu length is" + MyMenu.MYMENU_TAB_IDS.length);

        for (int i = 0; i < MyMenu.MYMENU_TAB_IDS.length; i++) {
            @MyMenuId int myMenuId = MyMenu.MYMENU_TAB_IDS[i];
            if (this.myMenus.size() == MyMenu.MYMENU_TAB_IDS.length) {
                MyMenu myMenu = this.myMenus.get(i);
                tabLayout.getTabAt(i).setText(getTab(myMenu).getText());
            } else {
                MyMenu myMenu = new MyMenu(myMenuId, this, self, 0);
                myMenus.add(myMenu);
                tabLayout.addTab(getTab(myMenu));
            }
        }
        if (adapter == null) {
            adapter = new MyMenuPagerAdapter(getSupportFragmentManager(), this, myMenus);
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
        }
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                adapter.notifyDataSetChanged();
//            }
//        });

        if (this.gcmObject != null) {
            try {
                int identifier = Integer.parseInt(gcmObject.convertedMyPageIdentifier());
                tabLayout.getTabAt(identifier).select();
                this.gcmObject = null;
            } catch (Exception e) {
                LOGE(TAG, e.getMessage());
            }
        }
    }

    public TabLayout.Tab getTab(MyMenu myMenu) {
        TabLayout.Tab tab = tabLayout.newTab();
        String count = "";
        if (myMenu.getObjectsCount() > 0) {
            count = "("+myMenu.getObjectsCount()+")";
        }
        tab.setText(myMenu.getName()+count);
        tab.setTag(myMenu.getMyMenuId());
        return tab;
    }

    @Subscribe
    public void updateMyMenu(MyMenu myMenu) {
        LOGD(TAG, "get objects count:"+myMenu.getObjectsCount());
        this.myMenus.get(myMenu.getMyMenuId()).setObjectsCount(myMenu.getObjectsCount());
        setupTabLayout();
    }

//    @Override
//    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from) {
//        getUser();
//    }


    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
        int action = args.getInt(NEXT_ACTION);
        if (action == NEXT_ACTION_UPDATE_PROFILE) {
            // アカウント連携完了
            if (from == Route.FACEBOOK) {
                AppController.getInstance().showAlert(this, getString(R.string.text_account_login_facebook_success), null);
            } else if (from == Route.TWITTER) {
                AppController.getInstance().showAlert(this, getString(R.string.text_account_login_twitter_success), null);
            } else if (from == Route.EMAIL) {
                AppController.getInstance().showAlert(this, getString(R.string.text_account_login_email_success), null);
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                reloadMyInfo();
            }
        });
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
        reloadMyInfo();
    }

    @Override
    protected void onRefreshedMyInfoFailed() {
    }

    @Override
    public void onFacebookLoginButtonClicked() {
        Bundle args = new Bundle();
        args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
        facebookAuthentication(args, true);
    }

    @Override
    public void onTwitterLoginButtonClicked() {
        Bundle args = new Bundle();
        args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
        twitterAuthentication(args, true);
    }


    @Override
    protected void onMigrateSuccess(@Route.From String from) {

    }

    @Override
    protected void onMigrateFail(@Route.From String from, @Nullable String message, ApiError error) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyMenuBus.get().unregister(this);
    }
}
