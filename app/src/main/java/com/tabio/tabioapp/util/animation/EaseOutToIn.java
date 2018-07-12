package com.tabio.tabioapp.util.animation;

import android.view.animation.Interpolator;

/**
 * EaseOutToIn.
 *   fast - slow - fast
 */
public class EaseOutToIn implements Interpolator {

        @Override
        public float getInterpolation(float input) {
            double p = (double)input;
            double x = Math.pow(((p * 2.0 - 1.0) / 1.34), 3.0) + 0.4 + (p * 0.2);
            return (float)Math.min(1f, Math.max(0, x));
        }
    }
