package com.tabio.tabioapp.piece;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Circle.
 */
public class Circle extends View {

    private static final float RADIUS_RATIO_LARGE = 0.3294f;

    private static final float RADIUS_RATIO_MIDDLE = 0.049f;

    private static final float RADIUS_RATIO_SMALL = 0.0093f;

    private int color;

    private float radius;

    private Paint paint;

    static Circle createCircle(ViewGroup parent, float radiusRatio) {
        Circle circle = new Circle(parent.getContext());
        parent.addView(circle);
        float radius = (float) parent.getWidth() * radiusRatio;
        int size = (int)(radius * 2f);
        circle.radius = radius;
        circle.getLayoutParams().width = size;
        circle.getLayoutParams().height = size;
        return circle;
    }

    public static CircleFrameLayout createLarge(ViewGroup parent) {
        CircleFrameLayout layout = new CircleFrameLayout(parent.getContext());
        parent.addView(layout);
        float radius = (float) parent.getWidth() * RADIUS_RATIO_LARGE;
        int size = (int) (radius * 2f);
        layout.getLayoutParams().width = size;
        layout.getLayoutParams().height = size;
        Circle circle = new Circle(parent.getContext());
        layout.setCircle(circle);
        circle.getLayoutParams().width = size;
        circle.getLayoutParams().height = size;
        circle.radius = radius;
        layout.move(parent.getWidth() / 2, parent.getHeight() / 2);
        return layout;
    }

    public static Circle createMiddle(ViewGroup parent) {
        Circle circle = createCircle(parent, RADIUS_RATIO_MIDDLE);
        circle.move(parent.getWidth() / 2, parent.getHeight() / 2);
        return circle;
    }

    public static Circle createSmall(ViewGroup parent) {
        Circle circle = createCircle(parent, RADIUS_RATIO_SMALL);
        circle.move(parent.getWidth() / 2, parent.getHeight() / 2);
        return circle;
    }

    public Circle(Context context) {
        super(context);
        paint = new Paint();
    }

    public void move(float x, float y) {
        setTranslationX(x - radius);
        setTranslationY(y - radius);
    }

    public float getRadius() {
        return radius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(color);
        final float w = getWidth();
        final float h = getHeight();
        canvas.drawCircle(w * 0.5f, h * 0.5f, radius, paint);
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    public static class CircleFrameLayout extends FrameLayout {

        Circle circle;

        public CircleFrameLayout(Context context) {
            super(context);
        }

        void setCircle(Circle circle) {
            addView(circle);
            this.circle = circle;
        }

        public void move(float x, float y) {
            setTranslationX(x - circle.radius);
            setTranslationY(y - circle.radius);
        }

        public void setColor(int color) {
            circle.setColor(color);
        }

        public Circle getCircle() {
            return circle;
        }
    }
}
