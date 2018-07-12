package com.tabio.tabioapp.item;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.filter.FilterActivity;
import com.tabio.tabioapp.filter.FilterChoiceListFragment;
import com.tabio.tabioapp.filter.FilterListFragment;
import com.tabio.tabioapp.filter.FilterModel;
import com.tabio.tabioapp.model.Filter;
import com.tabio.tabioapp.scan.ScannerActivity;
import com.tabio.tabioapp.ui.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ItemFilterActivity extends FilterActivity
        implements FilterListFragment.OnFilterListCallbacks, FilterChoiceListFragment.OnFilterChoiceListCallbacks {
    private static final String TAG = makeLogTag(ItemFilterActivity.class);

    @BindView(R.id.search_edit)
    EditText keywordForm;
    @BindView(R.id.search_result_text_left)
    TextView searchResultTitle;
    @BindView(R.id.search_result_text_right)
    TextView searchResultCount;
    @BindView(R.id.scan_button)
    View scanButton;
    @BindView(R.id.search_button)
    Button searchButton;
    

    private static final int SCANNER_REQUEST_CODE = 103;

    private ApiRequest request;

    public ItemFilterActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        AppController.getInstance().sendGAScreen("アイテム検索条件一覧");
        ButterKnife.bind(this);
        this.request = new ApiRequest(this);
        addKeyboardListener();

        this.scanButton.setVisibility(View.VISIBLE);

        if (getIntent().getSerializableExtra("filter") != null) {
            this.filter = (ItemFilter) getIntent().getSerializableExtra("filter");
        } else {
            this.filter = new ItemFilter(this);
        }

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.keywordForm.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    imm.hideSoftInputFromWindow(keywordForm.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    updateKeywordOnFilter();
                    return true;
                }
                return false;
            }
        });


        search(false);
        showList(true, this.filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onFilterListSelected(List<FilterModel> filterModels, int position) {
        this.filter.getFilterModelsList().set(position, filterModels);
        search(false);
    }

    @Override
    public void onFilterItemClicked(Filter filter, int position) {
        List<FilterModel> filterModels = filter.getFilterModelsList().get(position);
        showDetail(filterModels, position);
    }

    @Override
    public void onUpdateSelectedFilter(Filter filter) {
        this.filter = filter;
        search(false);
    }

    @Override
    public void showList(boolean open, Filter filter) {
        super.showList(open, filter);
        this.searchResultTitle.setText(getString(R.string.text_search_title));
    }

    @Override
    public void showDetail(List<FilterModel> filterModels, int position) {
        super.showDetail(filterModels, position);
        this.searchResultTitle.setText("< " + getString(R.string.button_back));
    }

    @OnClick(R.id.scan_button)
    void onScanButtonClicked() {
        Intent view = new Intent(this, ScannerActivity.class);
        startActivityForResult(view, SCANNER_REQUEST_CODE);
    }

    private void updateKeywordOnFilter() {
        FilterModel freewordFilterModel = this.filter.getFilterModelsList()
                .get(ItemFilter.ITEM_SEARCH_FREEWORD)
                .get(0);
        freewordFilterModel.setValue(this.keywordForm.getText().toString());
        showList(true, this.filter);
        this.keywordForm.setText("");
    }

    @OnClick(R.id.search_button)
    void onSearchButtonClicked() {
        search(true);
    }

    @OnClick(R.id.delete_button)
    void onKeywordDeleteButtonClicked() {
        this.keywordForm.setText("");
        updateKeywordOnFilter();
    }

    @OnTextChanged(R.id.search_edit)
    void onKeywordChanged(CharSequence text) {
    }

    private void search(boolean showResult) {
        if (showResult) {
            Intent data = new Intent();
            data.putExtra("filter", this.filter);
            setResult(RESULT_OK, data);
            finish();
            return;
        }
        ApiParams params = new ApiParams(self, true, ApiRoute.GET_ITEMS);
        params.put("index", 1);
        params.put("count", 1);
        params.put("search", 0);

        params = ((ItemFilter) filter).addFilterParams(params);
        showNetworkProgress();
        this.request.run(params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<ApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGE(TAG, e.getMessage());
                        hideProgress();
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        if (apiResponse.hasError()) {
                            AppController.getInstance().showApiErrorAlert(ItemFilterActivity.this, apiResponse.getError());
                            return;
                        }
                        try {
                            JSONObject result = apiResponse.getBody().getJSONObject("result");
                            final int total = result.getInt("total");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    searchResultCount.setText(String.valueOf(total) + getString(R.string.text_search_count));
                                }
                            });
                        } catch (JSONException e) {
                            LOGE(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void keyboardStatusChanged(boolean show) {
        this.searchButton.setVisibility(show?View.GONE:View.VISIBLE);
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
                        Intent view = new Intent(ItemFilterActivity.this, SwipeableItemsActivity.class);
                        view.putExtra("jan", data.getStringExtra("text"));
                        startActivity(view);
                    }
                }, 1000);

                break;
            }
        }
    }
}
