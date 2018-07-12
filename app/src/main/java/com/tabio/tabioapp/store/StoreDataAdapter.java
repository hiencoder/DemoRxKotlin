package com.tabio.tabioapp.store;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Blog;
import com.tabio.tabioapp.model.Coordinate;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.ui.widget.RoundCornerTransform;
import com.tabio.tabioapp.util.ViewUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/6/16.
 */
public class StoreDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(StoreDataAdapter.class);

    private Me self;
    private Context context;
    private LayoutInflater inflater;
    private Store store;
    private List<StoreViewModel> viewModels;
    private SupportMapFragment map;
    public OnMapReadyCallback mapReadyCallback;
    public OnStoreDataAdapterCallbacks callbacks;

    public StoreDataAdapter(Context context, Store store, List<StoreViewModel> viewModels, OnMapReadyCallback mapReadyCallback, OnStoreDataAdapterCallbacks callbacks) {
        this.self = AppController.getInstance().getSelf(false);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.store = store;
        this.viewModels = viewModels;
        this.mapReadyCallback = mapReadyCallback;
        if (this.map == null) {
            this.map = SupportMapFragment.newInstance();
            this.map.getMapAsync(this.mapReadyCallback);
        }
        this.callbacks = callbacks;
    }

    public interface OnStoreDataAdapterCallbacks {
        void onGoogleMapOpenButtonClicked();

        void onCallButtonClicked();

        void onStoreFavoriteButtonClicked();

        void onCoordinateFavoriteButtonClicked(Coordinate coordinate, final int position);

        void onCoordinateItemClicked(Coordinate coordinate, final int position);

        void onCoordinatesMoreButtonClicked();

        void onBlogItemClicked(Blog blog);

        void onBlogsMoreButtonClicked();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case StoreViewModel.VIEW_TYPE_MAP:
                return new MapViewHolder(inflater.inflate(R.layout.map_base_view, parent, false));
            case StoreViewModel.VIEW_TYPE_BASIC_INFO:
                return new BasicInfoViewHolder(inflater.inflate(R.layout.store_basic_info, parent, false));
            case StoreViewModel.VIEW_TYPE_TYPES:
                return new StoreServicesViewHolder(inflater.inflate(R.layout.store_services, parent, false));
            case StoreViewModel.VIEW_TYPE_ACTIONS:
                return new ActionsViewHolder(inflater.inflate(R.layout.store_action_buttons, parent, false));
            case StoreViewModel.VIEW_TYPE_ACCESS:
                return new AccessViewHolder(inflater.inflate(R.layout.store_access, parent, false));
            case StoreViewModel.VIEW_TYPE_COORDINATE_TITLE:
                return new TitleViewHolder(inflater.inflate(R.layout.title_item2, parent, false));
            case StoreViewModel.VIEW_TYPE_COORDINATE:
                return new CoordinatesViewHolder(inflater.inflate(R.layout.item_item, parent, false));
            case StoreViewModel.VIEW_TYPE_COORDINATE_MORE_BUTTON:
                return new ButtonViewHolder(inflater.inflate(R.layout.simple_clickable_item2, parent, false));
            case StoreViewModel.VIEW_TYPE_BLOG_TITLE:
                return new TitleViewHolder(inflater.inflate(R.layout.title_item2, parent, false));
            case StoreViewModel.VIEW_TYPE_BLOG:
                return new BlogViewHolder(inflater.inflate(R.layout.blog_item, parent, false));
            case StoreViewModel.VIEW_TYPE_BLOG_MORE_BUTTON:
                return new ButtonViewHolder(inflater.inflate(R.layout.simple_clickable_item2, parent, false));
            case StoreViewModel.VIEW_TYPE_NODATA:
                return new NoDataViewHolder(inflater.inflate(R.layout.no_data_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        int viewType = getItemViewType(position);
        switch (viewType) {
            case StoreViewModel.VIEW_TYPE_MAP:
                layoutParams.setFullSpan(true);
                ((StoreActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.map_view, map, StoreDataAdapter.TAG)
                        .commit();
                break;
            case StoreViewModel.VIEW_TYPE_BASIC_INFO: {
                layoutParams.setFullSpan(true);
                BasicInfoViewHolder bivh = (BasicInfoViewHolder) holder;
                bivh.storeName.setText(store.getNameWithBrand());
                bivh.address.setText(store.getAddress());
                bivh.tel.setText(this.context.getString(R.string.text_store_tel) + "  " + store.getTel());
                bivh.operationDate.setText(this.context.getString(R.string.text_store_operation_date) + "  " + store.getOperationDate());
                String operationTime = this.context.getString(R.string.text_store_operation_time) + "  ";
                operationTime += store.getTimeOfDayForDisplay();
                operationTime += "（" + this.context.getString(R.string.text_store_business_title_weekday) + "）";
                operationTime += " / ";
                operationTime += store.getTimeOfHolidayForDisplay();
                operationTime += "（" + this.context.getString(R.string.text_store_business_title_holiday) + "）";
                bivh.operationTime.setText(operationTime);

//                bivh.operationTime.setText(this.context.getString(R.string.text_store_operation_time)+" "+store.getOperationDate());
                break;
            }
            case StoreViewModel.VIEW_TYPE_TYPES: {
                layoutParams.setFullSpan(true);
                StoreServicesViewHolder tvh = (StoreServicesViewHolder) holder;
                for (int i = 0; i < store.getServices().size(); i++) {
                    String serviceId = store.getServices().get(i);
                    switch (serviceId) {
                        case Store.LADIES:
                            tvh.icon1.setImageResource(R.drawable.ic_type_on_mens);
                            tvh.text1.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.MENS:
                            tvh.icon2.setImageResource(R.drawable.ic_type_on_ladies);
                            tvh.text2.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.KIDS:
                            tvh.icon3.setImageResource(R.drawable.ic_type_on_kids);
                            tvh.text3.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.EMBROIDERY:
                            tvh.icon4.setImageResource(R.drawable.ic_type_on_embroidery);
                            tvh.text4.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.PRINTING:
                            tvh.icon5.setImageResource(R.drawable.ic_type_on_print);
                            tvh.text5.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.NONSKID:
                            tvh.icon6.setImageResource(R.drawable.ic_type_on_pot);
                            tvh.text6.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.DUTY_FREE:
                            tvh.icon7.setImageResource(R.drawable.ic_type_on_taxfree);
                            tvh.text7.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.CHINA_UNIONPAY:
                            tvh.icon8.setImageResource(R.drawable.ic_type_on_card);
                            tvh.text8.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.PIECE:
                            tvh.icon9.setImageResource(R.drawable.ic_type_on_piece);
                            tvh.text9.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                        case Store.POINT:
                            tvh.icon10.setImageResource(R.drawable.ic_type_on_point);
                            tvh.text10.setTextColor(ContextCompat.getColor(this.context, R.color.greenDark200));
                            break;
                    }
                }
                break;
            }
            case StoreViewModel.VIEW_TYPE_ACTIONS: {
                layoutParams.setFullSpan(true);
                ActionsViewHolder avh = (ActionsViewHolder) holder;
                avh.favoriteButton.setCompoundDrawablesWithIntrinsicBounds(store.isFavorite() ? R.drawable.ic_btn_fav : R.drawable.ic_btn_fav_gray, 0, 0, 0);
                avh.favoriteButton.setTextColor(ContextCompat.getColor(this.context, store.isFavorite() ? R.color.redDark600 : R.color.grayLight200));
                avh.favoriteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onStoreFavoriteButtonClicked();
                        }
                    }
                });
                avh.mapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onGoogleMapOpenButtonClicked();
                        }
                    }
                });
                avh.callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onCallButtonClicked();
                        }
                    }
                });
                break;
            }
            case StoreViewModel.VIEW_TYPE_ACCESS: {
                layoutParams.setFullSpan(true);
                AccessViewHolder avh = (AccessViewHolder) holder;
                avh.access.setText(store.getAccess());
                break;
            }
            case StoreViewModel.VIEW_TYPE_COORDINATE_TITLE: {
                layoutParams.setFullSpan(true);
                TitleViewHolder tvh = (TitleViewHolder) holder;
                tvh.title.setText(this.context.getString(R.string.text_coordinate_store_title, store.getNameWithBrand()));
                break;
            }
            case StoreViewModel.VIEW_TYPE_COORDINATE: {
                layoutParams.setFullSpan(false);
                if (store.getCoordinates().size() > 0) {
                    final int coordinatePosition = position - store.getCoordinateStartPosition();
                    final Coordinate coordinate = store.getCoordinates().get(coordinatePosition);
                    CoordinatesViewHolder h = (CoordinatesViewHolder) holder;
                    h.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (callbacks != null) {
                                callbacks.onCoordinateItemClicked(coordinate, coordinatePosition);
                            }
                        }
                    });
                    h.itemView.setBackgroundColor(ContextCompat.getColor(this.context, R.color.white));
                    h.name.setText(coordinate.getName());
                    h.favoriteCount.setText(String.valueOf(coordinate.getFavoriteCount()));
                    h.favoriteImg.setImageResource(coordinate.isFavorite() ? R.drawable.ic_fav2 : R.drawable.ic_fav1);
                    h.favoriteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (callbacks != null) {
                                callbacks.onCoordinateFavoriteButtonClicked(coordinate, coordinatePosition);
                            }
                        }
                    });
                    h.favoriteView.setBackgroundResource(coordinate.isFavorite() ? R.drawable.item_card_bottom_pink_background : R.drawable.item_card_bottom_gray_background);
                    h.favoriteCount.setTextColor(ContextCompat.getColor(this.context, coordinate.isFavorite() ? R.color.redDark600 : R.color.grayLight200));
                    Picasso.with(this.context)
                            .load(coordinate.getImgUrl())
                            .transform(new RoundCornerTransform(ViewUtils.getPixelFromDp(context,6), ViewUtils.getPixelFromDp(context,6)))
                            .placeholder(R.color.white)
                            .error(R.color.white)
                            .into(h.img);
                }
                break;
            }
            case StoreViewModel.VIEW_TYPE_COORDINATE_MORE_BUTTON: {
                layoutParams.setFullSpan(true);
                ButtonViewHolder bvh = (ButtonViewHolder) holder;
                bvh.title.setText(context.getString(R.string.button_more));
                bvh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onCoordinatesMoreButtonClicked();
                        }
                    }
                });
                break;
            }
            case StoreViewModel.VIEW_TYPE_BLOG_TITLE:
                layoutParams.setFullSpan(true);
                TitleViewHolder tvh = (TitleViewHolder) holder;
                tvh.title.setText(this.context.getString(R.string.text_blog_title, store.getNameWithBrand()));
                break;
            case StoreViewModel.VIEW_TYPE_BLOG: {
                layoutParams.setFullSpan(true);
                if (store.getBlogs().size() > 0) {
                    int blogPosition = position - store.getBlogStartPosition();
                    final Blog blog = store.getBlogs().get(blogPosition);
                    BlogViewHolder bvh = (BlogViewHolder) holder;
                    bvh.title.setText(blog.getTitle());
                    bvh.description.setText(blog.getDescription());
                    bvh.date.setText(blog.getDateForDisplay(self.getLanguage()));
                    bvh.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (callbacks != null) {
                                callbacks.onBlogItemClicked(blog);
                            }
                        }
                    });
                }
                break;
            }
            case StoreViewModel.VIEW_TYPE_BLOG_MORE_BUTTON: {
                layoutParams.setFullSpan(true);
                ButtonViewHolder bvh = (ButtonViewHolder) holder;
                bvh.title.setText(context.getString(R.string.button_more));
                bvh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbacks != null) {
                            callbacks.onBlogsMoreButtonClicked();
                        }
                    }
                });
                break;
            }
            case StoreViewModel.VIEW_TYPE_NODATA: {
                layoutParams.setFullSpan(true);
                NoDataViewHolder nvh = (NoDataViewHolder) holder;
                TextView text = (TextView) nvh.itemView;
                text.setVisibility(View.VISIBLE);
                text.setText(viewModels.get(position).getText());
                break;
            }

        }
    }

    public void updateStore(Store store, List<StoreViewModel> viewModels) {
        this.store = store;
        this.viewModels = viewModels;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.viewModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.viewModels.get(position).getViewType();
    }

    class MapViewHolder extends RecyclerView.ViewHolder {
        public MapViewHolder(View itemView) {
            super(itemView);
        }
    }

    class BasicInfoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.store_name)
        TextView storeName;
        @BindView(R.id.address)
        TextView address;
        @BindView(R.id.tel)
        TextView tel;
        @BindView(R.id.operation_date)
        TextView operationDate;
        @BindView(R.id.operation_time)
        TextView operationTime;

        public BasicInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class StoreServicesViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.store_type_icon_1)
        ImageView icon1;
        @BindView(R.id.store_type_icon_2)
        ImageView icon2;
        @BindView(R.id.store_type_icon_3)
        ImageView icon3;
        @BindView(R.id.store_type_icon_4)
        ImageView icon4;
        @BindView(R.id.store_type_icon_5)
        ImageView icon5;
        @BindView(R.id.store_type_icon_6)
        ImageView icon6;
        @BindView(R.id.store_type_icon_7)
        ImageView icon7;
        @BindView(R.id.store_type_icon_8)
        ImageView icon8;
        @BindView(R.id.store_type_icon_9)
        ImageView icon9;
        @BindView(R.id.store_type_icon_10)
        ImageView icon10;

        @BindView(R.id.store_type_text_1)
        TextView text1;
        @BindView(R.id.store_type_text_2)
        TextView text2;
        @BindView(R.id.store_type_text_3)
        TextView text3;
        @BindView(R.id.store_type_text_4)
        TextView text4;
        @BindView(R.id.store_type_text_5)
        TextView text5;
        @BindView(R.id.store_type_text_6)
        TextView text6;
        @BindView(R.id.store_type_text_7)
        TextView text7;
        @BindView(R.id.store_type_text_8)
        TextView text8;
        @BindView(R.id.store_type_text_9)
        TextView text9;
        @BindView(R.id.store_type_text_10)
        TextView text10;

        public StoreServicesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ActionsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.map_button)
        Button mapButton;
        @BindView(R.id.call_button)
        Button callButton;
        @BindView(R.id.favorite_button)
        Button favoriteButton;

        public ActionsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class AccessViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.access)
        TextView access;

        public AccessViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class CoordinatesViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.favorite_bg)
        View favoriteView;
        @BindView(R.id.favorite_button)
        Button favoriteButton;
        @BindView(R.id.favorite_img)
        ImageView favoriteImg;
        @BindView(R.id.favorite_count)
        TextView favoriteCount;

        public CoordinatesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;

        public TitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ButtonViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;

        public ButtonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class BlogViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.arrow)
        ImageView arrow;

        public BlogViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class NoDataViewHolder extends RecyclerView.ViewHolder {
        public NoDataViewHolder(View itemView) {
            super(itemView);
        }
    }
}
