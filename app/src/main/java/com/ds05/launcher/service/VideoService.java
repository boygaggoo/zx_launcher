package com.ds05.launcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.ds05.launcher.common.utils.Utils;

/**
 * Created by kabru on 2017/6/17.
 */

public class VideoService extends Service {

    private static final String TAG = VideoService.class.getSimpleName();

    public static final String RESULT_RECEIVER = "resultReceiver";
    public static final String VIDEO_PATH = "recordedVideoPath";

    public static final int RECORD_RESULT_OK = 0;
    public static final int RECORD_RESULT_DEVICE_NO_CAMERA= 1;
    public static final int RECORD_RESULT_GET_CAMERA_FAILED = 2;
    public static final int RECORD_RESULT_ALREADY_RECORDING = 3;
    public static final int RECORD_RESULT_NOT_RECORDING = 4;
    public static final int RECORD_RESULT_UNSTOPPABLE = 5;

    private static final String START_SERVICE_COMMAND = "startServiceCommands";
    private static final int COMMAND_NONE = -1;
    private static final int COMMAND_START_RECORDING = 0;
    private static final int COMMAND_STOP_RECORDING = 1;

    private static final String SELECTED_CAMERA_FOR_RECORDING = "cameraForRecording";

    private String mRecordingPath = null;

    public VideoService() {
    }

    public static boolean getRecordStatus(){
        return CramerThread.isRecording;
    }

    public static void startToStartRecording(Context context, int cameraId, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, VideoService.class);
        intent.putExtra(START_SERVICE_COMMAND, COMMAND_START_RECORDING);
        intent.putExtra(SELECTED_CAMERA_FOR_RECORDING, cameraId);
        intent.putExtra(RESULT_RECEIVER, resultReceiver);
        context.startService(intent);
    }

    public static void startToStopRecording(Context context, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, VideoService.class);
        intent.putExtra(START_SERVICE_COMMAND, COMMAND_STOP_RECORDING);
        intent.putExtra(RESULT_RECEIVER, resultReceiver);
        context.startService(intent);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            throw new IllegalStateException("Must start the service with intent");
        }
        switch (intent.getIntExtra(START_SERVICE_COMMAND, COMMAND_NONE)) {
            case COMMAND_START_RECORDING:
                handleStartRecordingCommand(intent);
                break;
            case COMMAND_STOP_RECORDING:
                handleStopRecordingCommand(intent);
                break;
            default:
                throw new UnsupportedOperationException("Cannot start service with illegal commands");
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wm != null){
            wm.removeView(relLay);
            wm = null;
        }
        if(thread != null && CramerThread.isRecording){
            thread.stopRecord();
        }
    }

    SurfaceView surfaceview;
    WindowManager wm;
    SurfaceHolder surfaceHolder; // //和surfaceView相关的
    LinearLayout relLay;
    CramerThread thread;

    private void handleStartRecordingCommand(Intent intent) {
        if (!Utils.isCameraExist(this)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }

        final ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);

        if (CramerThread.isRecording) {
            resultReceiver.send(RECORD_RESULT_ALREADY_RECORDING, null);
            return;
        }

        wm = (WindowManager) getApplicationContext().getSystemService("window");
        // 2.得到WindowManager.LayoutParams对象，为后续设置相关参数做准备：
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        // 3.设置相关的窗口布局参数，要实现悬浮窗口效果，要需要设置的参数有
        // 3.1设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        // 3.2设置图片格式，效果为背景透明 //wmParams.format = PixelFormat.RGBA_8888;
        wmParams.format = 1;
        // 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 4.// 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 5. 调整悬浮窗口至中间
        wmParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER;
        // 6. 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;
        // 7.将需要加到悬浮窗口中的View加入到窗口中了：
        // 如果view没有被加入到某个父组件中，则加入WindowManager中
        surfaceview = new SurfaceView(this);
        surfaceHolder = surfaceview.getHolder();
        WindowManager.LayoutParams params_sur = new WindowManager.LayoutParams();
        params_sur.width = 1;
        params_sur.height = 1;
        params_sur.alpha = 255;
        surfaceview.setLayoutParams(params_sur);
        surfaceview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("ZXH","###surfaceCreated holder= " + holder);
                surfaceHolder = holder;
                // //录像线程，当然也可以在别的地方启动，但是一定要在onCreate方法执行完成以及surfaceHolder被赋值以后启动
                if(thread == null){
                    thread = new CramerThread(VideoService.this, 10000, surfaceview, surfaceHolder);// 设置录制时间为10秒
                }
                thread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("ZXH","###surfaceChanged holder= " + holder);
                surfaceHolder = holder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("ZXH","###surfaceDestroyed");
                surfaceview = null;
                surfaceHolder = null;
            }
        });

        relLay = new LinearLayout(this);
        WindowManager.LayoutParams params_rel = new WindowManager.LayoutParams();
        params_rel.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params_rel.height = WindowManager.LayoutParams.WRAP_CONTENT;
        relLay.setLayoutParams(params_rel);
        relLay.addView(surfaceview);
        wm.addView(relLay, wmParams); // 创建View

        resultReceiver.send(RECORD_RESULT_OK, null);
        Log.d(TAG, "Recording is started");
    }


    private void handleStopRecordingCommand(Intent intent) {
        final ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);
        Log.d(TAG, "recording is finished.");
        if(thread != null){
            thread.stopRecord();
        }
        resultReceiver.send(RECORD_RESULT_OK, null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
