package com.tabio.tabioapp.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.util.StringUtils;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/23/16.
 */
public class BaseFragment extends Fragment {
    public static final String TAG = makeLogTag(BaseFragment.class);

    protected Me self;
    protected Handler handler;
    private View contentView;
    private ProgressBar networkProgress;
    private View noDataView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.self = AppController.getInstance().getSelf(false);
        this.handler = new Handler();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.contentView = view;
        this.networkProgress = (ProgressBar) view.findViewById(R.id.progress);
    }

    private ProgressBar getNetworkProgress() throws Exception {
        if (this.networkProgress == null) {
            this.networkProgress = (ProgressBar) this.contentView.findViewById(R.id.progress);
            if (this.networkProgress == null) {
                throw new RuntimeException("not found progress view");
            }
        }
        this.networkProgress.setVisibility(View.GONE);
        return this.networkProgress;
    }

    protected void showNetworkProgress() {
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    getNetworkProgress().setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    LOGE(TAG, "progress can't shown" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }


    protected void hideProgress() {
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    getNetworkProgress().setVisibility(View.GONE);
                } catch (Exception e) {
                    LOGE(TAG, "progress can't hide" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    protected View getNoDataView() throws Exception {
        if (this.noDataView == null) {
            this.noDataView = (View) this.contentView.findViewById(R.id.no_data_view);
        }
//        this.noDataView.setVisibility(View.VISIBLE);
//        this.noDataView.setVisibility(View.GONE);
        return this.noDataView;
    }

    protected void showNoDataView(final int stringId) {
        String text = getString(R.string.error_text_nodata, getString(stringId));
        showNoDataView(text);
    }

    protected void showNoDataView(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView title = (TextView) getNoDataView().findViewById(R.id.no_data_title);
                    title.setVisibility(View.VISIBLE);
                    title.setText(text);
                    TextView description = (TextView) getNoDataView().findViewById(R.id.no_data_description);
                    description.setVisibility(View.GONE);
                    getNoDataView().setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    LOGE(TAG, "nodataview can't shown" + e.getMessage());
                }
            }
        });
    }

    protected void showNoDataView(final int stringId, final int descStringId) {
        String text = getString(R.string.error_text_nodata, getString(stringId));
        String text2 = getString(descStringId);
        showNoDataView(text, text2);
    }

    protected void showNoDataView(final String text, final String text2) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView title = (TextView) getNoDataView().findViewById(R.id.no_data_title);
                    title.setVisibility(View.VISIBLE);
                    title.setText(text);
                    TextView description = (TextView) getNoDataView().findViewById(R.id.no_data_description);
                    description.setText(text2);
                    description.setVisibility(View.VISIBLE);
                    getNoDataView().setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    LOGE(TAG, "nodataview can't shown" + e.getMessage());
                }
            }
        });
    }

    protected void hideNoDataView() {
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    getNoDataView().setVisibility(View.GONE);
                } catch (Exception e) {
                    LOGE(TAG, "nodataview can't hide" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
