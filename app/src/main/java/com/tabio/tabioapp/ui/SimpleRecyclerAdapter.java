package com.tabio.tabioapp.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.tabio.tabioapp.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 1/25/16.
 */
public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.ViewHolder> {
    private static final String TAG = makeLogTag(SimpleRecyclerAdapter.class);

    private List<String> titles;

    public SimpleRecyclerAdapter(List<String> titles) {
        this.titles = titles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, null);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(this.titles.get(position));
    }


    @Override
    public int getItemCount() {
        return this.titles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1)
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
