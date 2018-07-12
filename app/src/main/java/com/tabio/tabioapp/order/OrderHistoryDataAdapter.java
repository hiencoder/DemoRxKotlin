package com.tabio.tabioapp.order;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Order;
import com.tabio.tabioapp.model.OrderDetail;
import com.tabio.tabioapp.util.DateUtils;
import com.tabio.tabioapp.util.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/26/16.
 */
public class OrderHistoryDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(OrderHistoryDataAdapter.class);

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_RECEIPT = 1;
    private static final int VIEW_TYPE_ACTION = 2;
    private static final int VIEW_TYPE_ITEM = 3;

    private Context context;
    private Me self;
    private LayoutInflater inflater;
    private Order object;

    private OnOrderHistoryDataAdapterCallbacks callbacks;

    public OrderHistoryDataAdapter(Context context, OnOrderHistoryDataAdapterCallbacks callbacks) {
        this.context = context;
        this.self = AppController.getInstance().getSelf(false);
        this.inflater = LayoutInflater.from(context);
        this.callbacks = callbacks;
    }

    public interface OnOrderHistoryDataAdapterCallbacks {
        void onItemClicked(OrderDetail orderDetail);

        void onItemFavoriteButtonClicked(OrderDetail orderDetail);

        void onItemPostReviewButtonClicked(OrderDetail orderDetail);

        void onActionButtonClicked(Order order);

        void onDeliveryCheckButtonClicked(Order order);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderHolder(inflater.inflate(R.layout.order_history_header, parent, false));
            case VIEW_TYPE_RECEIPT:
                return new ReceiptHolder(inflater.inflate(R.layout.order_receipt_item, parent, false));
            case VIEW_TYPE_ACTION:
                return new ActionHolder(inflater.inflate(R.layout.order_action_item, parent, false));
            case VIEW_TYPE_ITEM:
                return new ItemHolder(inflater.inflate(R.layout.order_history_detail_item, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER: {
                HeaderHolder vh = (HeaderHolder) holder;
                vh.title.setText(DateUtils.getDateFromFormat("yyyy/MM/dd HH:mm:ss", object.getOrderedDate(), self.getLanguage()));
                vh.status.setText(object.getStatusName(this.context));
                break;
            }
            case VIEW_TYPE_RECEIPT: {
                ReceiptHolder vh = (ReceiptHolder) holder;
                vh.price.setText(StringUtils.getWithYen(object.getSubTotal()));
                vh.discount.setText(StringUtils.getWithYen(object.getCouponDiscountPrice()));
                vh.usepoint.setText(StringUtils.getWithYen(object.getPointDiscountPrice()));
                vh.tax.setText(StringUtils.getWithYen(object.getTax()));
                vh.shippingFees.setText(StringUtils.getWithYen(object.getDeliverFee() + object.getCharge()));
                vh.totalPrice.setText(StringUtils.getWithYen(object.getPrice()));
                vh.getPiece.setText(String.valueOf(object.getAddPiece()));
                break;
            }
            case VIEW_TYPE_ACTION: {
                ActionHolder vh = (ActionHolder) holder;
                //1: 購入済、 2: 注文完了、 3: 発注待ち、 4: 発注済、 5: キャンセル、 6: 返品

                if (object.isOrderedByOnlineStore()) {
                    if (object.getStatus() == 1)/*購入済み*/ {
                        vh.actionButton.setText(this.context.getString(R.string.button_returned));
                        vh.deliveryButton.setVisibility(View.GONE);
                    } else if (object.getStatus() == 2) {
                        vh.actionButton.setText(this.context.getString(R.string.button_cancel));
                        vh.deliveryButton.setVisibility(View.GONE);
                    } else if (object.getStatus() == 4) {
                        vh.actionButton.setText(this.context.getString(R.string.button_returned));
                        vh.deliveryButton.setVisibility(View.VISIBLE);
                    } else {
                        vh.actionButton.setVisibility(View.GONE);
                        vh.deliveryButton.setVisibility(View.GONE);
                    }
                } else {
                    vh.actionButton.setVisibility(View.GONE);
                    vh.deliveryButton.setVisibility(View.GONE);
                }

                vh.actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onActionButtonClicked(object);
                        }
                    }
                });
                vh.deliveryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onDeliveryCheckButtonClicked(object);
                        }
                    }
                });
                break;
            }
            case VIEW_TYPE_ITEM: {
                ItemHolder vh = (ItemHolder) holder;
                final OrderDetail detail = object.getDetails().get(position - 3);
                vh.itemName.setText(detail.getName());
                Picasso.with(this.context)
                        .load(detail.getPictureUrl())
                        .placeholder(R.color.white)
                        .error(R.color.white)
                        .fit()
                        .centerCrop()
                        .into(vh.itemImg);
                vh.color.setText(detail.getColorCode() + ":" + detail.getColorName());
                vh.size.setText(detail.getSize());
                vh.quantity.setText(String.valueOf(detail.getQuantity()));
                vh.price.setText(StringUtils.getWithYen(detail.getPrice()));
                vh.favoriteCount.setText(String.valueOf(detail.getFavoriteCount()));
                vh.favoriteCount.setTextColor(ContextCompat.getColor(this.context, detail.isFavorite() ? R.color.redDark600 : R.color.grayLight200));
                vh.favoriteImg.setImageResource(detail.isFavorite() ? R.drawable.ic_fav2 : R.drawable.ic_fav1);
                vh.favoriteView.setBackgroundResource(detail.isFavorite() ? R.color.pinkLight : R.color.grayLight600);

                vh.favoriteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onItemFavoriteButtonClicked(detail);
                        }
                    }
                });
                vh.postReviewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onItemPostReviewButtonClicked(detail);
                        }
                    }
                });
                vh.itemImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onItemClicked(detail);
                        }
                    }
                });
                vh.itemName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onItemClicked(detail);
                        }
                    }
                });
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (this.object != null) {
            return 3 + this.object.getDetails().size();
        }
        return 0;
    }

    public Order getObject() {
        return this.object;
    }

    public void set(Order object) {
        this.object = object;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return VIEW_TYPE_HEADER;
            case 1:
                return VIEW_TYPE_RECEIPT;
            case 2:
                return VIEW_TYPE_ACTION;
            default:
                return VIEW_TYPE_ITEM;
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.order_status)
        TextView status;

        public HeaderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ReceiptHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.price)
        TextView price;
        @BindView(R.id.discount)
        TextView discount;
        @BindView(R.id.usepoint)
        TextView usepoint;
        @BindView(R.id.tax)
        TextView tax;
        @BindView(R.id.shipping_fees)
        TextView shippingFees;
        @BindView(R.id.total_price)
        TextView totalPrice;
        @BindView(R.id.get_piece)
        TextView getPiece;

        public ReceiptHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ActionHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.action_button)
        Button actionButton;
        @BindView(R.id.delivery_button)
        Button deliveryButton;

        public ActionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_name)
        TextView itemName;
        @BindView(R.id.item_img)
        ImageView itemImg;
        @BindView(R.id.color)
        TextView color;
        @BindView(R.id.size)
        TextView size;
        @BindView(R.id.quantity)
        TextView quantity;
        @BindView(R.id.price)
        TextView price;
        @BindView(R.id.favorite_bg)
        View favoriteView;
        @BindView(R.id.favorite_button)
        Button favoriteButton;
        @BindView(R.id.favorite_img)
        ImageView favoriteImg;
        @BindView(R.id.favorite_count)
        TextView favoriteCount;
        @BindView(R.id.post_review_button)
        Button postReviewButton;

        public ItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
