package com.tabio.tabioapp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import jp.mixi.compatibility.android.media.ExifInterfaceCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by pixie3 on 12/7/15.
 */
final public class ImageUtils {
    public static final String TAG = makeLogTag(ImageUtils.class);

    public static String convertHttpsUrl(String url) {
        if (url != null && !url.isEmpty()) {
            url = url.replace("http://", "https://");
        }
        return url;
    }

    @Nullable
    public static Bitmap getBitmapFromAssets(Context context, String path) {
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open(path);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    public static String getBase64FromBitmap(Bitmap bitmap) {
//        if (bitmap == null) {
//            throw new RuntimeException("bitmap is null");
//        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }
}
