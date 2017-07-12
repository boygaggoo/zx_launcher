package com.ds05.launcher.common;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.ds05.launcher.common.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 */

public final class CameraFeature {
    private static final String TAG = "DS05";

    private static final int CAPTURE_COUNTER = 3;
    private static final int SHORT_RECORD_TIME = 10 * 1000;
    private static final int VIDEO_FRAME_RATE = 20;

    public enum CameraId {
        Front,
        Back
    }

    private android.hardware.Camera mCamera;
    private CameraId mCamId;
    private SurfaceHolder mSurfaceHodler;
    private int mPreviewWidth, mPreviewHeight;

    private MediaRecorder mMediaRecorder;

    public CameraFeature(CameraId id) {
        mCamId = id;
    }

    public void setPreviewSurface(SurfaceHolder holder) {
        mSurfaceHodler = holder;
    }
    public void setPreviewSize(int w, int h) {
        mPreviewWidth = w;
        mPreviewHeight = h;
    }

    public void startPreview() {
        configCamera();
        if(mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void stopPreview() {
        if(mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void continuousShooting() {
        if(mCamera == null) return;
        if(isCaptureContinue()) {
            mHandler.sendEmptyMessageDelayed(EVT_CAPTURE_REQUEST, 200);
        }
    }
    private int mCaptureCount = 0;
    private boolean isCaptureContinue() {
        if(mCaptureCount < CAPTURE_COUNTER) {
            mCaptureCount++;
            return true;
        }

        mCaptureCount = 0;
        return false;
    }

    public void shortRecord() {
        if(mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoSize(mPreviewWidth, mPreviewHeight);
        mMediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE);
        mMediaRecorder.setVideoEncodingBitRate(mPreviewWidth * mPreviewHeight * VIDEO_FRAME_RATE);

        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioChannels(2);
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioEncodingBitRate(32000);

        mMediaRecorder.setMaxDuration(SHORT_RECORD_TIME);

        //TODO: 警报， 抓拍， 门铃
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS");
        Date date = new Date(System.currentTimeMillis());
        File dir = new File("/sdcard/DS05/Camera/Capture");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        String videoFile = "/sdcard/DS05/Camera/Capture/Capture-" + sdf.format(date) + ".mp4";

        mMediaRecorder.setPreviewDisplay(mSurfaceHodler.getSurface());
        mMediaRecorder.setOutputFile(videoFile);

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            Log.e(TAG, "{CameraFeature}{shortRecord}ERROR:" + e.getMessage());
        }
    }

    public void keepRecord() {
        //TODO: 持续录像。收到通知。开启录像， 之后监听通知停止录像。
        if(mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoSize(mPreviewWidth, mPreviewHeight);
        mMediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE);
        mMediaRecorder.setVideoEncodingBitRate(mPreviewWidth * mPreviewHeight * VIDEO_FRAME_RATE);

        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioChannels(2);
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioEncodingBitRate(32000);

        //TODO: 警报， 抓拍， 门铃
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS");
        Date date = new Date(System.currentTimeMillis());
        File dir = new File("/sdcard/DS05/Camera/Capture");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        String videoFile = "/sdcard/DS05/Camera/Capture/Capture-" + sdf.format(date) + ".mp4";

        mMediaRecorder.setPreviewDisplay(mSurfaceHodler.getSurface());
        mMediaRecorder.setOutputFile(videoFile);

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            Log.e(TAG, "{CameraFeature}{shortRecord}ERROR:" + e.getMessage());
        }
    }

    public void stopRecording() {
        if(mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void configCamera() {
        if(mSurfaceHodler == null) throw new IllegalArgumentException("Surface is invalide");
//        if(mPreviewHeight <= 0 || mPreviewWidth <= 0)
//            throw new IllegalArgumentException("Surface is invalide");

        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        mCamera = Camera.open(mCamId.ordinal());
        if(mCamera == null) {
            Log.e(TAG, "{CameraFeature}{configCamera}Open camera fail. mCamId:" + mCamId);
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        if(mPreviewHeight <= 0 || mPreviewWidth <= 0) {
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            if(sizes == null || sizes.size() == 0)
                throw new IllegalArgumentException("Not found support preview size");

            int len = sizes.size();
            mPreviewWidth = sizes.get(0).width;
            mPreviewHeight = sizes.get(0).height;
            if(len > 1) {
                for (int i = 0; i < len; i++) {
                    if(mPreviewWidth * mPreviewHeight <
                            sizes.get(i).width * sizes.get(i).height) {
                        mPreviewWidth = sizes.get(i).width;
                        mPreviewHeight = sizes.get(i).height;
                    }
                }
            }//if(len > 1)
        }
        parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
//        parameters.setPictureFormat(ImageFormat.JPEG);
//        parameters.setPictureSize(mPreviewWidth, mPreviewHeight);
        parameters.setPreviewFormat(ImageFormat.NV21);
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        mCamera.setParameters(parameters);
        mCamera.setPreviewCallback(mPreviewCallback);

        try {
            mCamera.setPreviewDisplay(mSurfaceHodler);
        } catch (IOException e) {
            Log.e(TAG, "{CameraFeature}{configCamera}ERROR:" + e.getMessage());
        }
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mCaptureLock.lock();
            try {
                if (mCaptureRequest) {
                    mCaptureRequest = false;

                    new CaptureThread(data, mPreviewWidth, mPreviewHeight).start();
                }
            } finally {
                mCaptureLock.unlock();
            }
        }
    };


    private ReentrantLock mCaptureLock = new ReentrantLock();
    private boolean mCaptureRequest = false;
    private static final int EVT_CAPTURE_REQUEST = 0;
    private static final int EVT_SCAN_FILE = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVT_CAPTURE_REQUEST:
                    mCaptureLock.lock();
                    mCaptureRequest = true;
                    mCaptureLock.unlock();
                    if(isCaptureContinue()) {
                        mHandler.sendEmptyMessageDelayed(EVT_CAPTURE_REQUEST, 500);
                    }
                    break;
                case EVT_SCAN_FILE:
                    Utils.scanFile((String)msg.obj);
                    break;
            }
        }
    };

    public class CaptureThread extends Thread {
        byte[] mYUVData;
        int mWidth, mHeight;
        public CaptureThread(byte[] data, int w, int h) {
            mYUVData = data;
            mWidth = w;
            mHeight = h;
        }

        @Override
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS");
            Date date = new Date(System.currentTimeMillis());
            File dir = new File("/sdcard/DS05/Camera/Capture");
            if(!dir.exists()) {
                dir.mkdirs();
            }
            String picName = "/sdcard/DS05/Camera/Capture/Capture-" + sdf.format(date) + ".jpg";

            YuvImage yuvImage = new YuvImage(mYUVData, ImageFormat.NV21, mWidth, mHeight, null);
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(new File(picName));
                Rect rect = new Rect(0, 0, mWidth, mHeight);
                yuvImage.compressToJpeg(rect, 100, outputStream);
                Message msg = new Message();
                msg.what = EVT_SCAN_FILE;
                msg.obj = picName;
                mHandler.sendMessageDelayed(msg, 100);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "{CameraFeature}{CaptureThread}ERROR:" + e.getMessage());
            } finally {
                if(outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) { }
                }
            }
        }//run
    }//CaptureThread
}
