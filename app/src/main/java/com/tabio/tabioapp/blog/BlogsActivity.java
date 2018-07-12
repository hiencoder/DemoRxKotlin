package com.tabio.tabioapp.blog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Blog;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.web.WebActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class BlogsActivity extends BaseActivity implements BlogDataAdapter.OnBlogDataAdapterCallbacks {
    public static final String TAG = makeLogTag(BlogsActivity.class);

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private List<Blog> blogs;
    private BlogDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogs);
        AppController.getInstance().sendGAScreen("ブログ一覧");
        AppController.getInstance().decideTrack("570f2e7f99c3634a425af4e6");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        Store store = (Store) getIntent().getSerializableExtra("store");
        getSupportActionBar().setTitle(getString(R.string.text_blog_title, store.getNameWithBrand()));
        this.blogs = store.getBlogs();
        this.adapter = new BlogDataAdapter(this.blogs, this);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(this.adapter);
    }

    @Override
    public void onBlogItemClicked(Blog blog) {
        Intent view = new Intent(this, WebActivity.class);
        view.putExtra("url", blog.getLink());
        AppController.getInstance().sendGAScreen("ブログ詳細");
        startActivity(view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LOGD(TAG, "clicked home button");
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
