package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseOutQuadInterpolator.
 */
public class EaseOutQuadInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return input * (2 - input);
    }
}
