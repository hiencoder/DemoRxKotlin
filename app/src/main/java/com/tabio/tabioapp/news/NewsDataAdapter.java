package com.tabio.tabioapp.news;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.News;
import com.tabio.tabioapp.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/14/16.
 */
public class NewsDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(NewsDataAdapter.class);

    private Context context;
    private Me self;
    private LayoutInflater inflater;
    private List<News> objects;
    private OnNewsDataAdapterCallbacks callbacks;

    public NewsDataAdapter(Context context, OnNewsDataAdapterCallbacks callbacks) {
        this.context = context;
        this.self = AppController.getInstance().getSelf(false);
        this.inflater = LayoutInflater.from(this.context);
        this.objects = new ArrayList<>();
        this.callbacks = callbacks;
    }

    public interface OnNewsDataAdapterCallbacks {
        void onNewsItemClicked(News news, int position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.news_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final News object = this.objects.get(position);
        final ViewHolder vh = (ViewHolder) holder;
        vh.body.setText(object.getMessage());
        vh.date.setText(DateUtils.getDateFromFormat("yyyy/MM/dd HH:mm:ss", object.getDate(), self.getLanguage()));
    }

    public List<News> getObjects() {
        return this.objects;
    }

    public void add(News object) {
        this.objects.add(object);
    }

    public void addAll(List<News> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.objects.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date) TextView date;
        @BindView(R.id.news_body) TextView body;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
