package com.tabio.tabioapp.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;

/**
 * Created by san on 4/1/16.
 */
public class ViewUtils {

    public static int getPixelFromDp(Context c, int dp) {
//        float scale = c.getResources().getDisplayMetrics().density;
//        int dpAsPixels = (int) (dp*scale + 0.5f);
//        return dpAsPixels;
        DisplayMetrics displayMetrics = c.getResources()
                .getDisplayMetrics();
        int px = Math.round(dp
                * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static Point getViewSize(View View){
        Point point = new Point(0, 0);
        point.set(View.getWidth(), View.getHeight());

        return point;
    }

    public static Point getDisplaySize(Activity c){
        Display display = c.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }
}
