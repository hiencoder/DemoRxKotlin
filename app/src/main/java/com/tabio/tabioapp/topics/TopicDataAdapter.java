package com.tabio.tabioapp.topics;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.ui.widget.RoundCornerTransform;
import com.tabio.tabioapp.ui.widget.SquareImageView;
import com.tabio.tabioapp.util.ViewUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/14/16.
 */
public class TopicDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(TopicDataAdapter.class);

    private Me self;
    private Context context;
    private List<Topic> objects;
    private LayoutInflater inflater;
    private OnTopicCallbacks callbacks;

    public TopicDataAdapter(Context context, OnTopicCallbacks callbacks) {
        this.self = AppController.getInstance().getSelf(false);
        this.context = context;
        this.objects = new ArrayList<>();
        this.inflater = LayoutInflater.from(this.context);
        this.callbacks = callbacks;
    }

    public interface OnTopicCallbacks {
        void onTopicClicked(Topic topic);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.topic_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Topic object = this.objects.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callbacks != null) {
                    callbacks.onTopicClicked(object);
                }
            }
        });
        vh.body.setText(object.getTitle());
        vh.date.setText(object.getDateForDisplay(self.getLanguage()));

        Picasso.with(context)
                .load(object.getThumbnail())
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .fit()
                .centerCrop()
                .transform(new RoundCornerTransform(ViewUtils.getPixelFromDp(context,6), ViewUtils.getPixelFromDp(context,6)))
                .into(vh.img);
    }

    @Override
    public int getItemCount() {
        return this.objects.size();
    }

    public List<Topic> getObjects() {
        return this.objects;
    }

    public void add(Topic object) {
        this.objects.add(object);
    }

    public void addAll(List<Topic> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

//        @Bind(R.id.card_view)
//        CardView cardView;
        @BindView(R.id.img)
        SquareImageView img;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.body)
        TextView body;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
