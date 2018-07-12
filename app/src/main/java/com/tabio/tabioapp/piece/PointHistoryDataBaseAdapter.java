package com.tabio.tabioapp.piece;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.ui.widget.LearningCurveTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/15/16.
 */
public abstract class PointHistoryDataBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = makeLogTag(PointHistoryDataBaseAdapter.class);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PIECE_HISTORY, POINT_HISTORY})
    public @interface HistoryMode {}
    public static final int PIECE_HISTORY = 0;
    public static final int POINT_HISTORY = 1;
    protected  @HistoryMode int historyMode;

    public static final int VIEW_TYPE_TITLE = 0;
    public static final int VIEW_TYPE_BALANCE = 1;
    public static final int VIEW_TYPE_HISTORY = 2;

    protected Context context;
    protected LayoutInflater inflater;

    public PointHistoryDataBaseAdapter(Context c, int historyMode) {
        this.context = c;
        this.historyMode = historyMode;
        this.inflater = LayoutInflater.from(this.context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TITLE:
                return new TitleViewHolder(this.inflater.inflate(R.layout.title_item, parent, false));
            case VIEW_TYPE_BALANCE:
                return new BalanceViewHolder(this.inflater.inflate(R.layout.point_balance_item, parent, false));
            case VIEW_TYPE_HISTORY:
                return new HistoryViewHolder(this.inflater.inflate(R.layout.point_history_item, parent, false));
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 2) {
            return VIEW_TYPE_TITLE;
        }
        if (position == 1) {
            return VIEW_TYPE_BALANCE;
        }
        return VIEW_TYPE_HISTORY;
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        public TitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class BalanceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.value) TextView value;
        @BindView(R.id.unit) TextView unit;
        @BindView(R.id.expires_date_text) TextView expiresDateText;

        public BalanceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date) TextView date;
        @BindView(R.id.body) TextView body;
        @BindView(R.id.value) LearningCurveTextView value;
        @BindView(R.id.unit) TextView unit;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
