package com.tabio.tabioapp.blog;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Blog;
import com.tabio.tabioapp.model.Me;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/7/16.
 */
public class BlogDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(BlogDataAdapter.class);

    private List<Blog> blogs;
    private OnBlogDataAdapterCallbacks callbacks;
    private Me self;

    public BlogDataAdapter(List<Blog> blogs, OnBlogDataAdapterCallbacks callbacks) {
        this.blogs = blogs;
        this.callbacks = callbacks;
        this.self = AppController.getInstance().getSelf(false);
    }

    public interface OnBlogDataAdapterCallbacks {
        void onBlogItemClicked(Blog blog);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_item, parent, false);
        final BlogViewHolder holder = new BlogViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Blog blog = this.blogs.get(position);
        BlogViewHolder bvh = (BlogViewHolder) holder;
        bvh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callbacks != null) {
                    callbacks.onBlogItemClicked(blog);
                }
            }
        });
        bvh.date.setText(blog.getDateForDisplay(self.getLanguage()));
        bvh.title.setText(blog.getTitle());
        bvh.description.setText(blog.getDescription());
    }

    @Override
    public int getItemCount() {
        return this.blogs.size();
    }

    class BlogViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.description)
        TextView description;
        public BlogViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
