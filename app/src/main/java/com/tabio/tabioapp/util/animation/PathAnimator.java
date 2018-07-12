package com.tabio.tabioapp.util.animation;

import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * PathAnimator.
 */
public class PathAnimator implements ValueAnimator.AnimatorUpdateListener {

    ValueAnimator pathAnimator = ValueAnimator.ofFloat(0f, 1f);

    View view;

    PathMeasure pathMeasure;

    float[] point = new float[2];

    public PathAnimator(View view, Path path) {
        this.view = view;
        this.pathMeasure = new PathMeasure(path, false);
        pathAnimator.addUpdateListener(this);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float f = animation.getAnimatedFraction();
        pathMeasure.getPosTan(pathMeasure.getLength() * f, point, null);
        view.setX(point[0]);
        view.setY(point[1]);
    }

    public ValueAnimator getValueAnimator() {
        return pathAnimator;
    }

    public PathAnimator setDuration(long duration) {
        pathAnimator.setDuration(duration);
        return this;
    }

    public PathAnimator setStartDelay(long startDelay) {
        pathAnimator.setStartDelay(startDelay);
        return this;
    }

    public PathAnimator setInterpolator(Interpolator interpolator) {
        pathAnimator.setInterpolator(interpolator);
        return this;
    }

    public void start() {
        pathAnimator.start();
    }
}
