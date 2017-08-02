package com.ds05.launcher.service;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.common.Constants;
import com.ds05.launcher.common.UploadFileTask;
import com.ds05.launcher.common.utils.AppUtil;
import com.ds05.launcher.common.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CramerThread extends Thread {
    private MediaRecorder mediarecorder;// 录制视频的类private long
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceview;// 显示视频的控件
    private Camera mCamera;
    private long recordTime;  
    private long startTime = Long.MIN_VALUE;  
    private long endTime = Long.MIN_VALUE;  
    private HashMap<String, String> map = new HashMap<String, String>();
    private static final String TAG = "SEDs508EG";  
    public static final int MEDIA_TYPE_IMAGE = 1;  
    public static final int MEDIA_TYPE_VIDEO = 2;
    private File f = null;
    private String mFilePath;
    private Context mContext;
    public static boolean isRecording = false;

    public CramerThread(Context context, long recordTime, SurfaceView surfaceview,
                        SurfaceHolder surfaceHolder) {
        mContext = context;
        this.recordTime = recordTime;  
        this.surfaceview = surfaceview;  
        this.surfaceHolder = surfaceHolder;  
    }  
  
    @Override  
    public void run() {  
        /** * 开始录像 */  
        startRecord();  
        /** * 启动定时器，到规定时间recordTime后执行停止录像任务 */  
//        Timer timer = new Timer();
//        timer.schedule(new TimerThread(), recordTime);
  
    }
  
    /** * 开始录像 */  
    public void startRecord() {  
//        mediarecorder = new MediaRecorder();// 创建mediarecorder对象
//        mCamera = Utils.getCameraInstance(0);; // 解锁camera
//        mCamera.unlock();
//        mediarecorder.setCamera(mCamera); // 设置录制视频源为Camera(相机)
//        mediarecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//        mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 设置录制文件质量，格式，分辨率之类，这个全部包括了
////        mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
//        mediarecorder.setPreviewDisplay(surfaceHolder.getSurface()); // 设置视频文件输出的路径
//        // mediarecorder.setOutputFile("/sdcard/sForm.3gp");

        mediarecorder = new MediaRecorder();// 创建mediarecorder对象
        mediarecorder.reset();
        // 设置录制视频源为Camera(相机)
        mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置录制的视频编码h263 h264
        mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mediarecorder.setVideoSize(640, 480);
        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
        mediarecorder.setVideoFrameRate(15);
        mediarecorder.setMaxDuration(10000);
        mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());

        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.ALARM_VIDEO_PATH;
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        mFilePath = dirPath + AppUtil.getVideoFileName();
        f = new File(mFilePath);
        Log.d("ZXH","###f = " + f.getAbsolutePath());
        mediarecorder.setOutputFile(f.getAbsolutePath());

        mediarecorder.setOnInfoListener(new MediaRecorder.OnInfoListener(){
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Log.d("ZXH","###MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
                    stopRecord();
                }
            }
        });

        try { // 准备录制  
            mediarecorder.prepare(); // 开始录制  
            mediarecorder.start();
            isRecording = true;
            // time.setVisibility(View.VISIBLE);// 设置录制时间显示  
        } catch (IllegalStateException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
    }
  
    /** * 停止录制 */  
    public void stopRecord() {  
        if (mediarecorder != null) {
            // 停止录制
            mediarecorder.stop();
            // 释放资源
            mediarecorder.release();
            mediarecorder = null;
        }
        isRecording = false;
        if(ConnectUtils.NETWORK_IS_OK  && ConnectUtils.CONNECT_SERVER_STATUS){
            UploadFileTask uploadFileTask=new UploadFileTask(mContext);
            uploadFileTask.execute(mFilePath, UploadFileTask.VIDEOTYPE);
//                                        Intent  regIntent = new Intent(context, CameraService.class);
//                                        regIntent.putExtra("FILENAME", ImageUtil.getFileNameNoEx(picPath.substring(picPath.lastIndexOf("/")+1))+".png");
//                                        regIntent.setAction(BROADCAST_NOTIFY_HUMAN_MONITORING);
//                                        startWakefulService(context, regIntent);
        }else{
            Toast.makeText(mContext, "完成录像,网络不可用,不进行网络发送。", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Log.d("ZXH","###stopRecord  f = " + f.getAbsolutePath());
        Uri uri = Uri.fromFile(f);
        intent.setData(uri);
        mContext.sendBroadcast(intent);
    }  
  
  
    /** * 定时器 * @author bcaiw * */  
    public class TimerThread extends TimerTask {
        /** * 停止录像 */  
        @Override  
        public void run() {  
            try {  
                stopRecord();  
                this.cancel();  
            } catch (Exception e) {
                Log.d("ZXH","###e = " + e);
            }  
  
        }  
    }
}