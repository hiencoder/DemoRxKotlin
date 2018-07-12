package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseInCubicInterpolator.
 */
public class EaseInCubicInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return input * input * input;
    }
}
