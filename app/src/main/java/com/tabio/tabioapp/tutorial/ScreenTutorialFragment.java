package com.tabio.tabioapp.tutorial;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.util.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 2/16/16.
 */
public class ScreenTutorialFragment extends BaseFragment implements ViewPager.OnPageChangeListener {
    public static final String TAG = makeLogTag(ScreenTutorialFragment.class);

    private ScreenTutorialPagerAdapter adapter;

    View contentView;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.close_button)
    Button closeButton;
    @BindView(R.id.page_controls)
    LinearLayout pageControls;

    private Unbinder unbinder;

    private int[] descriptionStringResIds;
    private OnScreenTutorialFragmentCallbacks callbacks;

    public ScreenTutorialFragment() {
    }

    public interface OnScreenTutorialFragmentCallbacks {
        void onScreenTutorialClosed();
    }

    public static ScreenTutorialFragment newInstance(String[] fileNames, int[] stringResIds) {
        ScreenTutorialFragment fragment = new ScreenTutorialFragment();
        Bundle args = new Bundle();
        args.putStringArray("fileNames", fileNames);
        args.putIntArray("stringResIds", stringResIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.contentView = inflater.inflate(R.layout.fragment_screen_tutorial, container, false);
        unbinder = ButterKnife.bind(this, this.contentView);
        return contentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnScreenTutorialFragmentCallbacks) {
            callbacks = (OnScreenTutorialFragmentCallbacks) context;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.descriptionStringResIds = getArguments().getIntArray("stringResIds");
        this.adapter = new ScreenTutorialPagerAdapter(getActivity().getSupportFragmentManager(),
                getArguments().getStringArray("fileNames"));
        this.viewPager.setAdapter(this.adapter);
        this.viewPager.addOnPageChangeListener(this);
        setPageControlls();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setCurrentPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setPageControlls() {
        for (int i = 0; i < getArguments().getIntArray("stringResIds").length; i++) {
            ImageView pageControl = new ImageView(getActivity(), null, R.attr.pageControlStyle);
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(
                            getResources().getDimensionPixelSize(R.dimen.pagecontrol_width),
                            getResources().getDimensionPixelSize(R.dimen.pagecontrol_height));
            int leftMargin = getResources().getDimensionPixelSize(R.dimen.pagecontrol_left_margin);
            int rightMargin = getResources().getDimensionPixelSize(R.dimen.pagecontrol_right_margin);
            layoutParams.setMargins(leftMargin, 0, rightMargin, 0);
            pageControl.setLayoutParams(layoutParams);
            pageControl.setId(getArguments().getIntArray("stringResIds")[i]);
            pageControls.addView(pageControl);
        }
        setCurrentPage(0);
    }


    private void setCurrentPage(int page) {
        for (int i = 0; i < getArguments().getIntArray("stringResIds").length; i++) {
            ImageView pageControl = (ImageView) contentView.findViewById(getArguments().getIntArray("stringResIds")[i]);
            pageControl.setImageResource(i == page ? R.drawable.pagecontrol_selected : R.drawable.pagecontrol);
        }
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(ViewUtils.getPixelFromDp(getActivity(), 16),//left
                ViewUtils.getPixelFromDp(getActivity(), 20),//top
                ViewUtils.getPixelFromDp(getActivity(), 16),//right
                ViewUtils.getPixelFromDp(getActivity(), 14)//bottom
        );
        this.description.setText(getString(this.descriptionStringResIds[page]));
    }


    @OnClick(R.id.close_button)
    void onCloseButtonClicked() {
        if (callbacks != null) {
            callbacks.onScreenTutorialClosed();
        }
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(this)
                .commit();
    }
}
