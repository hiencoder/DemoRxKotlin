package com.tabio.tabioapp.piece;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.PiecePointEvent;

import java.util.ArrayList;
import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 3/15/16.
 */
public class PointHistoryDataAdapter extends PointHistoryDataBaseAdapter {
    public static final String TAG = makeLogTag(PointHistoryDataAdapter.class);

    private Me self;
    private List<PiecePointEvent> objects;
    private int totalPoint;
    private String expiresDate;

    public PointHistoryDataAdapter(Context c, int totalPoint, String expiresDate) {
        super(c, POINT_HISTORY);
        this.self = AppController.getInstance().getSelf(false);
        this.objects = new ArrayList<>();
        this.totalPoint = totalPoint;
        this.expiresDate = expiresDate;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        PiecePointEvent object = null;
        if (viewType == VIEW_TYPE_HISTORY) {
            object = this.objects.get(position-3);
        }
        switch (viewType) {
            case VIEW_TYPE_TITLE:
                TitleViewHolder tvh = (TitleViewHolder) holder;
                tvh.itemView.setBackgroundColor(ContextCompat.getColor(this.context, R.color.whiteSmoke));
                String title = this.context.getString(position==0?R.string.text_histories_point_balance:R.string.text_main_title_expiresPoint);
                ((TextView)tvh.itemView).setText(title);
                break;
            case VIEW_TYPE_BALANCE:
                BalanceViewHolder bvh = (BalanceViewHolder) holder;
                bvh.value.setText(String.format("%1$,3d",this.totalPoint)+" ");
                bvh.unit.setText(this.context.getString(R.string.text_histories_unit_point));
                bvh.expiresDateText.setText(String.format(this.context.getString(R.string.text_piece_invalid), this.expiresDate));
                bvh.expiresDateText.setVisibility(this.totalPoint>0?View.VISIBLE:View.GONE);
                break;
            case VIEW_TYPE_HISTORY:
                HistoryViewHolder hvh = (HistoryViewHolder) holder;
                hvh.date.setText(object.getDateForDisplay(self.getLanguage()));
                hvh.body.setText(object.getName());
                hvh.value.setText(object.getPieceForDisplay());
                hvh.unit.setText(this.context.getString(this.historyMode==POINT_HISTORY?
                        R.string.text_histories_unit_point:
                        R.string.text_histories_unit_piece));
                break;
        }
    }

    public List<PiecePointEvent> getObjects() {
        return this.objects;
    }

    public void add(PiecePointEvent object) {
        this.objects.add(object);
    }

    public void addAll(List<PiecePointEvent> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }

    public void setTotalPoint(int totalPoint, String expiresDate) {
        this.totalPoint = totalPoint;
        this.expiresDate = expiresDate;
    }

    @Override
    public int getItemCount() {
        if (this.totalPoint > 0) {
            return this.objects.size() + 3;
        } else {
            return this.objects.size() + 2;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
