package com.tabio.tabioapp.item.review;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.item.SwipeableItemsActivity;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.model.Review;
import com.tabio.tabioapp.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ReviewCreateActivity extends BaseActivity {
    public static final String TAG = makeLogTag(ReviewCreateActivity.class);

    @BindView(R.id.item_img)
    ImageView itemImg;
    @BindView(R.id.item_name)
    TextView itemName;
    @BindView(R.id.nickname_input)
    EditText nicknameInput;
    @BindView(R.id.review_input)
    EditText reviewInput;

    private ApiRequest request;

    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_create);
        AppController.getInstance().sendGAScreen("レビュー作成");
        AppController.getInstance().decideTrack("570f2fc099c3634a425af4fc");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.button_review_create));
        ButterKnife.bind(this);
        this.request = new ApiRequest(this);

        if (getIntent().getSerializableExtra("item") != null) {
            this.item = (Item) getIntent().getSerializableExtra("item");
            showItem();
        } else {
            getItem(getIntent().getIntExtra("productId", 0));
        }
    }

    private void showItem() {
        Picasso.with(this)
                .load(this.item.getAsset().getMainImgUrls().get(0))
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .into(itemImg);
        itemName.setText(this.item.getName());
        nicknameInput.setText(self.getProfile().getNickname());
//        if (item.getReviews().size() > 0) {
//            reviewInput.setText(item.getReviews().get(0).getComment());
//        }
    }

    private void getItem(final int productId) {
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_ITEM);
        params.put("product", productId);
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
                        AppController.getInstance().dismissProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            if (apiResponse.hasError()) {
                                LOGE(TAG, "err:" + apiResponse.getErrorMessage());
                                AppController.getInstance().showApiErrorAlert(ReviewCreateActivity.this, apiResponse.getError(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                return;
                            }
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            item = new Item(result);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getReviews(productId);
                                }
                            });
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            AppController.getInstance().showApiErrorAlert(ReviewCreateActivity.this, ApiError.newNetworkErrorApiError(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                        }
                    }
                });
    }

    private void getReviews(int productId) {
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_REVIEWS);
        params.put("search", 1);
        params.put("index", 1);
        params.put("count", 3);
        params.put("product", productId);
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
                                showItem();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        AppController.getInstance().showApiErrorAlert(ReviewCreateActivity.this, ApiError.newUndefinedApiError(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            if (apiResponse.hasError()) {
                                AppController.getInstance().showApiErrorAlert(ReviewCreateActivity.this, ApiError.newUndefinedApiError(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                return;
                            }
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            JSONArray jreviews = result.getJSONArray("review");
                            for (int i = 0; i < jreviews.length(); i++) {
                                Review review = new Review(jreviews.getJSONObject(i));
                                item.getReviews().add(review);
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            hideProgress();
                            AppController.getInstance().showApiErrorAlert(ReviewCreateActivity.this, ApiError.newUndefinedApiError(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                        }
                    }
                });
    }

    private void send() {
        if (this.item == null) {
            return;
        }
        if (nicknameInput.getText().toString().isEmpty()) {
            AppController.getInstance().showAlert(ReviewCreateActivity.this, getString(R.string.error), getString(R.string.text_review_create_fail_nickname));
            return;
        }
        if (reviewInput.getText().toString().isEmpty()) {
            AppController.getInstance().showAlert(ReviewCreateActivity.this, getString(R.string.error), getString(R.string.text_review_create_fail_review));
            return;
        }

        ApiParams params = new ApiParams(self, true, ApiRoute.CREATE_REVIEW);
        params.put("operation", this.item.didReview() ? 1 : 0);
        if (this.item.didReview()) {
            for (Review r : this.item.getReviews()) {
                if (r.getReviewerIconImgUrl().equals(self.getProfile().getIconImgUrl())) {
                    params.put("review_id", r.getReviewId());
                }
            }
        } else {
            params.put("product", this.item.getProductId());
        }
        params.put("nickname", nicknameInput.getText().toString());
        params.put("comment", reviewInput.getText().toString());

        showNetworkProgress();
        AppController.getInstance().showSynchronousProgress(this);
        this.request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        AppController.getInstance().showApiErrorAlert(ReviewCreateActivity.this, ApiError.newUndefinedApiError());
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            if (apiResponse.hasError()) {
                                AppController.getInstance().showApiErrorAlert(ReviewCreateActivity.this, apiResponse.getError());
                                return;
                            }
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            int beforePiece = result.getInt("before_piece");
                            int afterPiece = result.getInt("after_piece");
                            int point = result.getInt("point");
                            int gotPiece = afterPiece - beforePiece;
                            LOGD(TAG, "レビュー投稿成功");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    AppController.getInstance().showAlert(ReviewCreateActivity.this,
                                            getString(R.string.text_review_post_success), getString(R.string.text_review_post_success_description), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent data = new Intent();
                                                    item.setDidReview(true);
                                                    data.putExtra("item", item);
                                                    setResult(RESULT_OK, data);
                                                    finish();
                                                }
                                            });
                                }
                            });
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            AppController.getInstance().showApiErrorAlert(ReviewCreateActivity.this, ApiError.newUndefinedApiError());
                        }
                    }
                });
    }

    @OnTextChanged(R.id.nickname_input)
    void onNicknameInputTextChanged(CharSequence text) {
    }

    @OnTextChanged(R.id.review_input)
    void onReviewInputTextChanged(CharSequence text) {
    }

    @OnClick(R.id.review_item_view)
    void onReviewItemViewClicked() {
        int[] ids = new int[]{this.item.getProductId()};
        Intent view = new Intent(this, SwipeableItemsActivity.class);
        view.putExtra("ids", ids);
        startActivity(view);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem settingsMenuItem = menu.findItem(R.id.action_create_review);
        SpannableString s = new SpannableString(settingsMenuItem.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)), 0, s.length(), 0);
        settingsMenuItem.setTitle(s);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.review_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LOGD(TAG, "clicked home button");
                finish();
                return true;
            case R.id.action_create_review:
                send();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
