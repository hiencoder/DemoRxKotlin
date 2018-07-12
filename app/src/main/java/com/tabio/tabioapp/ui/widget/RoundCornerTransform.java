package com.tabio.tabioapp.ui.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * Created by san on 7/20/16.
 */
public class RoundCornerTransform implements Transformation {

    private int Round;

    public RoundCornerTransform(int margin, int Round) {
        this.Round = Round;
    }

    @Override
    public String key() {
        return "Round" + Round;
    }

    @Override
    public Bitmap transform(Bitmap arg0) {
        // TODO Auto-generated method stub
        return getRoundedTopLeftCornerBitmap(arg0);
    }

    public Bitmap getRoundedTopLeftCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float Px = Round;

        final Rect bottomRect = new Rect(0, bitmap.getHeight() / 2,
                bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, Px, Px, paint);
        // Fill in upper right corner
        // canvas.drawRect(topRightRect, paint);
        // Fill in bottom corners
        canvas.drawRect(bottomRect, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if (bitmap != output) {
            bitmap.recycle();
        }
        return output;
    }
}
