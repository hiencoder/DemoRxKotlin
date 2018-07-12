package com.tabio.tabioapp.tutorial;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.ui.BaseFragment;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ScreenTutorialItemFragment extends BaseFragment {
    public static final String TAG = makeLogTag(ScreenTutorialItemFragment.class);

    private String gifFileName;
//    private String description;

    @BindView(R.id.gif_view)
    GifImageView gifImageView;

    private Unbinder unbinder;

    public ScreenTutorialItemFragment() {
        // Required empty public constructor
    }

    public static ScreenTutorialItemFragment newInstance(String fileName) {
        ScreenTutorialItemFragment fragment = new ScreenTutorialItemFragment();
        Bundle args = new Bundle();
        args.putString("fileName", fileName);
//        args.putInt("stringResId", stringResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.gifFileName = getArguments().getString("fileName");
//            this.description = getString(getArguments().getInt("stringResId"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screen_tutorial_item, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            GifDrawable gifFromAssets = new GifDrawable(getActivity().getAssets(), this.gifFileName + ".gif");
            gifFromAssets.setLoopCount(0);
            this.gifImageView.setImageDrawable(gifFromAssets);
        } catch (IOException e) {
            LOGE(TAG, e.getMessage());
            e.printStackTrace();
        }
//        this.descriptionText.setText(this.description);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
