package com.tabio.tabioapp.filter;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tabio.tabioapp.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class FilterChoiceListFragment extends Fragment {
    public static final String TAG = makeLogTag(FilterChoiceListFragment.class);

    @BindView(R.id.list_view)
    ListView listView;

    private FilterSelectableListAdapter adapter;

    private List<FilterModel> filterModels;

    private OnFilterChoiceListCallbacks callbacks;

    private Unbinder unbinder;

    public FilterChoiceListFragment() {
        // Required empty public constructor
    }

    public interface OnFilterChoiceListCallbacks {
        void onFilterListSelected(List<FilterModel> filterModels, int position);
    }

    public static FilterChoiceListFragment newInstance(List<FilterModel> filterModels, int position) {
        FilterChoiceListFragment fragment = new FilterChoiceListFragment();
        Bundle args = new Bundle();
        args.putSerializable("models", (Serializable) filterModels);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.filterModels = (List<FilterModel>) getArguments().getSerializable("models");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

        adapter = new FilterSelectableListAdapter(getActivity(), filterModels);
        listView.setAdapter(adapter);
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        filterModels.get(position).setSelect(!filterModels.get(position).isSelect());
        if (callbacks != null) {
            callbacks.onFilterListSelected(filterModels, getArguments().getInt("position"));
        }
        adapter.notifyDataSetChanged();
    }

    public class FilterSelectableListAdapter extends ArrayAdapter<FilterModel> {

        private LayoutInflater inflater;

        public FilterSelectableListAdapter(Context context, List<FilterModel> objects) {
            super(context, R.layout.filter_list_item, objects);

            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView != null) {
                viewHolder = (ViewHolder) convertView.getTag();
            } else {
                convertView = inflater.inflate(R.layout.filter_list_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }

            FilterModel filterModel = filterModels.get(position);

            viewHolder.title.setText(filterModel.getDisplayName());
            viewHolder.rightImg.setImageResource(filterModel.isSelect() ? R.drawable.ic_check : R.color.transparent);

            viewHolder.color.setVisibility(filterModel.isShowResource()?View.VISIBLE:View.GONE);
            if (filterModel.isShowResource()) {
                viewHolder.color.setImageResource(filterModel.getResourceId());
                if (filterModel.getResourceId() == R.color.white) {
                    viewHolder.color.setBorderWidth(2);
                } else {
                    viewHolder.color.setBorderWidth(0);
                }
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return FilterChoiceListFragment.this.filterModels.size();
        }

        public class ViewHolder {
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.right_icon)
            ImageView rightImg;
            @BindView(R.id.color)
            CircleImageView color;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFilterChoiceListCallbacks) {
            callbacks = (OnFilterChoiceListCallbacks) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FilterChoiceListCallbacks");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
