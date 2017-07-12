package com.ds05.launcher;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.ichano.rvs.streamer.ui.MediaSurfaceView;

/**
 * Created by kabru on 2017/6/4.
 */

public class CameraActivity_ZY extends Activity {

    private MediaSurfaceView mMediaSurfaceView;
    boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_by_zy);
        mMediaSurfaceView = (MediaSurfaceView) findViewById(R.id.cameraView);
        mMediaSurfaceView.openCamera(Configuration.ORIENTATION_LANDSCAPE);
        final int[] size = mMediaSurfaceView.getVideoSize();

        mMediaSurfaceView.flip();

        ViewTreeObserver viewTreeObserver = mMediaSurfaceView.getViewTreeObserver();

        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (isFirst) {
                    int height = mMediaSurfaceView.getMeasuredHeight();
                    int width = mMediaSurfaceView.getMeasuredWidth();
                    float r = (float) height / (float) width;
                    float r2 = (float) size[1] / (float) size[0];
                    RelativeLayout.LayoutParams pvLayout = (RelativeLayout.LayoutParams) mMediaSurfaceView.getLayoutParams();
                    if (r > r2) {
                        pvLayout.height = (int) (width * r2);
                    } else {
                        pvLayout.width = (int) (height / r2);
                    }
                    isFirst = false;
                }
                return true;
            }
        });
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mMediaSurfaceView.closeCamera();
    }

}
