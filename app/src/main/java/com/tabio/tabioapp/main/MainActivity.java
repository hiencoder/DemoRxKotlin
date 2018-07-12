package com.tabio.tabioapp.main;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.CouponBus;
import com.tabio.tabioapp.GcmObject;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.coupon.CouponListActivity;
import com.tabio.tabioapp.help.ScreenHelpActivity;
import com.tabio.tabioapp.me.MyBaseActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.piece.Piece;
import com.tabio.tabioapp.piece.PieceAnimView;
import com.tabio.tabioapp.ui.widget.LearningCurveTextView;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class MainActivity extends MyBaseActivity implements PieceAnimView.OnPieceAnimListener {
    public static final String TAG = makeLogTag(MainActivity.class);

    @BindView(R.id.piece_view)
    PieceAnimView pieceAnimView;
    @BindView(R.id.rank)
    LearningCurveTextView rank;
    @BindView(R.id.point)
    TextView point;
    @BindView(R.id.piece_expires_date)
    TextView pieceExpiresDate;
    @BindView(R.id.point_expires_date)
    TextView pointExpiresDate;
    @BindView(R.id.barcode_number)
    TextView barcodeNumber;
    @BindView(R.id.barcode)
    ImageView barcode;
    TextView couponCount;

    @BindView(R.id.rankup_view)
    View rankUpView;
    @BindView(R.id.previous_rank)
    LearningCurveTextView previousRankText;
    @BindView(R.id.new_rank)
    LearningCurveTextView newRankText;
    @BindView(R.id.given_point)
    TextView givenPointText;
    @BindView(R.id.total_point)
    TextView totalPointText;

    private ApiRequest apiRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppController.getInstance().sendGAScreen("会員証");
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);
        this.apiRequest = new ApiRequest(this);
        pieceAnimView.setPieceAnimListener(this);

        AppController.getInstance().decideTrack("570f295499c3634a425af4b4");

        if (!self.isLogin()) {
            self.setLogin(true);
            try {
                boolean result = self.getManager().save();
                LOGD(TAG, "ログインします:"+result);
            } catch (Exception e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        chkIncentive();
        getCouponCount();
//        updateProfile(null, null);
        refreshMembarshipStatus();

        if (getIntent().getSerializableExtra("gcmObject") != null) {
            GcmObject gcmObject = (GcmObject) getIntent().getSerializableExtra("gcmObject");
            Intent view = gcmObject.getIntent(this);
            startActivity(view);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CouponBus.get().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CouponBus.get().unregister(this);
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
        LOGD(TAG, "oldPiece:"+oldPiece);
        LOGD(TAG, "newPiece:"+newPiece);
        LOGD(TAG, "oldPoint:"+oldPoint);
        LOGD(TAG, "newPoint:"+newPoint);
        LOGD(TAG, "oldRank:"+oldRank);
        LOGD(TAG, "newRank:"+newRank);
        int rank = self.getRank();
        LOGD(TAG, "rank:" + rank);
        LOGD(TAG, "pieceAnimView:" + this.pieceAnimView.isReady());
        int currentPieceCount = Me.EXCHANGE_PIECE * (rank - 1);
        this.pieceAnimView.prepare(rank, currentPieceCount, newPiece);
        if (newRank > oldRank && this.pieceAnimView.getPieceNumLabel() != null) {
            int increasedCount = newPiece - oldPiece;
            this.pieceAnimView.getPieceNumLabel().setText(String.valueOf(increasedCount+(oldPiece%Me.EXCHANGE_PIECE)));
        }
        this.pieceAnimView.startAnim();

        self.setPiece(newPiece);
        self.setPoint(newPoint);
        self.setRank(newRank);
        try {
            self.getManager().save();
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        }
    }

    @Override
    protected void onRefreshedMyInfoFailed() {
        hideProgress();
    }

    private void refreshMembarshipStatus() {
        this.pieceExpiresDate.setText(" " + self.getPieceExpiresForDisplay(this, self.getLanguage()));
        this.pointExpiresDate.setText(" " + self.getPointExpiresForDisplay(this, self.getLanguage()));
        this.point.setText(" " + String.valueOf(self.getPoint()));
        int pieceColor = Piece.pieceColor(self.getRank());
        this.rank.setTextColor(pieceColor);
        this.rank.setText("Rank " + String.valueOf(self.getRank()));

        barcodeNumber.setText(self.getTabioId());
        Picasso.with(this)
                .load(self.getBarcodeUrl())
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .fit()
                .into(barcode);
    }


    private void getCouponCount() {
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_COUPON);
        params.put("index", 1);
        params.put("count", 1);
        this.apiRequest.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
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
                            final int total = apiResponse.getBody().getJSONObject("result").getInt("total");
                            LOGD(TAG, "coupon total:" + total);
                            if (total > 0) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            couponCount.setVisibility(View.VISIBLE);
                                            couponCount.setText(String.valueOf(total));
                                        } catch (NullPointerException e) {
                                            LOGE(TAG, e.getMessage());
                                            e.printStackTrace();
                                        }

                                    }
                                });

                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void chkIncentive() {
        String key = "didOpenIncentiveView";
        boolean didOpenIncentiveView = this.preferences.getBoolean(key, false);
        if (didOpenIncentiveView) {
            return;
        }
        didOpenIncentiveView = true;
        this.preferences.edit().putBoolean(key, didOpenIncentiveView).commit();
        String url = BuildConfig.BASE_URL + ApiRoute.WV_RECEIPT+"?tabio_id="+self.getTabioId();
        LOGD(TAG, "INCENTIVE URL:" + url);
        Uri uri = Uri.parse(url);
        Intent view = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(view);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void animViewInitialized(PieceAnimView view) {
        LOGD(TAG, "animViewInitialized");
        this.pieceAnimView.prepare(self.getRank(), 0, 0);
        refreshMembarshipStatus();
    }

    boolean didAnimate = false;

    @Override
    public void readyToAnim(PieceAnimView view) {
        LOGD(TAG, "readyToAnim");
//        view.startAnim();
        if (!didAnimate) {
            didAnimate = true;
            getUser(false);
        }
    }

    @Override
    public void finishedAnim(PieceAnimView view) {
    }

    @Override
    public void finishCoverUp(PieceAnimView view, int oldRank, int newRank, int point) {
        AppController.getInstance().decideTrack("570f29f699c3634a425af4bc");
        previousRankText.setText(" Rank "+String.valueOf(oldRank)+" ");
        newRankText.setText(" Rank "+String.valueOf(newRank)+" ");
        givenPointText.setText(getString(R.string.text_rankup_get, String.valueOf(point)));
        totalPointText.setText(getString(R.string.text_rankup_total, String.valueOf(self.getPoint())));
        rankUpView.setBackgroundColor(Piece.pieceColor(oldRank));
        pieceAnimView.expirationDateLabel.setVisibility(View.GONE);
        rankUpView.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishedCloseAnim(PieceAnimView view) {
        refreshMembarshipStatus();
        rankUpView.setVisibility(View.GONE);
        pieceAnimView.expirationDateLabel.setVisibility(View.VISIBLE);

        this.pieceAnimView.prepare(self.getRank(), Me.EXCHANGE_PIECE * (self.getRank() - 1), self.getPiece());
        this.pieceAnimView.startAnim();
    }

    @OnClick(R.id.barcode)
    void onBarcodeClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            WindowManager.LayoutParams windowParams = getWindow().getAttributes();
            windowParams.screenBrightness = 1.0f;
            getWindow().setAttributes(windowParams);
        }

    }

    @Subscribe
    public void onReceiveCouponFromGcm(GcmObject couponGcmObject) {
        getCouponCount();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_coupon);
        MenuItemCompat.setActionView(item, R.layout.menu_coupon);
        this.couponCount = (TextView) item.getActionView().findViewById(R.id.coupon_count);
        item.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent view = new Intent(MainActivity.this, CouponListActivity.class);
                startActivity(view);
                AppController.getInstance().sendGAEvent("Coupon","Show", "", 0);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            Intent view = new Intent(this, ScreenHelpActivity.class);
            AppController.getInstance().sendGAScreen("会員証ヘルプ");
            AppController.getInstance().sendGAEvent("Membership","Help","",0);
            AppController.getInstance().decideTrack("570f29e899c3634a425af4ba");
            view.putExtra("helpId", ScreenHelpActivity.HELP_MAIN);
            startActivity(view);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
        if (BuildConfig.DEBUG) {
//            sendTestGcm();
        }
    }


    private void sendTestGcm() {
        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_COUPONS, "", "test");
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_ACCOUNT, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_CART, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_CHECKIN, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_COORDINATES, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_EMAIL_LOGIN, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_EMAIL_MIGRATE, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_FAQ, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_ITEM, "5189", null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_ITEMS, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_LANGUAGE, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_MEMBERSHIP, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_MYPAGE, "0", null);/*0~9*/
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_NOTIFICATION, "0", null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_ONLINE_MIGRATE, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_POST_REVIEW, "5189", null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_PREFERENCES, "4", null);/*1~4*/
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_STORE, "204", null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_STORES, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_TUTORIAL, null, null);
//        GcmObject gcmObject = new GcmObject(GcmObject.SCREEN_WEB, "http://www.tabio.com/", null);

        Intent view = new Intent(this, MainActivity.class);
        view.putExtra("gcmObject", gcmObject);
        view.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        String title = "test";
        String message = "test";
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, view,
                PendingIntent.FLAG_CANCEL_CURRENT);
//        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(largeIcon)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
