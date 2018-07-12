package com.tabio.tabioapp.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.DebugActivity;
import com.tabio.tabioapp.GcmRegistrationService;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.checkin.CheckinActivity;
import com.tabio.tabioapp.coordinate.CoordinatesCollectionActivity;
import com.tabio.tabioapp.gps.GpsService;
import com.tabio.tabioapp.util.CameraUtils;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.item.ItemsActivity;
import com.tabio.tabioapp.me.MyActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONObject;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

abstract public class BaseActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = makeLogTag(BaseActivity.class);

    private Toolbar actionBarToolBar;
    private NavigationView navigationView;
    private View navigationHeaderView;
    protected Me self;
    protected Handler handler;
    private ProgressBar networkProgress;
    protected SharedPreferences preferences;
    protected View noDataView;

    public static final int NAV_MAIN = 0;
    public static final int NAV_COORDINATES = 1;
    public static final int NAV_CHECKIN = 2;
    public static final int NAV_ITEMS = 3;
    public static final int NAV_DEBUG = 4;
    public static final int[] NAV_ICON_LIST = new int[]{R.drawable.ic_menu_membership, R.drawable.ic_menu_scancode,
            R.drawable.ic_menu_checkin, R.drawable.ic_menu_item, R.drawable.ic_etc_tabio};
    public static final int[] NAV_TITLE_LIST = new int[]{R.string.text_main_title, R.string.text_coordinate_title,
            R.string.text_checkin_title, R.string.text_items2, R.string.debug};
    private ListView navigationListView;

    private static final int NAVDRAWER_LAUNCH_DELAY = 300;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;

    protected static final int REQUEST_CAMERA_PERMISSION = 124;
    protected static final int REQUEST_STORAGE_PERMISSION = 125;

    protected boolean allowUseCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.self = AppController.getInstance().getSelf(false);
        this.preferences = AppController.getInstance().sharedPreferences;
        changeLanguage(self.getLanguage());
        allowUseCamera = CameraUtils.allowUseCamera(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.handler = new Handler();
        // GCM取得
        if (checkPlayServices()) {
            Intent intent = new Intent(this, GcmRegistrationService.class);
            startService(intent);
        }

        // サービスを動かす
        stopService(new Intent(this, GpsService.class));
        startService(new Intent(this, GpsService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.self = AppController.getInstance().getSelf(false);
        checkForceUpdate();

        if (BuildConfig.DEBUG) {
//            String location = "MyLocation is lat"+self.getLatitude()+"\n"+"lon"+self.getLongitude();
//            Toast.makeText(this, location, Toast.LENGTH_SHORT).show();
        }

        if (!isValidUserStatus()) {
            AppController.getInstance().deleteSelfAndBackToTop();
            finish();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (AppController.getInstance().getAlertDialog() != null && AppController.getInstance().getAlertDialog().isShowing()) {
                AppController.getInstance().getAlertDialog().dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private boolean isValidUserStatus() {
        try {
            Me self = AppController.getInstance().getSelf(false);
            if (self.getStatus() == Me.LEAVED) {
                return false;
            }
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        getActionBarToolBar();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, getActionBarToolBar(), 0, 0);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationListView = (ListView) findViewById(R.id.navigation_list);
            NavigationListAdapter adapter = new NavigationListAdapter(this, 0);
            navigationListView.setOnItemClickListener(this);
            navigationListView.addHeaderView(getNavigationHeaderView());
            navigationListView.setAdapter(adapter);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LOGD(TAG, "selected navigation item");
        Intent intent = null;
        int itemId = position - 1;
        if (itemId == NAV_MAIN) {
            intent = new Intent(this, MainActivity.class);
        } else if (itemId == NAV_COORDINATES) {
            intent = new Intent(this, CoordinatesCollectionActivity.class);
        } else if (itemId == NAV_CHECKIN) {
            intent = new Intent(this, CheckinActivity.class);
        } else if (itemId == NAV_ITEMS) {
            intent = new Intent(this, ItemsActivity.class);
        } else if (itemId == NAV_DEBUG) {
            intent = new Intent(this, DebugActivity.class);
            startActivity(intent);
            return;
        }

        delayStart(intent);

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }

        closeDrawer();
        LOGE(TAG, "intent is null");
    }


    class NavigationListAdapter extends ArrayAdapter<Object> {

        public NavigationListAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_item, parent, false);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.icon.setImageResource(NAV_ICON_LIST[position]);
            holder.title.setText(getString(NAV_TITLE_LIST[position]));

            return convertView;
        }

        @Override
        public int getCount() {
            if (!BuildConfig.DEBUG) {
                return NAV_ICON_LIST.length - 1;
            }
            return NAV_ICON_LIST.length;
        }

        class ViewHolder {
            TextView title;
            ImageView icon;
        }
    }


    protected Toolbar getActionBarToolBar() {
        LOGD(TAG, "called getActionBarToolBar");
        if (actionBarToolBar == null) {
            LOGD(TAG, "actionBarToolBar is null");
            actionBarToolBar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (actionBarToolBar != null) {
                LOGD(TAG, "actionBarToolBar was found!");
                actionBarToolBar.setNavigationContentDescription("");
                setSupportActionBar(actionBarToolBar);
            }
        }
        return actionBarToolBar;
    }


    protected View getNavigationHeaderView() {
        LOGD(TAG, "called getNavigationHeaderView");
        if (navigationHeaderView == null) {
            LOGD(TAG, "navigationHeaderView is null");
            navigationHeaderView = LayoutInflater.from(this).inflate(R.layout.nav_header_main, this.navigationListView, false);
            if (navigationHeaderView != null) {
                LOGD(TAG, "navigationHeaderView was found!");
                navigationHeaderView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LOGD(TAG, "clicked navigation header view");
                        Intent intent = new Intent(BaseActivity.this, MyActivity.class);
                        closeDrawer();
                        delayStart(intent);
                    }
                });
            }
        }
        {
            TextView nickname = (TextView) navigationHeaderView.findViewById(R.id.nickname);
            CircleImageView icon = (CircleImageView) navigationHeaderView.findViewById(R.id.icon);
            ImageView cover = (ImageView) navigationHeaderView.findViewById(R.id.cover);
            nickname.setText(self.getNickname());
            setProfileIcon(this, self.getProfile().getIconImgUrl(), icon);
            setProfileCover(this, self.getProfile().getCoverImgUrl(), cover);
        }
        return navigationHeaderView;
    }

    public void setProfileCover(Context context, final String coverImgUrl, final ImageView cover) {
        if (!coverImgUrl.isEmpty() && !coverImgUrl.equals("null")) {
            LOGD(TAG, "coverImgUrl:"+coverImgUrl);
            try {
                Picasso.with(context)
                        .load(coverImgUrl)
                        .fit()
                        .centerCrop()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .noFade()
                        .into(cover, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError() {
                                LOGE(TAG, "COVER DOWNLOAD FAILED:"+coverImgUrl);
                            }
                        });
            } catch (IllegalArgumentException e) {
                LOGE(TAG, e.getMessage());
            }

        } else {
            cover.setBackgroundResource(R.drawable.piece_pattern);
        }
    }

    public void setProfileIcon(Context context, final String iconImgUrl, ImageView icon) {
        if (!iconImgUrl.isEmpty() && !iconImgUrl.equals("null")) {
            LOGD(TAG, "iconImgUrl:"+iconImgUrl);
            try {

                Picasso.with(context)
                        .load(iconImgUrl)
                        .placeholder(R.drawable.placeholder_white)
                        .error(R.drawable.ic_mypage_red_square)
                        .fit()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .noFade()
                        .centerCrop()
                        .into(icon, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                LOGE(TAG, "ICON DOWNLOAD FAILED:" + iconImgUrl);
                            }
                        });
            } catch (IllegalArgumentException e) {
                LOGE(TAG, e.getMessage());
            }
        } else {
            icon.setImageResource(R.drawable.ic_mypage_red_square);
        }

    }

    /**
     * If intent isn't null, return true.
     *
     * @param intent
     * @return
     */
    private boolean delayStart(final Intent intent) {
        if (intent != null) {
            final Intent i = intent;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                        TaskStackBuilder builder = TaskStackBuilder.create(BaseActivity.this);
//                        builder.addNextIntentWithParentStack(i);
//                        builder.startActivities();
//                    } else {
                    getNavigationHeaderView();// 更新する
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
//                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    LOGD(TAG, "start activity on drawer");

                }
            }, NAVDRAWER_LAUNCH_DELAY);
            return true;
        }
        return false;
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
            LOGD(TAG, "closed drawer");
        }
    }

    public void backToTutorial() {
        AppController.getInstance().deleteSelfAndBackToTop();
        finish();
    }

    public void checkForceUpdate() {
        ApiParams params = new ApiParams(self, false, ApiRoute.FORCE_UPDATE);
        ApiRequest request = new ApiRequest(this);
        request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            JSONObject android = apiResponse.getBody().getJSONObject("android");
                            String storeVersion = android.getString("store_version");
                            LOGD(TAG, "storeVersion:" + storeVersion);
                            boolean needUpdate = android.getBoolean("need_update");
                            LOGD(TAG, "needUpdate:" + needUpdate);
                            if (!storeVersion.equals(BuildConfig.VERSION_NAME) && needUpdate) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
                                builder.setCancelable(false);
                                builder.setTitle(getString(R.string.text_account_update_force_title));
                                builder.setMessage(getString(R.string.text_account_update_force_message));
                                builder.setPositiveButton(getString(R.string.text_account_update_force_button_positive), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LOGD(TAG, "forceUpdate");
                                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        } catch (Exception e) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.EC_URL)));
                                        }
                                    }
                                });
                                if (isFinishing()) {
                                    return;
                                }
                                builder.create().show();
                                return;
                            }
                        } catch (Exception e) {
                            LOGE(TAG, e.getMessage());
                        }
                    }
                });
    }

    private ProgressBar getNetworkProgress() throws Exception {
        if (this.networkProgress == null) {
            this.networkProgress = (ProgressBar) findViewById(R.id.progress);
            if (this.networkProgress == null) {
                throw new RuntimeException("not found progress view");
            }
        }
//        this.networkProgress.setVisibility(View.GONE);
        return this.networkProgress;
    }

    protected boolean isShowingNetworkProgress() {
        try {
            return getNetworkProgress().getVisibility() == View.VISIBLE;
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            return false;
        }
    }

    protected void showNetworkProgress() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    getNetworkProgress().setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    LOGE(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    protected void hideProgress() {
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    getNetworkProgress().setVisibility(View.GONE);
                } catch (Exception e) {
                    LOGE(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    private View getNoDataView() throws Exception {
        if (this.noDataView == null) {
            this.noDataView = (View) findViewById(R.id.no_data_view);
        }
        this.noDataView.setVisibility(View.GONE);
        return this.noDataView;
    }

    protected void showNoDataView(final int stringId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView title = (TextView) getNoDataView().findViewById(R.id.no_data_title);
                    title.setText(getString(R.string.error_text_nodata, getString(stringId)));
                    TextView description = (TextView) getNoDataView().findViewById(R.id.no_data_description);
                    description.setVisibility(View.GONE);
                    getNoDataView().setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    LOGE(TAG, "nodataview can't shown" + e.getMessage());
                }
            }
        });
    }

    protected void showNoDataView(final int stringId, final int descStringId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView title = (TextView) getNoDataView().findViewById(R.id.no_data_title);
                    title.setText(getString(R.string.error_text_nodata, getString(stringId)));
                    TextView description = (TextView) getNoDataView().findViewById(R.id.no_data_description);
                    description.setText(descStringId);
                    description.setVisibility(View.VISIBLE);
                    getNoDataView().setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    LOGE(TAG, "nodataview can't shown" + e.getMessage());
                }
            }
        });
    }

    protected void hideNoDataView() {
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    getNoDataView().setVisibility(View.GONE);
                } catch (Exception e) {
                    LOGE(TAG, "nodataview can't hide" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    protected void requestCameraPermission() {
        if (CameraUtils.allowUseCamera(this)) {
            return;
        }
        LOGD(TAG, "shouldShowRequestCameraPermission:" + ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA));
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        } else {
            // おそらく次は表示しないにチェックしちゃった
            // TODO: アラート出す
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                allowUseCamera = true;
                canUseCamera();
                return;
            } else {
                AppController.getInstance().showAlert(BaseActivity.this, getString(R.string.error), getString(R.string.text_camera_authorize_description));
            }
        }
    }

    protected void canUseCamera() {
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                LOGE(TAG, "User need install GooglePlayService");
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
            } else {
                LOGE(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    protected void changeLanguage(String lang) {
        self.setLanguage(lang);
        try {
            self.getManager().save();
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(lang.toLowerCase());
        res.updateConfiguration(conf, dm);
    }
}


