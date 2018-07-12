package com.tabio.tabioapp.order;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Order;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/14/16.
 */
public class OrderHistoryListDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(OrderHistoryListDataAdapter.class);

    private Context context;
    private Me self;
    private LayoutInflater inflater;
    private List<Order> objects;

    private OnOrderHistoryListDataAdapterCallbacks callbacks;

    public OrderHistoryListDataAdapter(Context context, OnOrderHistoryListDataAdapterCallbacks callbacks) {
        this.context = context;
        this.self = AppController.getInstance().getSelf(false);
        this.inflater = LayoutInflater.from(this.context);
        this.objects = new ArrayList<>();
        this.callbacks = callbacks;
    }

    public interface OnOrderHistoryListDataAdapterCallbacks {
        void onOrderHistoryItemClicked(Order order);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this.inflater.inflate(R.layout.order_history_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder) holder;
        final Order object = this.getObjects().get(position);
        vh.storeName.setText(object.getStoreNameWithBrand());
        vh.orderedDate.setText(object.getOrderedDateForDisplay(self.getLanguage()));
        vh.status.setText(object.getStatusName(this.context));
        vh.itemPrice.setText(object.getPriceWithYen());

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callbacks != null) {
                    callbacks.onOrderHistoryItemClicked(object);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.objects.size();
    }

    public List<Order> getObjects() {
        return this.objects;
    }

    public void add(Order object) {
        this.objects.add(object);
    }

    public void addAll(List<Order> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.purchased_date)
        TextView orderedDate;
        @BindView(R.id.purchase_status)
        TextView status;
        @BindView(R.id.store_name)
        TextView storeName;
        @BindView(R.id.item_price)
        TextView itemPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
