package com.tabio.tabioapp.coordinate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mugen.attachers.BaseAttacher;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Coordinate;
import com.tabio.tabioapp.scan.ScannerActivity;
import com.tabio.tabioapp.store.StoreActivity;
import com.tabio.tabioapp.tutorial.ScreenTutorialFragment;
import com.tabio.tabioapp.ui.widget.card.CardCollectionView;
import com.tabio.tabioapp.util.CameraUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/3/15.
 */
public class CoordinatesCollectionActivity extends CoordinatesBaseActivity implements CardCollectionView.CardEventListener {
    private static final String TAG = makeLogTag(CoordinatesCollectionActivity.class);

    private List<Integer> readIds = new ArrayList<>();
    private CoordinateAdapter adapter;
    private boolean isLoading = false;

    @BindView(R.id.card_collection_view)
    CardCollectionView collectionView;

    @BindView(R.id.back_button)
    ImageButton backButton;

    public static final int SCANNER_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinates_collection);
        AppController.getInstance().sendGAScreen("スキャンコーデ");
        AppController.getInstance().decideTrack("570f2ad099c3634a425af4c2");
        getSupportActionBar().setTitle(getString(R.string.text_coordinate_title_new));
        ButterKnife.bind(this);

        this.collectionView.setContentResource(R.layout.coordinate_card_view);
        this.collectionView.setListener(this);
        this.collectionView.setStackMargin(20);
        this.adapter = new CoordinateAdapter(this, R.layout.coordinate_card_view, this);
        this.collectionView.setAdapter(adapter);
        refreshCoordinatesData(null,
                this.preferences.getInt(COORDINATE_SKIP_MIN, 0),
                this.preferences.getInt(COORDINATE_SKIP_MAX, 0));

        chkTutorial();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBackButton(0);
    }

    private void chkTutorial() {
        String key = getString(R.string.didShowCoordinatesTutorial);
        boolean didShowCoordinatesTutorial = preferences.getBoolean(key, false);
        if (getSupportFragmentManager().findFragmentByTag(ScreenTutorialFragment.TAG) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentByTag(ScreenTutorialFragment.TAG))
                    .commit();
        }
        if (!didShowCoordinatesTutorial) {
            preferences.edit().putBoolean(key, true).commit();
            String[] fileNames = new String[]{"coordinate_tutorial_1","coordinate_tutorial_2"};
            int[] stringResIds = new int[]{R.string.text_coordinate_description_description1,R.string.text_coordinate_description_description2};
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_content, ScreenTutorialFragment.newInstance(fileNames,stringResIds), ScreenTutorialFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void favoriteFinished(Coordinate coordinate) {
//        this.coordinatesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onLoadFinishedCoordinatesData(List<Coordinate> coordinates) {
        this.adapter.addAll(coordinates);
        refreshBackButton(0);

        if (total == 0 || coordinates.size() < 1 && total == adapter.getCount()) {
            showNoDataView(R.string.text_coordinate);
        } else {
            hideNoDataView();
        }
        isLoading = false;
    }

    private void disallowClickBackButton() {
        this.backButton.setEnabled(false);
        this.backButton.setAlpha(0.3f);
    }

    private void allowClickBackButton() {
        this.backButton.setEnabled(true);
        this.backButton.setAlpha(1.f);
    }

    private void refreshBackButton(int currentIndex) {
        // no data
        if (this.adapter.getObjects().size() < 1) {
            disallowClickBackButton();
            return;
        }

        // first one
        if (currentIndex == 0) {
            disallowClickBackButton();
            return;
        }

        // last one
        if (currentIndex >= this.adapter.getObjects().size()) {
            allowClickBackButton();
            return;
        }

        // already judged. back to previous one
        if (this.adapter.getObjects().get(currentIndex).isJudged()) {
            disallowClickBackButton();
            return;
        }
        allowClickBackButton();
    }

    @OnClick(R.id.back_button)
    void backButtonClicked() {
        int lastIndex = this.collectionView.getCurrIndex() - 2;
        this.collectionView.backToPreviousOne();
        if (lastIndex >= 0) {
            this.preferences.edit().putInt(
                    COORDINATE_SKIP_MAX, this.adapter.getObjects().get(lastIndex).getCoordinateId()
            ).commit();
        }

        hideNoDataView();
        refreshBackButton(this.collectionView.getCurrIndex());
    }

    @OnClick(R.id.scan_button)
    void onScanButtonClicked() {
        LOGD(TAG, "scanButtonClicked");
        AppController.getInstance().sendGAEvent("Coordinate", "Scan", "", 0);
        if (CameraUtils.allowUseCamera(this)) {
            scan();
        } else {
            requestCameraPermission();
        }
    }

    private void scan() {
        Intent view = new Intent(this, ScannerActivity.class);
        startActivityForResult(view, SCANNER_REQUEST_CODE);
    }

    @Override
    protected void canUseCamera() {
        super.canUseCamera();
        scan();
    }

    @Override
    public void onCoordinateCardItemClicked(View view, Coordinate coordinate, int position) {
        super.onCoordinateCardItemClicked(view, coordinate, position);
        int id = view.getId();
        if (id == this.collectionView.getCurrIndex() + 100) {
            // 商品画面へ遷移
            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(this.adapter.getObjects().get(this.collectionView.getCurrIndex()));
            showItem(coordinates, this.collectionView.getCurrIndex());
        } else if (id == R.id.store_name) {
            Intent v = new Intent(this, StoreActivity.class);
            int onlineStoreId = BuildConfig.DEBUG?804:1623;
            if (coordinate.getStoreId() == onlineStoreId /*オンラインストア*/) {
                return;
            }
            v.putExtra("store_id", String.valueOf(coordinate.getStoreId()));
            startActivity(v);
        }
    }

    @Override
    public boolean swipeEnd(int section, float distance) {
        if (reaction != null) {
            reaction.setAlpha(0.f);
        }
        return (distance > 500) ? true : false;
    }

    @Override
    public boolean swipeStart(int section, float distance) {
        return false;
    }

    private ImageView reaction = null;

    @Override
    public boolean swipeContinue(int section, float distanceX, float distanceY) {

        float alpha = distanceX / 400;
        if (reaction == null) {
            int currentPosition = this.collectionView.getCurrIndex() + 100;
            View container = (CardView) this.collectionView.findViewById(currentPosition);
            reaction = (ImageView) container.findViewById(R.id.reaction);
        }
        int reactionRedId = (section == 1 || section == 3) ? R.drawable.ic_favorite_large : R.drawable.ic_skip_large;
        reaction.setImageResource(reactionRedId);

        reaction.setAlpha(alpha);
        return (distanceX > 600) ? true : false;
    }

    @Override
    public void discarded(int mIndex, int direction) {
        reaction = null;
        int discardedIndex = mIndex - 1;
        this.adapter.getObjects().get(discardedIndex).setJudged(true);

        Coordinate discardedCoordinate = this.adapter.getObjects().get(discardedIndex);

        this.readIds.add(discardedCoordinate.getCoordinateId());
        this.preferences.edit().putInt(
                COORDINATE_SKIP_MIN, discardedCoordinate.getCoordinateId()).commit();

        refreshBackButton(mIndex);

        boolean favoriteDirection = direction == 1 || direction == 3;
        if (favoriteDirection && !discardedCoordinate.isFavorite()) {
            favorite(discardedCoordinate);
        } else {
            AppController.getInstance().sendGAEvent("Coordinate", "Skip", discardedCoordinate.getStoreName(), (long) discardedCoordinate.getCoordinateId());
        }
        if (mIndex == 0) {
            this.preferences.edit().putInt(
                    COORDINATE_SKIP_MAX, discardedCoordinate.getCoordinateId()).commit();
        }

        if (this.adapter.getCount() == total && mIndex == this.adapter.getCount()) {
            showNoDataView(R.string.text_coordinate);
        } else {
            hideNoDataView();
            if ((adapter.getCount() - mIndex) <= 6 && !isLoading) {
                isLoading = true;
                refreshCoordinatesData(null);
            }
        }
    }

    @Override
    public void topCardTapped() {
        int currentPosition = this.collectionView.getCurrIndex();
        LOGD(TAG, "current position" + currentPosition);
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(this.adapter.getObjects().get(this.collectionView.getCurrIndex()));
        showItem(coordinates, currentPosition);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case SCANNER_REQUEST_CODE: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent view = new Intent(CoordinatesCollectionActivity.this, VerticalCoordinatesActivity.class);
                        view.putExtra("jan", data.getStringExtra("text"));
                        startActivity(view);
                    }
                }, 1000);

                break;
            }
        }
    }
}
