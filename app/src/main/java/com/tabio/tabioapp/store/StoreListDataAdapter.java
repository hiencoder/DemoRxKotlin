package com.tabio.tabioapp.store;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.util.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/11/16.
 */
public class StoreListDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(StoreListDataAdapter.class);

    private Context context;
    private Me self;
    private List<Store> stores;
    private boolean showMap = false;
    private boolean checkin = false;
    private LayoutInflater inflater;
    private SupportMapFragment mapFragment = null;

    public static final int VIEW_TYPE_MAP = 1;
    public static final int VIEW_TYPE_STORE = 2;

    public OnStoreListDataAdapterCallbacks callbacks;

    public interface OnStoreListDataAdapterCallbacks {
        void onActionButtonClicked(Store store);

        void onStoreItemClicked(Store store);

        void initializedMap(SupportMapFragment map);
    }

    public StoreListDataAdapter(Context c, boolean showMap, boolean checkin, OnStoreListDataAdapterCallbacks callbacks) {
        this.context = c;
        this.self = AppController.getInstance().getSelf(false);
        this.stores = new ArrayList<>();
        this.showMap = showMap;
        this.checkin = checkin;
        this.inflater = LayoutInflater.from(this.context);
        this.callbacks = callbacks;
        if (showMap && this.mapFragment == null) {
            this.mapFragment = SupportMapFragment.newInstance();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_MAP:
                return new MapViewHolder(this.inflater.inflate(R.layout.map_base_view, parent, false));
            case VIEW_TYPE_STORE:
                return new StoreViewHolder(this.inflater.inflate(R.layout.store_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MAP:
                if (mapFragment != null && !mapFragment.isAdded()) {
                    ((AppCompatActivity) this.context).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.map_view, mapFragment, StoreListDataAdapter.TAG)
                            .commit();
                    this.callbacks.initializedMap(this.mapFragment);
                }
                break;
            case VIEW_TYPE_STORE:
                final Store store = this.stores.get(showMap ? position - 1 : position);
                StoreViewHolder sh = (StoreViewHolder) holder;
                sh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onStoreItemClicked(store);
                        }
                    }
                });

                sh.date.setVisibility(checkin ? View.VISIBLE : View.GONE);
                sh.date.setText(DateUtils.getDateFromFormat("yyyy/MM/dd HH:mm:ss", store.getCheckinDate(), self.getLanguage()));
                sh.storeName.setText(store.getNameWithBrand());
                if (!store.getDistance().equals("")) {
                    sh.distanceBaseView.setVisibility(View.VISIBLE);
                    sh.distance.setText(store.getDistanceForDisplay());
                    sh.distance.setGravity(Gravity.RIGHT);
                    sh.distanceUnit.setText(store.getDistanceUnit());
                } else {
                    sh.distanceBaseView.setVisibility(View.GONE);
                }
                sh.businessHoursWeekday.setText(context.getString(R.string.text_store_business_title_weekday)+" "+store.getTimeOfDayForDisplay());
                sh.businessHoursHoliday.setText(context.getString(R.string.text_store_business_title_holiday)+" "+store.getTimeOfHolidayForDisplay());

                if (this.checkin) {
                    sh.actionButton.setImageResource(R.drawable.ic_checkin_red);
                } else {
                    sh.actionButton.setImageResource(store.isFavorite() ? R.drawable.ic_fav2 : R.drawable.ic_fav1);
                    sh.actionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (callbacks != null) {
                                callbacks.onActionButtonClicked(store);
                            }
                        }
                    });
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        if (showMap) {
            return this.stores.size() + 1;
        }
        return this.stores.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (showMap && position == 0) {
            return VIEW_TYPE_MAP;
        }
        return VIEW_TYPE_STORE;
    }

    public List<Store> getStores() {
        return this.stores;
    }

    public void add(Store store) {
        this.stores.add(store);
    }

    public void addAll(List<Store> stores) {
        this.stores.addAll(stores);
        notifyDataSetChanged();
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.store_name)
        TextView storeName;
        @BindView(R.id.business_hours_weekday)
        TextView businessHoursWeekday;
        @BindView(R.id.business_hours_holiday)
        TextView businessHoursHoliday;
        @BindView(R.id.distance_base_view)
        View distanceBaseView;
        @BindView(R.id.distance)
        TextView distance;
        @BindView(R.id.distance_unit)
        TextView distanceUnit;
        @BindView(R.id.action_button)
        ImageButton actionButton;

        public StoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class MapViewHolder extends RecyclerView.ViewHolder {

        public MapViewHolder(View itemView) {
            super(itemView);
        }
    }
}
