package com.ds05.launcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.ds05.launcher.common.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ds05.launcher.common.Constants.mRecording;
import static com.ds05.launcher.service.VideoService.RECORD_RESULT_ALREADY_RECORDING;
import static com.ds05.launcher.service.VideoService.RECORD_RESULT_GET_CAMERA_FAILED;
import static com.ds05.launcher.service.VideoService.RESULT_RECEIVER;

/**
 * Created by Vincent on 2017/6/23.
 */

public class PictureService  extends Service {

    private static final String TAG = PictureService.class.getSimpleName();


    private static final String START_SERVICE_COMMAND = "startServiceCommands";
    private static final int COMMAND_NONE = -1;
    private static final int COMMAND_START_CAPTURE = 0;
    private static final int COMMAND_STOP_CAPTURE = 1;
    private static final String SELECTED_CAMERA_FOR_CAPTURE = "cameraForCapture";

    private Camera mCamera;

    public PictureService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPicturePaths=new ArrayList<String>();
        if (intent == null) {
            throw new IllegalStateException("Must start the service with intent");
        }
        switch (intent.getIntExtra(START_SERVICE_COMMAND, COMMAND_NONE)) {
            case COMMAND_START_CAPTURE:
                handleStartCaptureCommand(intent);
                break;
            case COMMAND_STOP_CAPTURE:
                handleStopCaptureCommand(intent);
                break;
            default:
                throw new UnsupportedOperationException("Cannot start service with illegal commands");
        }

        return START_NOT_STICKY;
    }

    private void handleStopCaptureCommand(Intent intent) {
    }

    private void handleStartCaptureCommand(Intent intent) {
        Log.d(TAG,"*******************************************************handleStartCaptureCommand*********************************************************");
        if (!Utils.isCameraExist(this)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }

        final ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);
        if (mRecording) {
            resultReceiver.send(RECORD_RESULT_ALREADY_RECORDING, null);
            return;
        }
        mRecording = true;

        final int cameraId = intent.getIntExtra(SELECTED_CAMERA_FOR_CAPTURE, Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera = Utils.getCameraInstance(cameraId);
        if (mCamera != null) {
            SurfaceView sv = new SurfaceView(this);
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,  PixelFormat.TRANSLUCENT);
            SurfaceHolder sh = sv.getHolder();
            sv.setZOrderOnTop(true);
            sh.setFormat(PixelFormat.TRANSPARENT);
            sh.addCallback(new SurfaceHolder.Callback() {
                       @Override
                       public void surfaceCreated(SurfaceHolder holder) {
                           Camera.Parameters params = mCamera.getParameters();
                           mCamera.setParameters(params);
                           Camera.Parameters p = mCamera.getParameters();
                           List<Camera.Size> listSize;
                           listSize = p.getSupportedPreviewSizes();
                           Camera.Size mPreviewSize = listSize.get(2);
                          // Log.v(TAG, "preview width = " + mPreviewSize.width + " preview height = " + mPreviewSize.height);
                           p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                           listSize = p.getSupportedPictureSizes();
                           Camera.Size mPictureSize = listSize.get(2);
                          // Log.v(TAG, "capture width = " + mPictureSize.width  + " capture height = " + mPictureSize.height);
                           p.setPictureSize(mPictureSize.width, mPictureSize.height);
                           mCamera.setParameters(p);
                           try {
                               mCamera.setPreviewDisplay(holder);
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                           mCamera.startPreview();
                        //

                           mCamera.takePicture(null, null, mPictureCallback);
                           mCamera.unlock();
                       }

                       @Override
                       public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                       }

                       @Override
                       public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                       }
                   }
            );
            wm.addView(sv, params);
        }else {
            Log.d(TAG, "Get Camera from service failed");
            resultReceiver.send(RECORD_RESULT_GET_CAMERA_FAILED, null);
        }

    }

    public static void startToStartCapture(Context context, int cameraId, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, PictureService.class);
        intent.putExtra(START_SERVICE_COMMAND, COMMAND_START_CAPTURE);
        intent.putExtra(SELECTED_CAMERA_FOR_CAPTURE, cameraId);
        intent.putExtra(RESULT_RECEIVER, resultReceiver);
        context.startService(intent);

    }

    public static void startToStopCapture(Context context, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, PictureService.class);
        intent.putExtra(START_SERVICE_COMMAND, COMMAND_STOP_CAPTURE);
        intent.putExtra(RESULT_RECEIVER, resultReceiver);
        context.startService(intent);
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile =  new File(Environment.getExternalStorageDirectory() , getPhotoFileName());
            if (pictureFile == null) {
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                camera.startPreview();//图片保存完之后重新预览
            } catch (FileNotFoundException e) {
                Log.d("MotionDetector", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("MotionDetector", "Error accessing file: " + e.getMessage());
            }
        }
    };






    private String mPath;
    private int mPictureCount=3;//默认自动拍一张
    private final int mPictureCountMax=30;//默认自动拍一张
    int pic_count=0;
    private ArrayList<String> mPicturePaths;
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG,"*******************************************************onPictureTaken*********************************************************");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                FileOutputStream fileOutputStream = null;
                try {
                    Log.d(TAG,"图片保存路径："+Environment.getExternalStorageDirectory());

                    File file = new File(Environment.getExternalStorageDirectory() , getPhotoFileName());
                    mPath = file.getAbsolutePath();
                    Log.d(TAG, "onPictureTaken: " + mPath);
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(data);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "图片保存失败", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"图片保存失败");
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            if (mPictureCount>mPictureCountMax){
                                Toast.makeText(getApplicationContext(),"为了节约内存，连拍张数不要超过"+mPictureCountMax+"张", Toast.LENGTH_SHORT).show();
                            }else {
                                if (++pic_count<mPictureCount){
                                    //连拍三张
                                    mPicturePaths.add(mPath);

                                    PictureService.startToStopCapture(getApplicationContext(),  new ResultReceiver(new Handler()) {
                                        @Override
                                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                                        }
                                    });


                                    PictureService.startToStartCapture(getApplicationContext(),  Camera.CameraInfo.CAMERA_FACING_FRONT, new ResultReceiver(new Handler()) {
                                        @Override
                                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                                        }
                                    });
                                }else {

                                    mPicturePaths.add(mPath);//最后一张图片加入集合
                                    //Intent intent = new Intent().putStringArrayListExtra(Contast.PICTURE_PATHS, mPicturePaths);
                                    //setResult(RESULT_OK,intent);
                                    fileOutputStream.close();
                                    //保证最后一张图片加入集合并优化用户体验
                                    SystemClock.sleep(2000);
                                    // finish();
                                }
                                Toast.makeText(getApplicationContext(), "图片保存成功"+pic_count+"张", Toast.LENGTH_SHORT).show();
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else {
                Toast.makeText(getApplicationContext(), "SD不存在，图片保存失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"SD不存在，图片保存失败");
            }
        }
    };



    // 使用系统当前日期加以调整作为照片的名称
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat( "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

}
