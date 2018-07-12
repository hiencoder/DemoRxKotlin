package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseOutQuintInterpolator.
 */
public class EaseInQuintInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return input * input * input * input * input;
    }
}
