package com.tabio.tabioapp.order;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.item.SwipeableItemsActivity;
import com.tabio.tabioapp.item.review.ReviewActivity;
import com.tabio.tabioapp.item.review.ReviewCreateActivity;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.model.Order;
import com.tabio.tabioapp.model.OrderDetail;
import com.tabio.tabioapp.model.Review;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.web.WebActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 1/11/16.
 */
public class OrderHistoryActivity extends BaseActivity implements OrderHistoryDataAdapter.OnOrderHistoryDataAdapterCallbacks {
    public static final String TAG = makeLogTag(Order.class);

    private OrderHistoryDataAdapter adapter;

    private ApiRequest request;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Order order;

    private static final int CREATE_REVIEW_REQUEST = 1001;

    public OrderHistoryActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        AppController.getInstance().sendGAScreen("購入履歴詳細");
        AppController.getInstance().decideTrack("570f304399c3634a425af507");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.text_orderHistory));
        ButterKnife.bind(this);
        this.request = new ApiRequest(this);

        order = (Order) getIntent().getSerializableExtra("order");
        if (order == null) {
            AppController.getInstance().showApiErrorAlert(this, ApiError.newUndefinedApiError(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            return;
        }

        this.recyclerView.setHasFixedSize(false);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new OrderHistoryDataAdapter(this, this);
        this.recyclerView.setAdapter(this.adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (order != null) {
            getOrder(order.getOrderId());
        }
    }

    private void getOrder(int orderId) {
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_ORDER);
        params.put("id", orderId);
        showNetworkProgress();
        this.request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            Order order = new Order(result);
                            adapter.set(order);
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onItemClicked(OrderDetail orderDetail) {
        if (orderDetail.getProductId() == 0) {
            return;
        }
        Intent view = new Intent(this, SwipeableItemsActivity.class);
        int[] productIds = new int[]{orderDetail.getProductId()};
        view.putExtra("ids", productIds);
        startActivity(view);
    }

    @Override
    public void onItemFavoriteButtonClicked(final OrderDetail orderDetail) {
        if (orderDetail.getProductId() == 0) {
            return;
        }
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_ITEM);
        HashMap<String/*Key*/, List/*Arrayで送りたいやつ*/> dict = new HashMap<>();
        List<Integer> storeIds = Arrays.asList(orderDetail.getProductId());
        dict.put("product", storeIds);
        params.put(orderDetail.isFavorite() ? "delete" : "insert", dict);

        showNetworkProgress();
        this.request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, e.getMessage());
                        e.printStackTrace();
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            int beforePiece = result.getInt("before_piece");
                            int afterPiece = result.getInt("after_piece");
                            int point = result.getInt("point");
                            int gotPiece = afterPiece - beforePiece;
                            if (!apiResponse.hasError()) {
                                orderDetail.setFavorite(!orderDetail.isFavorite());
                                orderDetail.setFavoriteCount(orderDetail.getFavoriteCount() + (orderDetail.isFavorite() ? +1 : -1));
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onItemPostReviewButtonClicked(OrderDetail orderDetail) {
        if (orderDetail.getProductId() == 0) {
            return;
        }
        if (orderDetail.isPostReview()) {
            Intent view = new Intent(this, ReviewActivity.class);
            view.putExtra("product", orderDetail.getProductId());
            view.putExtra("myReview", true);
            startActivity(view);
        } else {
            Intent view = new Intent(this, ReviewCreateActivity.class);
            view.putExtra("productId", orderDetail.getProductId());
            startActivityForResult(view, CREATE_REVIEW_REQUEST);
        }
    }

    @Override
    public void onActionButtonClicked(Order order) {
        if (order.getStatus()==1 || order.getStatus()==4) {
            // 返品・交換
            Intent view = new Intent(this, WebActivity.class);
            view.putExtra("url", BuildConfig.BASE_URL+ApiRoute.WV_ITEM_RETURN+"?order_id="+order.getOrderId()+"&tabio_id="+self.getTabioId());
            startActivity(view);
        } else if (order.getStatus()==2) {
            // キャンセル
            Intent view = new Intent(this, WebActivity.class);
            view.putExtra("url", BuildConfig.BASE_URL+ApiRoute.WV_ITEM_CANCEL+"?order_id="+order.getOrderId()+"&tabio_id="+self.getTabioId());
            startActivity(view);
        }
    }

    @Override
    public void onDeliveryCheckButtonClicked(Order order) {
        Intent view = new Intent(this, WebActivity.class);
        view.putExtra("url", "http://toi.kuronekoyamato.co.jp/cgi-bin/tneko?init");
        startActivity(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_REVIEW_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra("item")) {
                if (data.getSerializableExtra("item") != null) {
                    Item item = (Item)data.getSerializableExtra("item");
                    for (OrderDetail detail : this.adapter.getObject().getDetails()) {
                        if (detail.getItemId().equals(item.getItemId())) {
                            detail.setPostReview(true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LOGD(TAG, "clicked home button");
            setResult(RESULT_OK);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }
}
