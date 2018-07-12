package com.tabio.tabioapp.coordinate;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Coordinate;
import com.tabio.tabioapp.ui.widget.RoundCornerTransform;
import com.tabio.tabioapp.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/15/16.
 */
public class CoordinateItemAdapter extends RecyclerView.Adapter<CoordinateItemAdapter.ViewHolder> {
    public static final String TAG = makeLogTag(CoordinateItemAdapter.class);

    private Context context;
    private List<Coordinate> objects;
    private OnCoordinateItemAdapterCallbacks callbacks;

    public CoordinateItemAdapter(Context context, OnCoordinateItemAdapterCallbacks callbacks) {
        this.context = context;
        this.objects = new ArrayList<>();
        this.callbacks = callbacks;
    }

    public interface OnCoordinateItemAdapterCallbacks {
        void onCoordinateItemClicked(Coordinate coordinate, int position);

        void onCoordinateFavoriteButtonClicked(Coordinate coordinate);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_item, null);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Coordinate coordinate = this.objects.get(position);

        Picasso.with(this.context)
                .load(coordinate.getImgUrl())
                .placeholder(R.color.white)
                .error(R.color.white)
                .fit()
                .transform(new RoundCornerTransform(ViewUtils.getPixelFromDp(context, 6), ViewUtils.getPixelFromDp(context, 6)))
                .centerCrop()
                .into(holder.img);

        holder.name.setText(coordinate.getName());
        holder.favoriteCount.setText(String.valueOf(coordinate.getFavoriteCount()));
        holder.favoriteCount.setTextColor(ContextCompat.getColor(this.context, coordinate.isFavorite() ? R.color.redDark600 : R.color.grayLight200));
        holder.favoriteImg.setImageResource(coordinate.isFavorite() ? R.drawable.ic_fav2 : R.drawable.ic_fav1);
        holder.favoriteView.setBackgroundResource(coordinate.isFavorite() ? R.drawable.item_card_bottom_pink_background : R.drawable.item_card_bottom_gray_background);

        holder.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callbacks != null) {
                    callbacks.onCoordinateFavoriteButtonClicked(coordinate);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callbacks != null) {
                    callbacks.onCoordinateItemClicked(coordinate, position);
                }
            }
        });
    }

    public List<Coordinate> getObjects() {
        return this.objects;
    }

    public void add(Coordinate object) {
        this.objects.add(object);
    }

    public void addAll(List<Coordinate> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
