package com.ds05.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ds05.launcher.common.Constants;
import com.ichano.MediaManagerService;
import com.ichano.MediaSurfaceView;
import com.ichano.rvs.streamer.constant.JpegType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by kabru on 2017/6/4.
 */

public class CameraActivity_ZY extends Activity {

    public static CameraActivity_ZY instance = null;
    private MediaSurfaceView mMediaSurfaceView;
    boolean isFirst = true;
    private static final int VISITOR_RING_TIME = 20000;
    private static final int CAPTURE_TIME = 2000;
    private Handler handler = new Handler();
    private boolean mNeedCapture = false;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_by_zy);
        mContext = this;
        Intent intent=getIntent();
        mNeedCapture = intent.getBooleanExtra(Constants.EXTRA_CAPTURE,false);
        instance = this;
        mMediaSurfaceView = (MediaSurfaceView) findViewById(R.id.cameraView);
        mMediaSurfaceView.openCamera(Configuration.ORIENTATION_LANDSCAPE);

        final int[] size = mMediaSurfaceView.getVideoSize();
        handler.postDelayed(finish, VISITOR_RING_TIME);

        if(mNeedCapture){
            handler.postDelayed(capture, CAPTURE_TIME);
        }

//        ViewTreeObserver viewTreeObserver = mMediaSurfaceView.getViewTreeObserver();
//
//        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                if (isFirst) {
//                    int height = mMediaSurfaceView.getMeasuredHeight();
//                    int width = mMediaSurfaceView.getMeasuredWidth();
//                    float r = (float) height / (float) width;
//                    float r2 = (float) size[1] / (float) size[0];
//                    RelativeLayout.LayoutParams pvLayout = (RelativeLayout.LayoutParams) mMediaSurfaceView.getLayoutParams();
//                    if (r > r2) {
//                        pvLayout.height = (int) (width * r2);
//                    } else {
//                        pvLayout.width = (int) (height / r2);
//                    }
//                    isFirst = false;
//                }
//                return true;
//            }
//        });
    }


    // 使用系统当前日期加以调整作为照片的名称
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat( "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    public boolean saveMyBitmap(File f, Bitmap bmp) throws IOException {
        if (f.exists()) {
            f.delete();
        }
        boolean flag = false;
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            flag = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        fOut.flush();
        fOut.close();
        return flag;
    }

    private Runnable capture = new Runnable() {
        @Override
        public void run() {
            try {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(getApplicationContext(), "SD不存在，图片保存失败", Toast.LENGTH_SHORT).show();
                }else{
                    Bitmap mBitmap = mMediaSurfaceView.capture(JpegType.NORMAL);
                    Log.d("ZXH","########## mBitmap = " + mBitmap);
                    if(mBitmap != null){
                        File f = new File(Environment.getExternalStorageDirectory() , getPhotoFileName());
                        boolean ret = saveMyBitmap(f,mBitmap);
                        if(ret){
                            Log.d("ZXH","##########sendBroadcast ACTION_MEDIA_SCANNER_SCAN_FILE");
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri uri = Uri.fromFile(f);
                            intent.setData(uri);
                            mContext.sendBroadcast(intent);
                        }
                    }else{
                        handler.postDelayed(capture, CAPTURE_TIME);
                    }
                }
            } catch (Exception e) {

            }
        }
    };

    private Runnable finish = new Runnable() {
        @Override
        public void run() {
            try {
                if(!MediaManagerService.hasConnected){
                    finish();
                }
            } catch (Exception e) {

            }
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        handler.removeCallbacks(finish);
        handler.removeCallbacks(capture);
        mMediaSurfaceView.closeCamera();
    }

}
