package com.tabio.tabioapp.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;

import java.io.File;

/**
 * Created by san on 3/4/16.
 */
public class CameraUtils {

    public static Uri showPictureChooser(int requestCode, Context c, String imageName) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);

        String time = BuildConfig.DEBUG ? String.valueOf(System.currentTimeMillis()) : "";
        String fileName = imageName + "_" + time + "_.jpg";
        File capturedFile = new File(AppController.getInstance().getAppMediaDirectory(), fileName);
        Uri pictureUri = Uri.fromFile(capturedFile);

        Intent i2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i2.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);

        Intent chooserView = Intent.createChooser(i, "");
        chooserView.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { i2 });
        ((AppCompatActivity)c).startActivityForResult(chooserView, requestCode);
        return pictureUri;
    }

    public static boolean allowUseCamera(Context context) {
        return allowUseStorage(context) && ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean allowUseStorage(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static int getCameraPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
    }
}
