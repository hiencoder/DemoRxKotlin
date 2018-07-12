package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseOutCubicInterpolator.
 */
public class EaseOutCubicInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        final float p = input - 1;
        return p * p * p + 1;
    }
}
