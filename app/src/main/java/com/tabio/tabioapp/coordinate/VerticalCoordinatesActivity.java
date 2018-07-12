package com.tabio.tabioapp.coordinate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.model.Coordinate;
import com.tabio.tabioapp.store.StoreActivity;
import com.tabio.tabioapp.ui.RecyclerItemDividerDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class VerticalCoordinatesActivity extends CoordinatesBaseActivity {
    public static final String TAG = makeLogTag(VerticalCoordinatesActivity.class);

    @BindView(R.id.coordinates_view)
    RecyclerView coordinatesView;

    private VerticalCoordinateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_coordinates);
        AppController.getInstance().sendGAScreen("コーデスキャン結果");
        AppController.getInstance().decideTrack("570f2af299c3634a425af4c6");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);

        adapter = new VerticalCoordinateAdapter(this, this);
        coordinatesView.setHasFixedSize(false);
        coordinatesView.addItemDecoration(new RecyclerItemDividerDecoration(this, R.drawable.transparent_divider));
        coordinatesView.setLayoutManager(new LinearLayoutManager(this));
        coordinatesView.setAdapter(this.adapter);
        LOGD(TAG, "JAN:" + getIntent().getStringExtra("jan"));
        refreshCoordinatesData(getIntent().getStringExtra("jan"));
    }

    @Override
    protected void onLoadFinishedCoordinatesData(List<Coordinate> coordinates) {
        this.adapter.addAll(coordinates);
        if (coordinates.size() < 1) {
            showNoDataView(R.string.text_coordinate);
        } else {
            hideNoDataView();
            getSupportActionBar().setTitle(coordinates.get(0).getName());
        }
    }

    @Override
    protected void favoriteFinished(Coordinate coordinate) {
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onCoordinateCardItemClicked(View view, Coordinate coordinate, int position) {
        super.onCoordinateCardItemClicked(view, coordinate, position);
        int id = view.getId();
        if (id == R.id.card) {
            showItem(this.adapter.getObjects(), position);
        } else if (id == R.id.store_name) {
            Intent v = new Intent(this, StoreActivity.class);
            if (coordinate.getStoreId() == 782/*オンラインストア*/) {
                return;
            }
            v.putExtra("store_id", String.valueOf(coordinate.getStoreId()));
            startActivity(v);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
