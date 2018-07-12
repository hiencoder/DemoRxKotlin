package com.tabio.tabioapp.util.animation;

import android.animation.Animator;
import android.support.annotation.CallSuper;

/**
 * AnimatorRepeatListener.
 */
public class AnimatorRepeatListener implements Animator.AnimatorListener {

    boolean canceled;

    @Override
    public void onAnimationStart(Animator animation) {
        canceled = false;
    }

    @CallSuper
    @Override
    public void onAnimationEnd(Animator animation) {
        if (!canceled) {
            onAnimationRepeat(animation);
            animation.start();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        canceled = true;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
