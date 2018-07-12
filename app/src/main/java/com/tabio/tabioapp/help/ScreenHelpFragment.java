package com.tabio.tabioapp.help;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tabio.tabioapp.R;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ScreenHelpFragment extends Fragment {
    public static final String TAG = makeLogTag(ScreenHelpFragment.class);

    private int helpId;
    private int[] layoutIds;
    private int[] stringIds;
    private int[] imageResIds;
    private int myPosition;

    private ImageView image;
    private TextView description;

    public ScreenHelpFragment() {
        // Required empty public constructor
    }

    public static ScreenHelpFragment newInstance(int helpId, int[] layoutIds, int[] stringIds, int[] imageResIds, int position) {
        ScreenHelpFragment fragment = new ScreenHelpFragment();
        Bundle args = new Bundle();
        args.putInt("helpId", helpId);
        args.putIntArray("layoutIds", layoutIds);
        args.putIntArray("stringIds", stringIds);
        args.putIntArray("imageResIds", imageResIds);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            this.helpId = args.getInt("helpId");
            this.layoutIds = args.getIntArray("layoutIds");
            this.stringIds = args.getIntArray("stringIds");
            this.imageResIds = args.getIntArray("imageResIds");
            this.myPosition = args.getInt("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(this.layoutIds[this.myPosition], container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.image = (ImageView) view.findViewById(R.id.image);
        this.image.setBackgroundResource(this.imageResIds[this.myPosition]);

        int descriptionStringId = this.stringIds[this.myPosition];
        if (descriptionStringId != 0) {
            this.description = (TextView) view.findViewById(R.id.description);
            description.setText(getString(descriptionStringId));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
