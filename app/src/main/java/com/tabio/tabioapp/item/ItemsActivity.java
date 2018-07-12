package com.tabio.tabioapp.item;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.item.adapter.ItemDataAdapter;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.web.WebActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ItemsActivity extends CartBaseActivity {
    private static final String TAG = makeLogTag(ItemsActivity.class);

    private ItemsFragment itemsFragment;

    private ApiRequest request;
    private ApiParams params;
    private ItemFilter itemFilter;

    private static final int REQUEST_ITEM_FILTER = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        AppController.getInstance().sendGAScreen("アイテム一覧");
        getSupportActionBar().setTitle(getString(R.string.text_items2));
        ButterKnife.bind(this);
        this.request = new ApiRequest(this);
        this.itemFilter = new ItemFilter(this);

        this.params = new ApiParams(self, true, ApiRoute.GET_ITEMS);
        this.params.put("search", 0);
        this.params = itemFilter.addFilterParams(params);
        itemsFragment = ItemsFragment.newInstance(this.params, getString(R.string.text_no_item_search), getString(R.string.text_no_item_search_description));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.items_view, itemsFragment, ItemsFragment.TAG)
                .commit();
    }

    private void search(boolean clear) {
        if (this.itemFilter.isFiltering()) {
            getSupportActionBar().setTitle(getString(R.string.text_search_filtering));
        } else {
            getSupportActionBar().setTitle(getString(R.string.text_items2));
        }
        this.params = new ApiParams(self, true, ApiRoute.GET_ITEMS);
        this.params.put("search", 0);
        this.params = itemFilter.addFilterParams(this.params);
        this.itemsFragment.reload(this.params, clear);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search: {
                Intent view = new Intent(this, ItemFilterActivity.class);
                view.putExtra("filter", this.itemFilter);
                startActivityForResult(view, REQUEST_ITEM_FILTER);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ITEM_FILTER && resultCode == RESULT_OK) {
            if (data.getSerializableExtra("filter") != null) {
                this.itemFilter = (ItemFilter) data.getSerializableExtra("filter");
                search(true);
            }
        }
    }
}
