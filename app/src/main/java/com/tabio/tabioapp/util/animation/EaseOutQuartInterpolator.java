package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseOutQuartInterpolator.
 */
public class EaseOutQuartInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        input -= 1;
        return 1 - input * input * input * input;
    }
}
