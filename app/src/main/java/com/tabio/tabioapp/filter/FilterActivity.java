package com.tabio.tabioapp.filter;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Filter;
import com.tabio.tabioapp.ui.BaseActivity;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.List;

import butterknife.OnClick;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 2/4/16.
 */
abstract public class FilterActivity extends BaseActivity {
    public static final String TAG = makeLogTag(FilterActivity.class);

    public Filter filter;
    public boolean isList = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @CallSuper
    public void showList(boolean open, Filter filter) {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(open ? FragmentTransaction.TRANSIT_FRAGMENT_OPEN : FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .replace(R.id.search_main_content, FilterListFragment.newInstance(filter), FilterListFragment.TAG)
                .commit();
        FilterChoiceListFragment fragment = (FilterChoiceListFragment)
                getSupportFragmentManager().findFragmentByTag(FilterChoiceListFragment.TAG);
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        isList = true;
    }

    @CallSuper
    public void showDetail(List<FilterModel> filterModels, int position) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.search_main_content, FilterChoiceListFragment.newInstance(filterModels, position), FilterChoiceListFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        isList = false;
    }

    @OnClick(R.id.search_result_text_left)
    public void onSearchResultTextLeftClicked() {
        if (!isList) {
            showList(false, filter);
        }
    }

    protected void addKeyboardListener() {
        KeyboardVisibilityEvent.setEventListener(
                this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        keyboardStatusChanged(isOpen);
                    }
                });
    }


    abstract protected void keyboardStatusChanged(boolean show);

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isList) {
                showList(false, filter);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
