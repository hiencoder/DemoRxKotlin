package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseInQuadInterpolator.
 */
public class EaseInQuadInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return input * input;
    }
}
