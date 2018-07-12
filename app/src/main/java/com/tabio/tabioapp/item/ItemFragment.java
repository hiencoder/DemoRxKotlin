package com.tabio.tabioapp.item;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.item.adapter.ImageGalleryAdapter;
import com.tabio.tabioapp.item.review.ReviewActivity;
import com.tabio.tabioapp.item.review.ReviewCreateActivity;
import com.tabio.tabioapp.item.review.ReviewsActivity;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.model.Lineup;
import com.tabio.tabioapp.model.Magazine;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Review;
import com.tabio.tabioapp.model.Stock;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.store.StoreActivity;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.ui.widget.LearningCurveTextView;
import com.tabio.tabioapp.util.GpsUtils;
import com.tabio.tabioapp.util.ViewUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ItemFragment extends BaseFragment {
    public static final String TAG = makeLogTag(ItemFragment.class);

    @BindView(R.id.gallery)
    ViewPager galleryView;
    @BindView(R.id.img_count_numerator)
    TextView imgCountNumerator;
    @BindView(R.id.img_count_denominator)
    TextView imgCountDenominator;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.item_id)
    TextView itemId;
    @BindView(R.id.item_price1)
    TextView price1;
    @BindView(R.id.strike_through)
    View strikeThrough;
    @BindView(R.id.item_price2)
    TextView price2;
    @BindView(R.id.favorite_button)
    View favoriteButton;
    @BindView(R.id.favorite_img)
    ImageView favoriteImg;
    @BindView(R.id.favorite_count)
    TextView favoriteCount;
    @BindView(R.id.share_button)
    ImageButton shareButton;
    @BindView(R.id.size)
    TextView size;
    @BindView(R.id.top_purchase_button)
    Button topPurchaseButton;
    @BindView(R.id.lineup_view)
    LinearLayout lineupsView;
    @BindView(R.id.has_stock_stores_title)
    TextView hasStockStoresTitle;
    @BindView(R.id.instock_stores)
    LinearLayout instockStoresView;
    @BindView(R.id.instock_stores_caution)
    LinearLayout instockStoresCautionView;
    @BindView(R.id.info_view)
    LinearLayout infoView;
    @BindView(R.id.info)
    TextView info;
    @BindView(R.id.material_view)
    LinearLayout materialView;
    @BindView(R.id.material)
    TextView material;
    @BindView(R.id.post_histories_view)
    LinearLayout postHistoriesView;
    @BindView(R.id.post_histories)
    TextView postHistories;
    @BindView(R.id.purchase_button)
    Button bottomPurchaseButton;
    @BindView(R.id.review_title)
    TextView reviewTitle;
    @BindView(R.id.reviews)
    LinearLayout reviewsView;
    @BindView(R.id.review_more_button)
    View reviewMoreButton;
    @BindView(R.id.create_review_button)
    View createReviewButton;


    private CircleImageView[] lineupImgs;

    private Item item;

    private ApiRequest request;
    private Handler handler;

    private ImageGalleryAdapter galleryAdapter;

    private OnItemFragmentCallbacks callbacks;

    private LocationManager locationManager;

    private Unbinder unbinder;

    public ItemFragment() {
        // Required empty public constructor
    }

    public interface OnItemFragmentCallbacks {
        void onLoadProgress();

        void onLoadFinished();

        void onItemUpdated(Item item);

        void onItemFavoriteStatusChanged(Item item);

        void onAddCartButtonClicked(Item item);

        void onItemRestockRequestSuccessed(Item item);
    }

    public static ItemFragment newInstance(int productId, OnItemFragmentCallbacks callbacks) {
        ItemFragment fragment = new ItemFragment();
        fragment.callbacks = callbacks;
        Bundle args = new Bundle();
        args.putInt("id", productId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ItemFragment newInstance(String jan, OnItemFragmentCallbacks callbacks) {
        ItemFragment fragment = new ItemFragment();
        fragment.callbacks = callbacks;
        Bundle args = new Bundle();
        args.putString("jan", jan);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getInstance().sendGAScreen("アイテム詳細");
        AppController.getInstance().decideTrack("570f2f9a99c3634a425af4f6");

        this.locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        this.handler = new Handler();
        this.item = new Item();
        if (getArguments().getInt("id") != 0) {
            this.item.setProductId(getArguments().getInt("id"));
            // TODO:在庫確認のため
//            this.item.setProductId(5189);
            // TODO:画像が揃ってるやつ
//            this.item.setProduct(19003);
        } else {
            if (getArguments().getString("jan") != null) {
                this.item.setJan(getArguments().getString("jan"));
            }
        }
        this.request = new ApiRequest(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getItem();
    }

    private void reloadItemInfo(boolean loadedInstockStores) {
        try {
            if (callbacks != null) {
                callbacks.onItemUpdated(this.item);
            }
            this.createReviewButton.setVisibility(this.item.didReview() ? View.GONE : View.VISIBLE);
            this.hasStockStoresTitle.setVisibility(View.GONE);
            this.instockStoresView.setVisibility(View.GONE);
            this.name.setText(this.item.getName());
            if (this.item.getAsset().getCurrentLineup() != null) {
                this.itemId.setText(
                        String.valueOf(this.item.getItemId()) + "-" +
                                this.item.getAsset().getCurrentLineup().getCode() + ":" +
                                this.item.getAsset().getCurrentLineup().getName());
            }
            this.favoriteCount.setText(String.valueOf(this.item.getFavoriteCount()));
            this.favoriteCount.setTextColor(ContextCompat.getColor(getActivity(), this.item.isFavorite()?R.color.redDark600:R.color.grayLight200));
            this.favoriteImg.setImageResource(this.item.isFavorite() ? R.drawable.ic_btn_fav : R.drawable.ic_btn_fav_gray);
            this.size.setText(this.item.getSize());

            Lineup currentLineup = this.item.getAsset().getCurrentLineup();
            if (currentLineup != null) {
                if (this.item.getPrice()/*定価*/ > currentLineup.getPrice()/*通販用販売価格*/) {
                    // セール
                    this.price1.setText(this.item.getPriceWithYen());
                    this.price1.setVisibility(View.VISIBLE);
                    this.strikeThrough.setVisibility(View.VISIBLE);
                    this.price1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (strikeThrough != null) {
                                ViewGroup.LayoutParams params = strikeThrough.getLayoutParams();
                                params.width = price1.getMeasuredWidth();
//                                LOGD(TAG, "width:" + params.width);
                                strikeThrough.setLayoutParams(params);
                                strikeThrough.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    this.price2.setText(currentLineup.getPriceWithYen());
//            this.price2.setTextColor(ContextCompat.getColor(getActivity(), R.color.redDark600));
                } else {
                    // 通常価格
                    this.price1.setVisibility(View.GONE);
                    this.strikeThrough.setVisibility(View.GONE);
                    this.price2.setText(this.item.getPriceWithYen());
//            this.price2.setTextColor(ContextCompat.getColor(getActivity(), R.color.greenDark200));
                }
            }

            {
                String purchaseText = "";
                if (currentLineup != null) {
                    switch (currentLineup.getStatus()) {
                        case Lineup.SALE:
                            purchaseText = getString(R.string.button_purchaseNow);
                            this.topPurchaseButton.setBackgroundResource(R.drawable.action_button);
                            this.topPurchaseButton.setEnabled(true);
                            this.bottomPurchaseButton.setEnabled(true);
                            this.bottomPurchaseButton.setBackgroundResource(R.drawable.action_button);
                            break;
                        case Lineup.CAN_RESTOCK_REQUEST:
                            if (currentLineup.isRestocked()) {
                                purchaseText = getString(R.string.button_restockRequested);
                                this.topPurchaseButton.setEnabled(false);
                                this.bottomPurchaseButton.setEnabled(false);
                            } else {
                                purchaseText = getString(R.string.button_restockRequest);
                                this.topPurchaseButton.setEnabled(true);
                                this.bottomPurchaseButton.setEnabled(true);
                            }
                            this.topPurchaseButton.setBackgroundResource(R.drawable.light_gray_button);

                            this.bottomPurchaseButton.setBackgroundResource(R.drawable.light_gray_button);
                            break;
                        case Lineup.SOLD_OUT:
                            purchaseText = getString(R.string.button_soldout);
                            this.topPurchaseButton.setBackgroundResource(R.drawable.light_gray_button);
                            this.topPurchaseButton.setEnabled(false);
                            this.bottomPurchaseButton.setEnabled(false);
                            this.bottomPurchaseButton.setBackgroundResource(R.drawable.light_gray_button);
                            break;
                    }
                    this.topPurchaseButton.setText(purchaseText);
                    this.bottomPurchaseButton.setText(purchaseText);
                } else {
                    this.topPurchaseButton.setBackgroundResource(R.drawable.light_gray_button);
                    this.topPurchaseButton.setEnabled(false);
                    this.bottomPurchaseButton.setEnabled(false);
                    this.bottomPurchaseButton.setBackgroundResource(R.drawable.light_gray_button);
                    purchaseText = getString(R.string.button_soldout);
                    this.topPurchaseButton.setText(purchaseText);
                    this.bottomPurchaseButton.setText(purchaseText);
                }
            }
            // アイテム説明
            this.info.setText(this.item.getDescription());
            // 素材
            this.material.setText(this.item.getMaterial());
            // 雑誌掲載

            if (this.item.getPostHistories().size() > 0) {
                String postHistories = "";
                for (int i = 0; i < this.item.getPostHistories().size(); i++) {
                    if (i != 0) {
                        postHistories += "\n";
                    }
                    Magazine magazine = this.item.getPostHistories().get(i);
                    postHistories += magazine.getName();
                    postHistories += magazine.getIssue() + getString(R.string.text_item_magazine_published);
                    postHistories += " " + getString(R.string.text_item_magazine_published);
                }
                this.postHistories.setText(postHistories);
            } else {
                this.postHistoriesView.setVisibility(View.GONE);
            }

            // 在庫のある店舗
            showStockStores(loadedInstockStores);
            // レビュー
            showReviews();

        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        }
    }

    private void showStockStores(boolean loadedInstockStores) {
        this.instockStoresView.removeAllViews();
        this.instockStoresView.setVisibility(View.VISIBLE);
        this.hasStockStoresTitle.setVisibility(View.VISIBLE);
        this.instockStoresCautionView.setVisibility(View.VISIBLE);
        this.hasStockStoresTitle.setText(getString(R.string.text_item_menu_title_instockStores));
        if (this.item.getAsset().getCurrentLineup().hasStock() && GpsUtils.allowAccessLocation(getActivity(), this.locationManager)) {
            for (int i = 0; i < this.item.getAsset().getCurrentLineup().getStockHasStores().size(); i++) {
                final Store stockHasStore = this.item.getAsset().getCurrentLineup().getStockHasStores().get(i);
                View stockStoreView = LayoutInflater.from(getActivity()).inflate(R.layout.instock_store_item, instockStoresView, false);
                stockStoreView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (stockHasStore.getStoreId() != null) {
                            Intent view = new Intent(getActivity(), StoreActivity.class);
                            view.putExtra("store_id", stockHasStore.getStoreId());
                            startActivity(view);
                        }
                    }
                });
                TextView brand = (TextView) stockStoreView.findViewById(R.id.brand);
                TextView status = (TextView) stockStoreView.findViewById(R.id.status);
                TextView name = (TextView) stockStoreView.findViewById(R.id.store_name);
                LearningCurveTextView distance = (LearningCurveTextView) stockStoreView.findViewById(R.id.distance);
                LearningCurveTextView distanceUnit = (LearningCurveTextView) stockStoreView.findViewById(R.id.distance_unit);
                brand.setText(stockHasStore.getBrand());
                String s = "";
                switch (stockHasStore.getStock().getStatus()) {
                    case Stock.FEW:
                        s = getString(R.string.text_item_status_few);
                        break;
                    case Stock.HAS_STOCK:
                        s = getString(R.string.text_item_status_hasStock);
                        break;
                    case Stock.OUT_OF_STOCK:
                        s = getString(R.string.text_item_status_outOfStock);
                        break;
                }
                status.setText(s);
                name.setText(stockHasStore.getName());
                distance.setText(stockHasStore.getDistanceForDisplay());
                distanceUnit.setText(stockHasStore.getDistanceUnit());
                this.instockStoresView.addView(stockStoreView);
            }
        } else {
            TextView nodata = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.no_data_item, this.instockStoresView, false);
            if (!loadedInstockStores) {
                nodata.setText("");
            } else {
                nodata.setText(getString(R.string.text_item_stock_noStore));
            }
            nodata.setVisibility(View.VISIBLE);
            this.instockStoresCautionView.setVisibility(View.GONE);
            this.instockStoresView.addView(nodata);
            FrameLayout line = new FrameLayout(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewUtils.getPixelFromDp(getActivity(), 2));
            line.setLayoutParams(params);
            line.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.whiteSmoke));

            this.instockStoresView.addView(line);
        }
    }

    private void showReviews() {
        this.reviewTitle.setText(getString(R.string.text_review));
        this.reviewsView.removeAllViews();
        if (!this.item.hasReviews()) {
            TextView nodata = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.no_data_item, this.reviewsView, false);
            nodata.setVisibility(View.VISIBLE);
            nodata.setText(getString(R.string.error_text_nodata, getString(R.string.text_review)));
            this.reviewsView.addView(nodata);
            reviewMoreButton.setVisibility(View.GONE);
            return;
        }
        for (int i = 0; i < this.item.getReviews().size(); i++) {
            final Review review = this.item.getReviews().get(i);
            View reviewView = LayoutInflater.from(getActivity()).inflate(R.layout.review_view, null);
            reviewView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LOGD(TAG, "review:"+review.getReviewId());
                    Intent view = new Intent(getActivity(), ReviewActivity.class);
                    view.putExtra("review", review);
                    view.putExtra("product", item.getProductId());
                    startActivity(view);
                }
            });
            CircleImageView icon = (CircleImageView) reviewView.findViewById(R.id.reviewer_icon);
            TextView name = (TextView) reviewView.findViewById(R.id.reviewer_name);
            TextView date = (TextView) reviewView.findViewById(R.id.review_date);
            TextView body = (TextView) reviewView.findViewById(R.id.review_body);
            View bottomLine = reviewView.findViewById(R.id.bottom_line);

            if (review.getReviewerIconImgUrl() == null) {
                icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_mypage_green_square));
            } else {
                Picasso.with(getActivity())
                        .load(review.getReviewerIconImgUrl())
                        .placeholder(R.drawable.placeholder_white)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .error(R.drawable.ic_mypage_red_square)
                        .into(icon);
            }
            name.setText(review.getReviewerNickname());
            date.setText(review.getDateDisplay(self.getLanguage()));
            body.setText(review.getComment());
            bottomLine.setVisibility(i == this.item.getReviewCount() - 1 ? View.GONE : View.VISIBLE);
            this.reviewsView.addView(reviewView);
        }
        reviewMoreButton.setVisibility(this.item.getReviewCount() > 3 ? View.VISIBLE : View.GONE);
    }

    private void getReviews() {
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_REVIEWS);
        params.put("search", 2);
        params.put("index", 1);
        params.put("count", 3);
        params.put("product", this.item.getProductId());
        if (callbacks != null) {
            callbacks.onLoadProgress();
        }
        this.request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        if (callbacks != null) {
                            callbacks.onLoadFinished();
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (item.getReviews().size() > 0) {
                                        reloadItemInfo(false);
                                    }
                                } catch (Exception e) {
                                    LOGE(TAG, e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (callbacks != null) {
                            callbacks.onLoadFinished();
                        }
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            JSONArray jreviews = result.getJSONArray("review");
                            for (int i = 0; i < jreviews.length(); i++) {
                                Review review = new Review(jreviews.getJSONObject(i));
                                item.getReviews().add(review);
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void getHasStockStores() {
        ApiParams params = new ApiParams(self, true, true, ApiRoute.GET_STORES);
        params.put("search", 0);
        params.put("product_class", this.item.getAsset().getCurrentLineup().getClassId());
        params.put("index", 1);
        params.put("count", 3);

        // 恵比寿
//        params.put("latitude", 35.646386);
//        params.put("longitude",139.709896);
        if (callbacks != null) {
            callbacks.onLoadProgress();
        }
        this.request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        if (callbacks != null) {
                            callbacks.onLoadFinished();
                        }
                        LOGD(TAG, "hasStock:" + item.getAsset().getCurrentLineup().hasStock());
                        if (item.getAsset().getCurrentLineup().hasStock()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    reloadItemInfo(true);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, e.getMessage());
                        if (callbacks != null) {
                            callbacks.onLoadFinished();
                        }
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            JSONArray jstores = result.getJSONArray("store");
                            item.getAsset().getCurrentLineup().setStockHasStores(new ArrayList<Store>());
                            for (int i = 0; i < jstores.length(); i++) {
                                Store store = new Store(jstores.getJSONObject(i));
                                if (store.hasStock(item.getAsset().getCurrentLineup().getClassId())) {
                                    item.getAsset().getCurrentLineup().getStockHasStores().add(store);
                                }
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });

    }

    private void getItem() {
        LOGD(TAG, "getItem");
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_ITEM);
        if (this.item.getProductId() != 0) {
            params.put("product", this.item.getProductId());
        } else {
            if (this.item.getJan() != null && this.item.getJan().length() > 0) {
                params.put("jan", this.item.getJan());
            }
        }

        if (callbacks != null) {
            callbacks.onLoadProgress();
        }
        this.request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                reloadItemInfo(false);
                                setupGallery();
                                setupLineups();
                                getReviews();
                            }
                        });
                        if (callbacks != null) {
                            callbacks.onLoadFinished();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        showCloseError(null);
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            if (apiResponse.hasError()) {
                                showCloseError(apiResponse);
                                return;
                            }
                            LOGD(TAG, apiResponse.getBody().toString(4));
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            if (result == null || result.equals("null")) {
                                showCloseError(apiResponse);
                                return;
                            }
                            item = new Item(result);
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                            showCloseError(apiResponse);
                        }
                    }
                });
    }

    private void setupLineups() {
        if (this.item.getAsset() == null) {
            return;
        }
        int lineupsSize = item.getAsset().getLineups().size();
        this.lineupImgs = new CircleImageView[lineupsSize];
        try {
            for (int i = 0; i < lineupsSize; i++) {
                View lineupView = LayoutInflater.from(getActivity()).inflate(R.layout.lineup_item, null);
                CircleImageView img = (CircleImageView) lineupView.findViewById(R.id.img);
                Picasso.with(getActivity())
                        .load(item.getAsset().getLineups().get(i).getChipImgUrl())
                        .placeholder(R.drawable.placeholder_white)
                        .error(R.drawable.placeholder_white)
                        .into(img);
                this.lineupImgs[i] = img;
                lineupView.setTag(i + 10);
                lineupView.setOnClickListener(onLineupClickListener);
                this.lineupsView.addView(lineupView);
            }
            selectLineup();
        } catch (NullPointerException e) {
            LOGE(TAG, e.getMessage());
        }
    }

    private View.OnClickListener onLineupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = ((int) v.getTag()) - 10;
            selectLineup(position, false);
        }
    };

    private void selectLineup() {
        selectLineup(0, true);
    }

    private void selectLineup(int index, boolean main) {
        int lineupsSize = item.getAsset().getLineups().size();
        for (int i = 0; i < lineupsSize; i++) {
            Lineup lineup = item.getAsset().getLineups().get(i);
            lineup.setSelected(i == index);
            this.lineupImgs[i].setBorderColorResource(lineup.isSelected() ? R.color.redDark600 : R.color.transparent);
            this.lineupImgs[i].setBorderOverlay(true);
        }
        try {
            String selectedImgUrl = this.item.getAsset().getCurrentLineup().getImgUrl();
            for (int i = 0; i < this.galleryAdapter.getCount(); i++) {
                if (galleryAdapter.getImgUrls().get(i).equals(selectedImgUrl)) {
                    galleryView.setCurrentItem(main?0:i, false);
                    break;
                }
            }
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        }
        getHasStockStores();
        reloadItemInfo(false);
    }

    private void setGalleryCount(int index) {
        this.imgCountNumerator.setText(String.valueOf(index + 1));
        this.imgCountDenominator.setText(String.valueOf(this.galleryAdapter.getCount()));
    }

    private void setupGallery() {
        try {
            List<String> imgUrls = this.item.getAsset().getMainImgUrls();
            for (Lineup lineup : this.item.getAsset().getLineups()) {
                imgUrls.add(lineup.getImgUrl());
            }
            galleryAdapter = new ImageGalleryAdapter(getActivity(), imgUrls);
            galleryView.setAdapter(galleryAdapter);
            galleryView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    setGalleryCount(position);
                    reloadItemInfo(false);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            setGalleryCount(0);
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void addCart() {
        if (callbacks != null) {
            callbacks.onAddCartButtonClicked(this.item);
        }
    }

    private void restockRequest() {
        ApiParams params = new ApiParams(self, true, ApiRoute.RESTOCK_REQUEST);
        params.put("product_class", this.item.getAsset().getCurrentLineup().getClassId());

        if (callbacks != null) callbacks.onLoadProgress();
        this.request.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (callbacks != null) callbacks.onLoadFinished();
                        AppController.getInstance().showApiErrorAlert(getActivity(), ApiError.newUndefinedApiError());
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (callbacks != null) callbacks.onLoadFinished();
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showApiErrorAlert(getActivity(), apiResponse.getError());
                            return;
                        }
                        item.getAsset().getCurrentLineup().setRestocked(true);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callbacks != null) {
                                    callbacks.onItemRestockRequestSuccessed(item);
                                }
                                reloadItemInfo(false);
                            }
                        });
                    }
                });
    }

    @OnClick({R.id.top_purchase_button, R.id.purchase_button})
    void onPurchasButtonClicked(View v) {
        if (!itemExists()) {
            return;
        }
        if (this.item.getAsset().getLineups().size() < 1) {
            return;
        }
        int status = this.item.getAsset().getCurrentLineup().getStatus();
        switch (status) {
            case Lineup.SALE:
                AppController.getInstance().sendGAEvent("Item", "AddCart", v.getId() == R.id.top_purchase_button ? "small" : "large", (long) this.item.getAsset().getCurrentLineup().getClassId());
                addCart();
                break;
            case Lineup.CAN_RESTOCK_REQUEST:
                AppController.getInstance().sendGAEvent("Item", "StockRequest", v.getId() == R.id.top_purchase_button ? "small" : "large", (long) this.item.getAsset().getCurrentLineup().getClassId());
                restockRequest();
                break;
            case Lineup.SOLD_OUT:
                // TODO
                break;
        }
    }

    private static final int REQUEST_CREATE_REVIEW = 110;

    @OnClick(R.id.create_review_button)
    void onCreateReviewButtonClicked() {
        if (!itemExists()) {
            return;
        }
        Intent view = new Intent(getActivity(), ReviewCreateActivity.class);
        view.putExtra("item", this.item);
        startActivityForResult(view, REQUEST_CREATE_REVIEW);
    }

    @OnClick(R.id.review_more_button)
    void onReviewMoreButtonClicked() {
        if (!itemExists()) {
            return;
        }
        Intent view = new Intent(getActivity(), ReviewsActivity.class);
        view.putExtra("item", this.item);
        startActivity(view);
    }

    @OnClick(R.id.favorite_button)
    void onFavoriteButtonClicked() {
        if (!itemExists()) {
            return;
        }
        ApiParams params = new ApiParams(self, true, ApiRoute.FAVORITE_ITEM);
        HashMap<String/*Key*/, List/*Arrayで送りたいやつ*/> dict = new HashMap<>();
        List<Integer> storeIds = Arrays.asList(item.getProductId());
        dict.put("product", storeIds);
        params.put(item.isFavorite() ? "delete" : "insert", dict);
        AppController.getInstance().sendGAEvent("Item", "Favorite", "detail", (long) item.getProductId());

        if (callbacks != null) {
            callbacks.onLoadProgress();
        }
        this.request.run(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        if (callbacks != null) {
                            callbacks.onLoadFinished();
                            callbacks.onItemFavoriteStatusChanged(item);
                            reloadItemInfo(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, e.getMessage());
                        e.printStackTrace();
                        if (callbacks != null) {
                            callbacks.onLoadFinished();
                        }
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
                                item.setFavorite(!item.isFavorite());
                                item.setFavoriteCount(item.getFavoriteCount() + (item.isFavorite() ? +1 : -1));
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    private boolean itemExists() {
        if (this.item == null || this.item.getAsset() == null) {
            return false;
        }
        return true;
    }

    private void showCloseError(ApiResponse apiResponse) {
        if (callbacks != null) {
            callbacks.onLoadFinished();
        }
        AppController.getInstance().showApiErrorAlert(getActivity(), apiResponse.getError(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    @OnClick(R.id.share_button)
    void shareButtonClicked() {
        if (!itemExists()) {
            return;
        }
        AppController.getInstance().sendGAEvent("Item", "Share", "", (long) this.item.getProductId());
        Intent v = new Intent();
        v.setAction(Intent.ACTION_SEND);
        v.setType("text/plain");
        v.putExtra(Intent.EXTRA_TEXT, this.item.getShareUrl());
        startActivity(v);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE_REVIEW && resultCode == Activity.RESULT_OK) {
            this.item.setDidReview(true);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    reloadItemInfo(false);
                    showReviews();
                }
            });
        }
    }
}
