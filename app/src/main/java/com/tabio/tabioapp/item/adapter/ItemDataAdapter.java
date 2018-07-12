package com.tabio.tabioapp.item.adapter;

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
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.ui.widget.RoundCornerTransform;
import com.tabio.tabioapp.ui.widget.SquareImageView;
import com.tabio.tabioapp.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 12/11/15.
 */
public class ItemDataAdapter extends RecyclerView.Adapter<ItemDataAdapter.ViewHolder> {
    private static final String TAG = makeLogTag(ItemDataAdapter.class);

    private Context context;
    private List<Item> objects;
    private OnItemDataAdapterCallbacks callbacks;

    public ItemDataAdapter(Context context, OnItemDataAdapterCallbacks callbacks) {
        this.context = context;
        this.objects = new ArrayList<>();
        this.callbacks = callbacks;
    }

    public interface OnItemDataAdapterCallbacks {
        void onItemClicked(Item item, int position);
        void onItemFavoriteButtonClicked(Item item);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_item, null);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Item object = this.objects.get(position);

        Picasso.with(this.context)
                .load(object.getPictureImgUrl())
                .placeholder(R.color.white)
                .error(R.color.white)
                .fit()
                .transform(new RoundCornerTransform(ViewUtils.getPixelFromDp(context,6), ViewUtils.getPixelFromDp(context,6)))
                .centerCrop()
                .into(holder.img);

        holder.name.setText(object.getName());
        holder.favoriteCount.setText(String.valueOf(object.getFavoriteCount()));
        holder.favoriteCount.setTextColor(ContextCompat.getColor(this.context,  object.isFavorite()?R.color.redDark600:R.color.grayLight200));
        holder.favoriteImg.setImageResource(object.isFavorite()?R.drawable.ic_fav2:R.drawable.ic_fav1);
        holder.favoriteView.setBackgroundResource(object.isFavorite()?R.drawable.item_card_bottom_pink_background:R.drawable.item_card_bottom_gray_background);

        holder.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callbacks != null) {
                    callbacks.onItemFavoriteButtonClicked(object);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callbacks != null) {
                    callbacks.onItemClicked(object, position);
                }
            }
        });
    }

    public List<Item> getObjects() {
        return this.objects;
    }

    public void add(Item object) {
        this.objects.add(object);
    }

    public void addAll(List<Item> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img)
        SquareImageView img;
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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
