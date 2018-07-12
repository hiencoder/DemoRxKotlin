package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseInSineInterpolator.
 */
public class EaseInSineInterpolator implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return (float) (1 - Math.cos(input * (Math.PI / 2)));
    }
}
