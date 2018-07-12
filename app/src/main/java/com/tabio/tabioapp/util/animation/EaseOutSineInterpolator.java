package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseOutSineInterpolator.
 */
public class EaseOutSineInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return (float) (Math.sin(input * (Math.PI / 2)));
    }
}
