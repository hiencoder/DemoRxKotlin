package com.tabio.tabioapp.item.review;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.model.Review;
import com.tabio.tabioapp.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ReviewsActivity extends BaseActivity {
    public static final String TAG = makeLogTag(ReviewsActivity.class);

    private Item item;
    private ReviewsFragment reviewsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getInstance().sendGAScreen("レビュー一覧");
        setContentView(R.layout.activity_reviews);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        this.item = (Item) getIntent().getSerializableExtra("item");
        getSupportActionBar().setTitle(this.item.getName());

        ApiParams params = new ApiParams(self, true, ApiRoute.GET_REVIEWS);
        params.put("search", 0);
        params.put("product", this.item.getProductId());
        this.reviewsFragment = ReviewsFragment.newInstance(false, true, params);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.review_list, this.reviewsFragment, ReviewsFragment.TAG)
                .commit();
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
