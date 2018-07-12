package com.tabio.tabioapp.piece;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;

/**
 * Rectangle.
 */
public class Rectangle extends View {

    private int color;

    private Paint paint;

    public static Rectangle createRectangle(ViewGroup parent) {
        Rectangle rectangle = new Rectangle(parent.getContext());
        parent.addView(rectangle);
        rectangle.getLayoutParams().width = parent.getWidth();
        rectangle.getLayoutParams().height = parent.getHeight();
        return rectangle;
    }

    public Rectangle(Context context) {
        super(context);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(color);
    }

    public void setColor(int color) {
        this.color = color;
    }
}
