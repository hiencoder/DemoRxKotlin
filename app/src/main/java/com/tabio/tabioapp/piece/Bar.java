package com.tabio.tabioapp.piece;

import com.tabio.tabioapp.R;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Bar.
 */
public class Bar extends ImageView {

    public static final int NUM = 18;

    public static final float DEGREE_PITCH = 360 / NUM;

    private static final float WIDTH_RATIO_SHORT = 0.025f;

    private static final float WIDTH_RATIO_LONG = 0.035f;

    private static final float HEIGHT_RATIO_SHORT = 0.45f;
    private static final float HEIGHT_RATIO_LONG = 0.5f;

    private Param param;

    public static Param createParamShort(float viewWidth, float viewHeight) {
        Param param = new Param();
        param.viewWidth = viewWidth;
        param.viewHeight = viewHeight;
        param.targetWidth = viewWidth * WIDTH_RATIO_SHORT;
        param.targetHeight = viewWidth * HEIGHT_RATIO_SHORT;
        return param;
    }

    public static Param createParamLong(float viewWidth, float viewHeight) {
        Param param = new Param();
        param.viewWidth = viewWidth;
        param.viewHeight = viewHeight;
        param.targetWidth = viewWidth * WIDTH_RATIO_LONG;
        param.targetHeight = viewWidth * HEIGHT_RATIO_LONG;
        return param;
    }

    public static class Param {

        public float viewWidth;

        public float viewHeight;

        public float targetWidth;

        public float targetHeight;

        @Override
        public String toString() {
            return "Param viewWidth=" + viewWidth + " viewHeight=" + viewHeight
                    + " targetWidth=" + targetWidth + " targetHeight=" + targetHeight;
        }
    }

    Bar(Context context) {
        super(context);
    }

    public void setColor(int color) {
        setColorFilter(new LightingColorFilter(color, 1));
    }

    public static Bar createBar(ViewGroup parent, Param param) {
        Bar bar = new Bar(parent.getContext());
        parent.addView(bar);
        bar.getLayoutParams().width = (int) param.targetWidth;
        bar.getLayoutParams().height = (int) param.targetHeight;
        bar.setImageResource(R.drawable.bar_animation);
        bar.stopAnimation();
        bar.setPivotX(param.targetWidth * 0.5f);
        bar.setPivotY(param.targetHeight);
        bar.param = param;
        bar.move(param.viewWidth / 2, param.viewHeight / 2);
        return bar;
    }

    public void move(float x, float y) {
        float mx = x - param.targetWidth * 0.5f;
        float my = y - param.targetHeight;
        setTranslationX(mx);
        setTranslationY(my);
    }


    public void startAnimation() {
        final Drawable d = getDrawable();
        if (d != null && d instanceof AnimationDrawable) {
            AnimationDrawable ad = (AnimationDrawable) d;
            if (ad.isRunning()) {
                ad.stop();
            }
            ad.start();
        }
    }

    public void stopAnimation() {
        final Drawable d = getDrawable();
        if (d != null && d instanceof AnimationDrawable) {
            AnimationDrawable ad = (AnimationDrawable) d;
            if (ad.isRunning()) {
                ad.stop();
            }
        }
    }

    public long getDuration() {
        final Drawable d = getDrawable();
        if (d != null && d instanceof AnimationDrawable) {
            AnimationDrawable ad = (AnimationDrawable) d;
            long duration = 0;
            for (int i = 0; i < ad.getNumberOfFrames(); i++) {
                duration += ad.getDuration(i);
            }
            return duration;
        } else {
            return 0;
        }
    }
}
