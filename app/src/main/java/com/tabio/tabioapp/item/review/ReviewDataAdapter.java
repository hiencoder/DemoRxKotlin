package com.tabio.tabioapp.item.review;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Review;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/14/16.
 */
public class ReviewDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(ReviewDataAdapter.class);

    private Context context;
    private List<Review> objects;
    private LayoutInflater inflater;
    private boolean myReviews = false;
    private boolean hideItemView = true;
    private Me self;

    private OnReviewDataAdapterCallbacks callbacks;

    public interface OnReviewDataAdapterCallbacks {
        void onReviewItemClicked(Review review);
    }

    public ReviewDataAdapter(Context context, boolean myReviews, boolean hideItemView, OnReviewDataAdapterCallbacks callbacks) {
        this.context = context;
        this.objects = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
        this.myReviews = myReviews;
        this.hideItemView = hideItemView;
        this.callbacks = callbacks;
        this.self = AppController.getInstance().getSelf(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReviewViewHolder(this.inflater.inflate(R.layout.review_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Review review = this.objects.get(position);

        ReviewViewHolder rvh = (ReviewViewHolder) holder;
        rvh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callbacks != null) {
                    callbacks.onReviewItemClicked(review);
                }
            }
        });
        rvh.itemName.setText(review.getItemName());
        Picasso.with(this.context)
                .load(review.getItemImgUrl())
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .into(rvh.itemImg);
        rvh.reviewItemView.setVisibility(this.hideItemView ? View.GONE:View.VISIBLE);
        rvh.itemBottomLine.setVisibility(this.hideItemView ? View.VISIBLE:View.GONE);
        rvh.status.setVisibility(this.myReviews?View.VISIBLE:View.GONE);
        if (this.myReviews) {
            rvh.status.setText(this.context.getString(review.isConfirmed()?R.string.text_review_confirmed:R.string.text_review_confirm_in));
        }
        rvh.reviewerName.setText(review.getReviewerNickname());
        rvh.reviewDate.setText(review.getDateDisplay(self.getLanguage()));
        rvh.reviewBody.setText(review.getComment());
        rvh.reviewBody.setMaxLines(2);
        rvh.reviewBody.setEllipsize(TextUtils.TruncateAt.END);
        Picasso.with(this.context)
                .load(review.getReviewerIconImgUrl())
                .placeholder(R.drawable.ic_mypage_blue_square)
                .error(R.drawable.ic_mypage_red_square)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(rvh.reviewerIcon);
    }

    @Override
    public int getItemCount() {
        return this.objects.size();
    }

    public List<Review> getObjects() {
        return this.objects;
    }

    public void add(Review object) {
        this.objects.add(object);
    }

    public void addAll(List<Review> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

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

        public ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
