package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseInQuartInterpolator.
 */
public class EaseInQuartInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return input * input * input * input;
    }
}
