package com.tabio.tabioapp.coupon;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.model.Coupon;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.ui.SimpleRecyclerAdapter;
import com.tabio.tabioapp.util.ImageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class CouponListFragment extends BaseFragment {
    public static final String TAG = makeLogTag(CouponListFragment.class);

    private float defaultPositionCouponId = 0;
    private List<Coupon> coupons;

    private CouponListCallbacks callbacks;
    private ApiRequest apiRequest;

    private CouponListDataAdapter adapter;

    private int index = 0;
    private static final int COUNT = 100;

    @BindView(R.id.coupon_list)
    RecyclerView couponRecyclerView;

    private Unbinder unbinder;

    public CouponListFragment() {
        // Required empty public constructor
    }

    public interface CouponListCallbacks {
        //        void onCouponListItemClicked(Coupon coupon);
        void onCouponDetailButtonClicked(Coupon coupon);
    }

    public static CouponListFragment newInstance() {
        return newInstance(0);
    }

    public static CouponListFragment newInstance(float defaultPositionCouponId) {
        CouponListFragment fragment = new CouponListFragment();
        Bundle args = new Bundle();
        args.putFloat("defaultPositionCouponId", defaultPositionCouponId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.apiRequest = new ApiRequest(getActivity());

        if (getArguments() != null) {
            this.defaultPositionCouponId = getArguments().getInt("defaultPosition", 0);
        }
        LOGD(TAG, "defaultPositionCouponId:"+this.defaultPositionCouponId);
        this.coupons = new ArrayList<>();

        AppController.getInstance().decideTrack("570f2a0899c3634a425af4be");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CouponListCallbacks) {
            callbacks = (CouponListCallbacks) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CouponListCallbacks");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coupon_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new CouponListDataAdapter(getActivity());
        couponRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        couponRecyclerView.setAdapter(this.adapter);

        refreshCouponData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void refreshCouponData() {
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_COUPON);
        this.index += 1;
        params.put("index", this.index);
        params.put("count", COUNT);
        showNetworkProgress();
        this.apiRequest.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, e.getMessage());
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        try {
                            if (apiResponse.hasError()) {
                                AppController.getInstance().showApiErrorAlert(getActivity(), apiResponse.getError());
                                hideProgress();
                                return;
                            }
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            int total = result.getInt("total");
                            LOGD(TAG, "coupon total:" + total);
                            JSONArray objects = result.getJSONArray("coupon");
                            if (objects.length() < 1) {
                                showNoDataView(R.string.text_coupon_title);
                            } else {
                                hideNoDataView();
                            }
                            int position = 0;
                            for (int i = 0; i < objects.length(); i++) {
                                Coupon coupon = new Coupon(getActivity(), objects.getJSONObject(i));
                                if (Float.parseFloat(coupon.getCouponId()) == defaultPositionCouponId) {
                                    position = i;
                                }
                                coupons.add(coupon);
                            }
                            adapter.notifyDataSetChanged();
                            couponRecyclerView.scrollToPosition(position);
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }


    class CouponListDataAdapter extends RecyclerView.Adapter<CouponListDataAdapter.CouponViewHolder> {

        private LayoutInflater inflater;

        public CouponListDataAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public CouponListDataAdapter.CouponViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.coupon_item, parent, false);
            return new CouponViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CouponListDataAdapter.CouponViewHolder holder, int position) {
            final Coupon coupon = coupons.get(position);
            holder.detailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callbacks != null) {
                        callbacks.onCouponDetailButtonClicked(coupon);
                    }
                }
            });
            holder.title.setText(coupon.getType());
            LOGD(TAG, coupon.getName()+":"+coupon.getImgUrl());
            LOGD(TAG, "横:"+holder.img.getMeasuredWidth());

            holder.img.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Picasso.with(getActivity())
                            .load(coupon.getImgUrl())
                            .placeholder(R.drawable.placeholder_white)
                            .error(R.drawable.placeholder_white)
                            .into(holder.img, new Callback() {
                                @Override
                                public void onSuccess() {

                                }
                                @Override
                                public void onError() {
                                    LOGE(TAG, "coupon image dl failed:"+coupon.getImgUrl());
                                }
                            });
                }
            });
            holder.name.setText(coupon.getName());
            holder.date.setText(coupon.getDate(self.getLanguage()));
            holder.description.setText(coupon.getStores());
            holder.description.setVisibility(coupon.getStores() != null ? View.VISIBLE : View.GONE);
            Picasso.with(getActivity())
                    .load(coupon.getBarcodeImgUrl())
                    .placeholder(R.drawable.placeholder_white)
                    .fit()
                    .error(R.drawable.placeholder_white)
                    .into(holder.couponBarcode);
            holder.barcodeNumber.setText(coupon.getCode());
//            holder.caution.setText(coupon.getComment());
//            holder.caution.setVisibility(coupon.getComment() != null ? View.VISIBLE : View.GONE);
            Picasso.with(getActivity())
                    .load(self.getBarcodeUrl())
                    .placeholder(R.drawable.placeholder_white)
                    .fit()
                    .error(R.drawable.placeholder_white)
                    .into(holder.myBarcode);
            holder.myBarcodeNumber.setText(self.getTabioId());
        }

        @Override
        public int getItemCount() {
            return coupons.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        class CouponViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.img)
            ImageView img;
            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.date)
            TextView date;
            @BindView(R.id.description)
            TextView description;
            @BindView(R.id.detail_button)
            Button detailButton;
            @BindView(R.id.barcode)
            ImageView couponBarcode;
            @BindView(R.id.barcode_number)
            TextView barcodeNumber;
//            @BindView(R.id.caution)
//            TextView caution;
            @BindView(R.id.my_barcode)
            ImageView myBarcode;
            @BindView(R.id.my_barcode_number)
            TextView myBarcodeNumber;


            public CouponViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
