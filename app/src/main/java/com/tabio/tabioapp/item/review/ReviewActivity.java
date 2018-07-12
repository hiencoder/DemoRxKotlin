package com.tabio.tabioapp.item.review;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.MyMenuBus;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.item.SwipeableItemsActivity;
import com.tabio.tabioapp.model.Review;
import com.tabio.tabioapp.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ReviewActivity extends BaseActivity {
    public static final String TAG = makeLogTag(ReviewActivity.class);

    @BindView(R.id.review_item_view)
    View reviewItemView;
    @BindView(R.id.item_img)
    ImageView itemImg;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.item_name)
    TextView itemName;
    @BindView(R.id.item_bottom_line)
    View itemBottomLine;

    @BindView(R.id.review_view)
    View reviewView;
    @BindView(R.id.reviewer_icon)
    ImageView reviewerIcon;
    @BindView(R.id.reviewer_name)
    TextView reviewerName;
    @BindView(R.id.review_date)
    TextView reviewDate;
    @BindView(R.id.review_body)
    TextView reviewBody;
    @BindView(R.id.bottom_line)
    View bottomLine;
    @BindView(R.id.review_arrow)
    ImageView reviewArrow;

    private ApiRequest request;

    private Review review;
    private boolean myReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        AppController.getInstance().sendGAScreen("レビュー詳細");
        AppController.getInstance().decideTrack("570f2fb599c3634a425af4fa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.text_review));
        ButterKnife.bind(this);

        this.request = new ApiRequest(this);

        this.myReview = getIntent().getBooleanExtra("myReview", false);
        if (getIntent().getSerializableExtra("review") != null) {
            this.review = (Review) getIntent().getSerializableExtra("review");
        }
//            showReview();

        final int product;
        if (this.review != null) {
            product = this.review.getItemProductId();
        } else {
            product = getIntent().getIntExtra("product", 0);
        }
        if (product > 0) {
            ApiParams params = new ApiParams(self, true, ApiRoute.GET_REVIEWS);
            params.put("search", myReview?1:2);
            params.put("product", product);
            params.put("count", 30);
            params.put("index", 1);
            AppController.getInstance().showSynchronousProgress(this);
            request.run(params)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.newThread())
                    .subscribe(new Observer<ApiResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            AppController.getInstance().showApiErrorAlert(ReviewActivity.this, ApiError.newUndefinedApiError(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onNext(ApiResponse apiResponse) {
                            if (apiResponse.hasError()) {
                                AppController.getInstance().showApiErrorAlert(ReviewActivity.this, apiResponse.getError(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                return;
                            }
                            try {
                                JSONObject result = apiResponse.getBody().getJSONObject("result");
                                JSONArray objects = result.getJSONArray("review");
                                if (objects.length() > 0) {
                                    if (review != null) {
                                        for (int i = 0; i < objects.length(); i++) {
                                            Review r = new Review(objects.getJSONObject(i));
                                            if (review.getReviewId() == r.getReviewId()) {
                                                review = r;
                                                break;
                                            }
                                        }
                                    } else {
                                        review = new Review(objects.getJSONObject(0));
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showReview();
                                            AppController.getInstance().dismissProgress();
                                        }
                                    });
                                } else {
                                    AppController.getInstance().showApiErrorAlert(ReviewActivity.this, ApiError.newUndefinedApiError(), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                LOGE(TAG, e.getMessage());
                                e.printStackTrace();
                                AppController.getInstance().showApiErrorAlert(ReviewActivity.this, ApiError.newUndefinedApiError(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                            }
                        }
                    });
        } else {
            AppController.getInstance().showApiErrorAlert(this, ApiError.newUndefinedApiError(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
    }

    private void showReview() {
        this.itemName.setText(review.getItemName());
        Picasso.with(this)
                .load(review.getItemImgUrl())
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .into(itemImg);

        this.status.setVisibility(this.myReview ? View.VISIBLE : View.GONE);
        if (this.myReview) {
            this.status.setText(getString(review.isConfirmed() ? R.string.text_review_confirmed : R.string.text_review_confirm_in));
        }

        this.reviewArrow.setVisibility(View.GONE);

        this.reviewerName.setText(review.getReviewerNickname());
        this.reviewDate.setText(review.getDateDisplay(self.getLanguage()));
        this.reviewBody.setText(review.getComment());
        this.reviewBody.setMaxLines(100);
        if (review.getReviewerIconImgUrl() == null) {
            reviewerIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mypage_green_square));
        } else {
            Picasso.with(this)
                    .load(review.getReviewerIconImgUrl())
                    .placeholder(R.drawable.placeholder_white)
                    .error(R.drawable.ic_mypage_red_square)
                    .into(reviewerIcon);
        }
    }

    @OnClick(R.id.review_item_view)
    void onReviewItemViewClicked() {
        if (this.review != null) {
            int product = this.review.getItemProductId();
            int[] ids = new int[]{product};
            Intent view = new Intent(this, SwipeableItemsActivity.class);
            view.putExtra("ids", ids);
            startActivity(view);
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
}
