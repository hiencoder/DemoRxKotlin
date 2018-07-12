package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseOutQuintInterpolator.
 */
public class EaseOutQuintInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        input -= 1;
        return 1 + input * input * input * input * input;
    }
}
