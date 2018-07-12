package com.tabio.tabioapp.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * ImeUtils.
 */
public class ImeUtils {

    public static void hideScreenKeyboard(@NonNull Activity activity) {
        final View focus = activity.getCurrentFocus();
        if (focus != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void hideScreenKeyboard(@NonNull View view) {
        Context context = view.getContext();
        if (context != null && context instanceof Activity) {
            hideScreenKeyboard((Activity) context);
        }
    }
}
