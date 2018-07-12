package com.tabio.tabioapp.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/21/15.
 */
public class FontCache {
    private static final String TAG = makeLogTag(FontCache.class);

    private static HashMap<String, Typeface> fontCache = new HashMap<>();

    public static Typeface getTypeface(String fontname, Context context) {
        Typeface typeface = fontCache.get(fontname);

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), fontname);
            } catch (Exception e) {
                return null;
            }

            fontCache.put(fontname, typeface);
        }

        return typeface;
    }
}
