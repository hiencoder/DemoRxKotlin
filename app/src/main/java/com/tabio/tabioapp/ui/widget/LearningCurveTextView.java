package com.tabio.tabioapp.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tabio.tabioapp.util.FontCache;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/21/15.
 */
public class LearningCurveTextView extends TextView {
    private static final String TAG = makeLogTag(LearningCurveTextView.class);

    public static final String FONT_FILENAME = "learningcurve_tt.ttf";

    public LearningCurveTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        applyCustomFont(context);
    }

    public LearningCurveTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        applyCustomFont(context);
    }

    public LearningCurveTextView(Context context) {
        super(context);

        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = FontCache.getTypeface(FONT_FILENAME, context);
        setTypeface(customFont);
    }
}
