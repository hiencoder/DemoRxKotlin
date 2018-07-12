package com.tabio.tabioapp.filter;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Filter;
import com.tabio.tabioapp.store.StoreFilter;
import com.tabio.tabioapp.util.ViewUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class FilterListFragment extends Fragment {
    public static final String TAG = makeLogTag(FilterListFragment.class);

    private Filter filter;
    private FilterListAdapter adapter;
    private OnFilterListCallbacks callbacks;
    private LayoutInflater inflater;

    private View headerView;
    private View sortView;

    @BindView(R.id.list_view)
    ListView listView;

    private Unbinder unbinder;


    public interface OnFilterListCallbacks {
        void onFilterItemClicked(Filter filter, int position);

        void onUpdateSelectedFilter(Filter filter);
    }


    public FilterListFragment() {
        // Required empty public constructor
    }

    public static FilterListFragment newInstance(Filter filter) {
        FilterListFragment fragment = new FilterListFragment();
        Bundle args = new Bundle();
//        filter.setContext(fragment.getActivity());
        args.putSerializable("filter", filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().getSerializable("filter") != null) {
            filter = (Filter) getArguments().getSerializable("filter");
        }

        this.inflater = LayoutInflater.from(getActivity());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFilterListCallbacks) {
            callbacks = (OnFilterListCallbacks) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FilterListCallbacks");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new FilterListAdapter(getActivity(), filter.getTitles());
        View baseHeaderView = inflater.inflate(R.layout.filter_header_view, null, false);
        headerView = (View) baseHeaderView.findViewById(R.id.header_content);
        listView.addHeaderView(baseHeaderView);
        listView.setAdapter(adapter);
        if (this.filter.getFilterType() == Filter.FILTER_TYPE_ITEM) {
            sortView = (View) inflater.inflate(R.layout.filter_sort, null, false);
            listView.addFooterView(sortView);
        }

        updateFilterList();
    }

    public void updateFilterList() {
        for (int i = 0; i < this.filter.getFilterModelsList().size(); i++) {
            addHeaderListItem(i, this.filter.getFilterModelsList().get(i));
        }
        TextView textView = new TextView(getActivity());
        textView.setPadding(ViewUtils.getPixelFromDp(getActivity(), 10), ViewUtils.getPixelFromDp(getActivity(), 16), 0, ViewUtils.getPixelFromDp(getActivity(), 6));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grayLight600));
        textView.setText(getString(R.string.text_search_condition_title));
        textView.setLayoutParams(params);
        ((LinearLayout) this.headerView).addView(textView);
        FrameLayout line = new FrameLayout(getActivity());
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewUtils.getPixelFromDp(getActivity(), 1));
        line.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grayLight400));
        line.setLayoutParams(params);
        ((LinearLayout) this.headerView).addView(line);

        // ソート

        if (this.filter.getFilterType() == Filter.FILTER_TYPE_ITEM) {
            updateSortList();
            int[] sortViewResIds = new int[]{R.id.sort_new,R.id.sort_popular,R.id.sort_price_row,R.id.sort_price_high};

            for (int i=0; i<sortViewResIds.length; i++) {
                View sort = (View) sortView.findViewById(sortViewResIds[i]);
                sort.setTag(i+100);
                sort.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int selectedIndex = ((int)v.getTag())-100;
                        filter.getSortFilterModel().setSelectedSortType(selectedIndex+1);
                        updateSortList();
                        if (callbacks != null) {
                            callbacks.onUpdateSelectedFilter(filter);
                        }
                    }
                });
            }
        }

        this.adapter.notifyDataSetChanged();
        if (this.callbacks != null) {
            this.callbacks.onUpdateSelectedFilter(this.filter);
        }
    }

    public void updateSortList() {
        if (this.filter.getFilterType() == Filter.FILTER_TYPE_ITEM) {
            int[] checkIconResIds = new int[]{R.id.right_icon1,R.id.right_icon2,R.id.right_icon3,R.id.right_icon4};
            for (int i=0; i<checkIconResIds.length; i++) {
                ImageView check = (ImageView) sortView.findViewById(checkIconResIds[i]);
                boolean selected = this.filter.getSortFilterModel().getSelectedSortType()-1 == i?true:false;
                check.setVisibility(selected?View.VISIBLE:View.GONE);
            }
        }
    }

    private void addHeaderListItem(final int listPosition, List<FilterModel> models) {
        for (int i = 0; i < models.size(); i++) {
            FilterModel model = models.get(i);
            if (model.isSelect()) {
                final View view = inflater.inflate(R.layout.filter_header_list_item, null, false);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 5, 0, 0);
                view.setLayoutParams(params);
                ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
                deleteButton.setTag(i);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int listItemPosition = Integer.valueOf(v.getTag().toString());
                        filter.getFilterModelsList().get(listPosition).get(listItemPosition).setSelect(false);
                        filter.getFilterModelsList().get(listPosition).get(listItemPosition).setValue("");
                        ((LinearLayout) headerView).removeAllViews();
                        updateFilterList();
                    }
                });
                TextView title = (TextView) view.findViewById(R.id.filter_name);
                TextView body = (TextView) view.findViewById(R.id.filter_body);
                title.setText(filter.getListTitles()[listPosition]);
                if (model.getDisplayName().equals("")) {
                    body.setText(model.getValue());
                } else {
                    body.setText(model.getDisplayName());
                }
                ((LinearLayout) this.headerView).addView(view);
            }
        }
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        if (callbacks != null) {
            callbacks.onFilterItemClicked(filter, position - 1 + 1/*header分をマイナスする からの、フリーワード分を＋１*/);
        }
    }

    public static class FilterListAdapter extends ArrayAdapter<List<List>> {

        private String[] titles;

        private LayoutInflater inflater;

        public FilterListAdapter(Context context, String[] titles) {
            super(context, R.layout.filter_list_item);
            inflater = LayoutInflater.from(context);
            this.titles = titles;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            position ++;
            ViewHolder viewHolder;
            if (convertView != null) {
                viewHolder = (ViewHolder) convertView.getTag();
            } else {
                convertView = inflater.inflate(R.layout.filter_list_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }

            viewHolder.title.setText(this.titles[position]);
            return convertView;
        }

        @Override
        public int getCount() {
            return titles.length-1;//フリーワード分
        }

        public static class ViewHolder {
            @BindView(R.id.title)
            TextView title;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
