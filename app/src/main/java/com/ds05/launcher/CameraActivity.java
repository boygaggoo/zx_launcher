package com.ds05.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ds05.launcher.common.CameraFeature;
import com.ds05.launcher.service.HWSink;

/**
 * Created by Chongyang.Hu on 2017/3/1 0001.
 */

public class CameraActivity extends Activity implements SurfaceHolder.Callback {
    private static final int PREVIEW_WIDTH = 854;
    private static final int PREVIEW_HEIGHT = 480;

    private CameraFeature mCameraFeature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        registerBroadcast();

        mCameraFeature = new CameraFeature(CameraFeature.CameraId.Front);
        mCameraFeature.setPreviewSize(0, 0);

        SurfaceView sf = (SurfaceView)findViewById(R.id.id_camera_surface);
        sf.getHolder().addCallback(this);
    }

    @Override
    protected void onDestroy() {
        //sendBroadcast(new Intent(HWSink.ACTION_STOP_CAMERA_NOTIFY_RESP));
        unregisterBroadcast();
        super.onDestroy();
    }

//    @Override
//    public void onBackPressed() {
//        //super.onBackPressed();
//    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCameraFeature.setPreviewSurface(holder);
        mCameraFeature.startPreview();
        //mHandler.sendEmptyMessageDelayed(EVT_TAKE_PICTURE, 1 * 1000);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraFeature.stopPreview();
    }

    private void registerBroadcast() {
        //IntentFilter filter = new IntentFilter();
        //filter.addAction(HWSink.ACTION_STOP_CAMERA_NOTIFY);

        //registerReceiver(mReceiver, filter);
    }
    private void unregisterBroadcast() {
        //unregisterReceiver(mReceiver);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(HWSink.ACTION_STOP_CAMERA_NOTIFY)) {
                mHandler.sendEmptyMessageDelayed(EVT_STOP_CAMERA_NOTIFY, 100);
            }
        }
    };

    private static final int EVT_STOP_CAMERA_NOTIFY = 0;
    private static final int EVT_TAKE_PICTURE = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVT_STOP_CAMERA_NOTIFY:
                    finish();
                    break;
                case EVT_TAKE_PICTURE:
                    mCameraFeature.continuousShooting();
                    break;
            }
        }
    };
}
