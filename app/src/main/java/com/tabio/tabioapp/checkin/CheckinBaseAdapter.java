package com.tabio.tabioapp.checkin;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.ui.widget.LearningCurveTextView;
import com.tabio.tabioapp.util.GpsUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/25/16.
 */
class CheckinBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(CheckinBaseAdapter.class);

    private Context context;
    private LayoutInflater inflater;
    private List<CheckinBaseViewModel> models;
    private OnCheckinBaseViewCallbacks callbacks;

    private SupportMapFragment mapFragment = null;

    public interface OnCheckinBaseViewCallbacks {
        void initializedMap(SupportMapFragment map);

        void onCheckinClicked(Store store);

        void onStoreClicked(Store store);

        void onFavoriteButtonClicked(Store store);

        void onHistoryButtonClicked();

        void onNearbyStoresButtonClicked();
    }

    public CheckinBaseAdapter(Context context, OnCheckinBaseViewCallbacks callbacks) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.callbacks = callbacks;

        this.models = new ArrayList<>();
        this.mapFragment = SupportMapFragment.newInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case CheckinBaseViewModel.VIEW_TYPE_VIEW_MAP:
                return new MapViewHolder(inflater.inflate(R.layout.map_view, parent, false));

            case CheckinBaseViewModel.VIEW_TYPE_TITLE_CAN_CHECKIN:
                return new TitleViewHolder(inflater.inflate(R.layout.title_item, parent, false));

            case CheckinBaseViewModel.VIEW_TYPE_VIEW_CHECKINABLE_STORE:
                return new StoreViewHolder(inflater.inflate(R.layout.store_item, parent, false));

            case CheckinBaseViewModel.VIEW_TYPE_VIEW_CHECKIN_HISTORIES:
                return new ListViewHolder(inflater.inflate(R.layout.simple_clickable_item, parent, false));

            case CheckinBaseViewModel.VIEW_TYPE_TITLE_NEARBY_STORES:
                return new TitleViewHolder(inflater.inflate(R.layout.title_item, parent, false));

            case CheckinBaseViewModel.VIEW_TYPE_VIEW_NEARBY_STORE:
                return new StoreViewHolder(inflater.inflate(R.layout.store_item, parent, false));

            case CheckinBaseViewModel.VIEW_TYPE_VIEW_NEARBY_STORES:
                return new ListViewHolder(inflater.inflate(R.layout.simple_clickable_item, parent, false));

            case CheckinBaseViewModel.VIEW_TYPE_VIEW_GRAY_BG:
                return new GrayViewHolder(inflater.inflate(R.layout.gray_specific_background, parent, false));

            case CheckinBaseViewModel.VIEW_TYPE_VIEW_NO_CHECKINABLE_STORE:
                return new NoCheckinableSoresViewHolder(inflater.inflate(R.layout.no_checkin_view, parent, false));

            default:
                LOGE(TAG, "viewType must be specific");
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CheckinBaseViewModel model = models.get(position);
        switch (getItemViewType(position)) {
            case CheckinBaseViewModel.VIEW_TYPE_VIEW_MAP:
                if (!mapFragment.isAdded()) {

                    ((CheckinActivity) this.context).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.map, mapFragment, CheckinBaseAdapter.TAG)
                            .commit();
                    this.callbacks.initializedMap(this.mapFragment);
//                    if (GpsUtils.allowAccessLocation(this.context)) {
//                        mapFragment.getMapAsync((CheckinActivity) this.context);
//                    }
                }
                break;

            // チェックイン可能店舗（タイトル）
            case CheckinBaseViewModel.VIEW_TYPE_TITLE_CAN_CHECKIN:
                ((TextView) ((TitleViewHolder) holder).itemView).setText(model.getTitle());
                break;

            // チェックイン可能店舗
            case CheckinBaseViewModel.VIEW_TYPE_VIEW_CHECKINABLE_STORE: {
                StoreViewHolder vh = ((StoreViewHolder) holder);
                Store store = model.getStore();
                vh.storeName.setText(store.getNameWithBrand());
                vh.businessHoursWeekday.setText(context.getString(R.string.text_store_business_title_weekday)+" "+store.getTimeOfDayForDisplay());
                vh.businessHoursHoliday.setText(context.getString(R.string.text_store_business_title_holiday)+" "+store.getTimeOfHolidayForDisplay());

                if (store.getDistance().equals("")) {
                    vh.distance.setVisibility(View.GONE);
                    vh.distanceUnit.setVisibility(View.GONE);
                } else {
                    vh.distance.setVisibility(View.VISIBLE);
                    vh.distanceUnit.setVisibility(View.VISIBLE);
                    vh.distance.setText(store.getDistanceForDisplay());
                    vh.distanceUnit.setText(store.getDistanceUnit());
                }
//                vh.actionButton.setImageResource(store.isFavorite() ? R.drawable.ic_fav2 : R.drawable.ic_fav1);
                vh.actionButton.setImageResource(R.drawable.ic_checkin_gray);

                // TODO Action button has to change title.
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        doCheckin(model.getStore());
                        if (callbacks != null) {
                            callbacks.onCheckinClicked(model.getStore());
                        }
                    }
                });
                break;
            }

            // チェックイン履歴もっと見る
            case CheckinBaseViewModel.VIEW_TYPE_VIEW_CHECKIN_HISTORIES:
                ((ListViewHolder) holder).title.setText(model.getTitle());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onHistoryButtonClicked();
                        }
                    }
                });
                break;

            // 近くの店舗（タイトル）
            case CheckinBaseViewModel.VIEW_TYPE_TITLE_NEARBY_STORES:
                ((TextView) ((TitleViewHolder) holder).itemView).setText(model.getTitle());
                break;

            // 近くの店舗
            case CheckinBaseViewModel.VIEW_TYPE_VIEW_NEARBY_STORE: {
                StoreViewHolder vh = ((StoreViewHolder) holder);
                Store store = model.getStore();
                vh.storeName.setText(store.getNameWithBrand());
                vh.businessHoursWeekday.setText(context.getString(R.string.text_store_business_title_weekday)+" "+store.getTimeOfDayForDisplay());
                vh.businessHoursHoliday.setText(context.getString(R.string.text_store_business_title_holiday)+" "+store.getTimeOfHolidayForDisplay());

                if (store.getDistance().equals("")) {
                    vh.distance.setVisibility(View.GONE);
                    vh.distanceUnit.setVisibility(View.GONE);
                } else {
                    vh.distance.setVisibility(View.VISIBLE);
                    vh.distanceUnit.setVisibility(View.VISIBLE);
                    vh.distance.setText(store.getDistanceForDisplay());
                    vh.distanceUnit.setText(store.getDistanceUnit());
                }
                vh.actionButton.setImageResource(store.isFavorite() ? R.drawable.ic_fav2 : R.drawable.ic_fav1);
                vh.actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onFavoriteButtonClicked(model.getStore());
                        }
                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onStoreClicked(model.getStore());
                        }
                    }
                });
                break;
            }

            // 近くの店舗もっと見る
            case CheckinBaseViewModel.VIEW_TYPE_VIEW_NEARBY_STORES:
                ((ListViewHolder) holder).title.setText(model.getTitle());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onNearbyStoresButtonClicked();
                        }
                    }
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.models.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return this.models.size();
    }


    public List<CheckinBaseViewModel> getObjects() {
        return models;
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.action_button)
        ImageButton actionButton;
        @BindView(R.id.store_name)
        TextView storeName;
        @BindView(R.id.business_hours_weekday)
        TextView businessHoursWeekday;
        @BindView(R.id.business_hours_holiday)
        TextView businessHoursHoliday;
        @BindView(R.id.distance)
        LearningCurveTextView distance;
        @BindView(R.id.distance_unit)
        LearningCurveTextView distanceUnit;


        public StoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        public TitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;

        public ListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class MapViewHolder extends RecyclerView.ViewHolder {
        public MapViewHolder(View itemView) {
            super(itemView);
        }
    }

    class GrayViewHolder extends RecyclerView.ViewHolder {
        public GrayViewHolder(View itemView) {
            super(itemView);
        }
    }

    class NoCheckinableSoresViewHolder extends RecyclerView.ViewHolder {
        public NoCheckinableSoresViewHolder(View itemView) {
            super(itemView);
        }
    }
}
