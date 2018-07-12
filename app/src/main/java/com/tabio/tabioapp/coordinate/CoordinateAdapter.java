package com.tabio.tabioapp.coordinate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Coordinate;
import com.tabio.tabioapp.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 12/8/15.
 */

class CoordinateCallbacks {
    interface OnCoordinateCardViewCallbacks {
        void onCoordinateCardItemClicked(View view, Coordinate coordinate, int position);
    }
}


class VerticalCoordinateAdapter extends RecyclerView.Adapter<VerticalCoordinateAdapter.ViewHolder> {
    public static final String TAG = makeLogTag(VerticalCoordinateAdapter.class);

    private Context context;
    private LayoutInflater inflater;
    private List<Coordinate> objects;
    private CoordinateCallbacks.OnCoordinateCardViewCallbacks callbacks;

    public VerticalCoordinateAdapter(Context context, CoordinateCallbacks.OnCoordinateCardViewCallbacks callbacks) {
        this.context = context;
        this.inflater = LayoutInflater.from(this.context);
        this.objects = new ArrayList<>();
        this.callbacks = callbacks;
    }

    public void add(Coordinate coordinate) {
        this.objects.add(coordinate);
    }

    public void addAll(List<Coordinate> coordinates) {
        this.objects.addAll(coordinates);
        notifyDataSetChanged();
    }

    public List<Coordinate> getObjects() {
        return this.objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.coordinate_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Coordinate coordinate = this.objects.get(position);

        // TODO fix fucking codes
        holder.itemView.setId(R.id.card);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCoordinateCardItemClicked(v, coordinate, position);
            }
        });
//        holder.img.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {callbacks.onCoordinateCardItemClicked(v, coordinate);}});
//        holder.favoriteButton.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {callbacks.onCoordinateCardItemClicked(v, coordinate);}});
        holder.favoriteButton.setBackgroundResource(coordinate.isFavorite() ? R.drawable.ic_fav2 : R.drawable.ic_fav1);
        holder.favoriteCount.setText(String.valueOf(coordinate.getFavoriteCount()));

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCoordinateCardItemClicked(v, coordinate, position);
            }
        });
        holder.storeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCoordinateCardItemClicked(v, coordinate, position);
            }
        });

        int imgHeight = ViewUtils.getPixelFromDp(this.context, 400);
        if (holder.img.getLayoutParams().height != imgHeight) {
            holder.img.getLayoutParams().height = ViewUtils.getPixelFromDp(this.context, 400);
        }
//        .resize(ViewUtils.getPixelFromDp(this.context, 400), ViewUtils.getPixelFromDp(this.context, 400))
        Picasso.with(this.context)
                .load(coordinate.getImgUrl())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .into(holder.img);
        holder.favoriteCount.setText(String.valueOf(coordinate.getFavoriteCount()));
        if (coordinate.getChipImgUrl() != null && !coordinate.getChipImgUrl().equals("")) {
            Picasso.with(this.context)
                    .load(coordinate.getChipImgUrl())
                    .placeholder(R.drawable.placeholder_white)
                    .error(R.drawable.placeholder_white)
                    .into(holder.chip);
        }
        holder.storeName.setText(coordinate.getStoreNameWithBrand());
        holder.itemName.setText(coordinate.getName());
    }

    @Override
    public int getItemCount() {
        return this.objects.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.favorite_button)
        ImageButton favoriteButton;
        @BindView(R.id.favorite_count)
        TextView favoriteCount;
        @BindView(R.id.share_button)
        ImageButton shareButton;
        @BindView(R.id.chip)
        CircleImageView chip;
        @BindView(R.id.store_name)
        TextView storeName;
        @BindView(R.id.name)
        TextView itemName;
        @BindView(R.id.reaction)
        ImageView reaction;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}


class CoordinateAdapter extends ArrayAdapter<Coordinate> implements View.OnClickListener {
    private static final String TAG = makeLogTag(CoordinatesCollectionActivity.class);

    private CoordinateCallbacks.OnCoordinateCardViewCallbacks callbacks;
    private List<Coordinate> objects;
    private Context context;

    public CoordinateAdapter(Context context, int resource, CoordinateCallbacks.OnCoordinateCardViewCallbacks callbacks) {
        super(context, resource);
        this.context = context;
        this.callbacks = callbacks;
        this.objects = new ArrayList<>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Coordinate coordinate = this.objects.get(position);

        ImageView img = (ImageView) convertView.findViewById(R.id.img);
        Picasso.with(this.context)
                .load(coordinate.getImgUrl())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .into(img);

        ImageButton favoriteButton = (ImageButton) convertView.findViewById(R.id.favorite_button);
        favoriteButton.setBackgroundResource(coordinate.isFavorite() ? R.drawable.ic_fav2 : R.drawable.ic_fav1);

        TextView favoriteCount = (TextView) convertView.findViewById(R.id.favorite_count);
        favoriteCount.setText(String.valueOf(coordinate.getFavoriteCount()));

        ImageButton shareButton = (ImageButton) convertView.findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCoordinateCardItemClicked(v, coordinate, position);
            }
        });

        ImageView chip = (ImageView) convertView.findViewById(R.id.chip);

        if (coordinate.getChipImgUrl() != null && !coordinate.getChipImgUrl().equals("")) {
            Picasso.with(getContext())
                    .load(coordinate.getChipImgUrl())
                    .placeholder(R.drawable.placeholder_white)
                    .error(R.drawable.placeholder_white)
                    .into(chip);
        }


        TextView storeName = (TextView) convertView.findViewById(R.id.store_name);
        storeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCoordinateCardItemClicked(v, coordinate, position);
            }
        });
        storeName.setText(coordinate.getStoreNameWithBrand());

        TextView itemName = (TextView) convertView.findViewById(R.id.name);
        itemName.setText(coordinate.getName());

        convertView.setId(position + 100);// CoordinatesCollectionActivityで必要

        // convertViewにOnClickを受け取るとSwipeが効かなくなる
//        convertView.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {callbacks.onCoordinateCardItemClicked(v, coordinate);}});

        return convertView;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public int getCount() {
        return objects.size();
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
}



