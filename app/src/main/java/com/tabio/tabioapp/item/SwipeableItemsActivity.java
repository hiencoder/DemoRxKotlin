package com.tabio.tabioapp.item;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.GcmObject;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.item.adapter.ItemsPagerAdapter;
import com.tabio.tabioapp.item.review.ReviewCreateActivity;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.preference.NotificationSettingsActivity;
import com.tabio.tabioapp.web.WebActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class SwipeableItemsActivity extends CartBaseActivity implements
        ItemFragment.OnItemFragmentCallbacks, ViewPager.OnPageChangeListener {
    private static final String TAG = makeLogTag(SwipeableItemsActivity.class);

    @BindView(R.id.view_pager) ViewPager viewPager;

    private int[] productIds;
    private String jan;
    private int myPosition;

    private ApiRequest request;
    private ItemsPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipeable_items);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);
        this.request = new ApiRequest(this);

        this.myPosition = getIntent().getIntExtra("position", 0);
        this.productIds = new int[0];
        if (getIntent().getIntArrayExtra("ids") != null) {
            this.productIds = getIntent().getIntArrayExtra("ids");
            this.adapter = new ItemsPagerAdapter(getSupportFragmentManager(), this.productIds, this);
        } else {
            if (getIntent().getStringExtra("jan") != null) {
                this.jan = getIntent().getStringExtra("jan");
                this.adapter = new ItemsPagerAdapter(getSupportFragmentManager(), this.jan, this);
            } else if (getIntent().getStringExtra("classId") != null) {
                // TODO
            }
        }

        this.viewPager.setAdapter(this.adapter);
        this.viewPager.setCurrentItem(this.myPosition, false);
        this.viewPager.addOnPageChangeListener(this);
        if (this.productIds != null && this.productIds.length > 0) {
            try {
                sendReadItem(this.productIds[this.myPosition]);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOGE(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onLoadProgress() {
        showNetworkProgress();
    }

    @Override
    public void onLoadFinished() {
        hideProgress();
    }

    @Override
    public void onItemUpdated(Item item) {
        if (this.productIds == null || this.productIds.length < 1) {
            return;
        }
        int currentPosition = this.viewPager.getCurrentItem();
        if (this.productIds[currentPosition] == item.getProductId()) {
            getSupportActionBar().setTitle(item.getName());
        }
    }

    List<Integer> changedProductIds = new ArrayList<>();
    @Override
    public void onItemFavoriteStatusChanged(final Item item) {
        Intent data = new Intent();
        if (changedProductIds.contains(item.getProductId())) {
            changedProductIds.remove((Integer)item.getProductId());
        } else {
            changedProductIds.add(item.getProductId());
        }
        data.putIntegerArrayListExtra("productIds", (ArrayList<Integer>) changedProductIds);
        setResult(RESULT_OK, data);
        if (item.isFavorite()) {
            View contentView = (View) findViewById(R.id.main_content);
            final View targetView = (View) LayoutInflater.from(this).inflate(R.layout.favorite_success_view, (ViewGroup) contentView, false);
            Button postReview = (Button) targetView.findViewById(R.id.post_review_button);
            postReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent view = new Intent(SwipeableItemsActivity.this, ReviewCreateActivity.class);
                    view.putExtra("item", item);
                    startActivity(view);
                }
            });
            animateViewBottomUp(targetView);
        }
    }

    @Override
    public void onAddCartButtonClicked(Item item) {
        addCart(item);
    }

    @Override
    public void onItemRestockRequestSuccessed(Item item) {
        if (item.getAsset().getCurrentLineup().isRestocked()) {
            View contentView = (View) findViewById(R.id.main_content);
            final View targetView = (View) LayoutInflater.from(this).inflate(R.layout.restock_success, (ViewGroup) contentView, false);
            Button pushSettingsButton = (Button) targetView.findViewById(R.id.push_setting_button);
            pushSettingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent view = new Intent(SwipeableItemsActivity.this, NotificationSettingsActivity.class);
                    startActivity(view);
                }
            });
            animateViewBottomUp(targetView);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (this.adapter.getProductIds() == null || this.adapter.getProductIds().length < 1) {
            return;
        }
        sendReadItem(this.adapter.getProductIds()[position]);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void sendReadItem(int productId) {
        LOGD(TAG, "read item:"+productId);
        ApiParams params = new ApiParams(self, true, ApiRoute.SEND_READ_ITEM);
        params.put("product", productId);

        this.request.run(params)
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
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items2, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LOGD(TAG, "clicked home button");
                finish();
                return true;
            case R.id.action_cart: {
                // TODO
                Intent intent = new Intent(this, WebActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
