package com.ds05.launcher.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 */

public final class Utils {

    public static boolean checkNetwork() {
        ConnectivityManager connectivity = (ConnectivityManager) LauncherApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void scanDir(final File files) {
        if(files == null || !files.exists()
                || !files.isDirectory()) {
            throw new IllegalArgumentException("{Utils}{scanDir}Invalide files:" + files.toString());
        }

        new Thread("Util-scanDir") {
            @Override
            public void run() {
                MediaScannerConnection.scanFile(
                        LauncherApplication.getContext(),
                        files.list(),
                        null, null);
            }
        };
    }
    public static void scanFile(final String path) {
        File file = new File(path);
        if(!file.exists() || file.isDirectory()) return;

        new Thread("Util-scanFile") {
            @Override
            public void run() {
                MediaScannerConnection.scanFile(
                        LauncherApplication.getContext(),
                        new String[]{path},
                        null, null);
            }
        }.start();
    }





    public static boolean isCameraExist(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCameraExist(int cameraId) {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == cameraId) {
                return true;
            }
        }
        return false;
    }

    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Log.d("TAG", "Open camera failed: " + e);
        }

        return c;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DS05");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +"IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +"VID_"+ timeStamp + ".mp4");
            try {
                mediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        return mediaFile;
    }

}
