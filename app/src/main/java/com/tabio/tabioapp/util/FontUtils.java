package com.tabio.tabioapp.util;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * FontUtils.
 */
public class FontUtils {

    public static Rect getTextBounds(String text, Typeface typeface, float textSize) {
        Rect rect = new Rect();
        Paint p = new Paint();
        p.setTypeface(typeface);
        p.setTextSize(textSize);
        p.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    public static Rect getTextBounds(TextView textView) {
        return getTextBounds(textView.getText().toString(), textView.getTypeface(),
                textView.getTextSize());
    }

    public static float getTextHeight(TextView textView) {
        Paint p = new Paint();
        p.setTypeface(textView.getTypeface());
        p.setTextSize(textView.getTextSize());
        Paint.FontMetrics fm = p.getFontMetrics();
        return Math.abs(fm.top) + fm.bottom;
    }
}
