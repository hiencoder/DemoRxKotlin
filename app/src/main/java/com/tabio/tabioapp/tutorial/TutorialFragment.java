package com.tabio.tabioapp.tutorial;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tabio.tabioapp.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class TutorialFragment extends Fragment {
    public static final String TAG = makeLogTag(TutorialFragment.class);

    @BindView(R.id.gif_view)
    GifImageView gifView;
    @BindView(R.id.logo)
    ImageView logo;

    private Unbinder unbinder;

    private boolean showLogo;
    private String gifFileName;

    public TutorialFragment() {
        // Required empty public constructor
    }

    public static TutorialFragment newInstance(boolean showLogo, String gifFileName) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putString("gifFileName", gifFileName);
        args.putBoolean("showLogo", showLogo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.gifFileName = getArguments().getString("gifFileName");
        this.showLogo = getArguments().getBoolean("showLogo");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (showLogo) {
            this.logo.setVisibility(View.VISIBLE);
            this.gifView.setVisibility(View.GONE);
        } else {
            this.logo.setVisibility(View.GONE);
            this.gifView.setVisibility(View.VISIBLE);
            try {
                GifDrawable gifFromAssets = new GifDrawable(getActivity().getAssets(), this.gifFileName + ".gif");
                gifFromAssets.setLoopCount(0);
                this.gifView.setImageDrawable(gifFromAssets);
            } catch (Exception e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
