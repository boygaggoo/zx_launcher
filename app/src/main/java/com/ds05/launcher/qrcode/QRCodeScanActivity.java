package com.ds05.launcher.qrcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.R;
import com.ds05.launcher.qrcode.camera.CameraManager;
import com.ds05.launcher.qrcode.decoding.CaptureActivityHandler;
import com.ds05.launcher.qrcode.decoding.InactivityTimer;
import com.ds05.launcher.qrcode.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

public class QRCodeScanActivity extends Activity implements Callback {

    public static final int ACT_REQUEST_CODE_GET_WIFI = 1;
    public static final String EXTRA_QRCODE_RESULT = "QRCode-Result";

    public static final String EXTRA_REQ_REASON = "REQ-REASON";
    public static final int REASON_NONE = -1;
    public static final int REASON_GET_WIFI = 0;

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode_layout);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("QRCodeScanActivity","onResume  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> onResume");

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

//        *** Debug code ***
//        Result result = new Result("TP-LINK-7C20;tplink123456789", null, null, null);
//        handleDecode(result, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(0, surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void handleDecode(final Result obj, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();

        int reqReason = getIntent().getIntExtra(EXTRA_REQ_REASON, REASON_NONE);
        switch (reqReason) {
            case REASON_GET_WIFI: {
                if(obj == null && TextUtils.isEmpty(obj.getText())) {
                    setResult(RESULT_CANCELED);
                    showDialog(obj, barcode);
                    break;
                }
                String[] wifiInfo = obj.getText().split(",");
                if (wifiInfo.length == 2 || wifiInfo.length == 3) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_QRCODE_RESULT, obj.getText());
                    setResult(RESULT_OK, intent);

                    finish();
                    break;
                } else {
                    setResult(RESULT_CANCELED);
                    showDialog(obj, barcode);
                    break;
                }
            }
            default:
                showDialog(obj, barcode);
        }
    }

    private void showDialog(final Result obj, Bitmap barcode) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        if (barcode == null) {
            dialog.setIcon(null);
        } else {
            Drawable drawable = new BitmapDrawable(barcode);
            dialog.setIcon(drawable);
        }
        dialog.setTitle(R.string.settings_str_scan_qr_code_result);
        dialog.setMessage(obj.getText());
        dialog.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Intent intent = new Intent();
//                intent.setAction("android.intent.action.VIEW");
//                Uri content_url = Uri.parse(obj.getText());
//                intent.setData(content_url);
//                startActivity(intent);
                //vincent add start
                Intent intent = new Intent();
                intent.setAction("com.ds05.Broadcast.ToServer.NOTIFY_QRCODE_RESULT");
                String [] arrs=obj.getText().split(",");
                intent.putExtra("QRCodeResult_UserId",arrs[0]);
                intent.putExtra("QRCodeResult_WifiSSID",arrs[1]);
                intent.putExtra("QRCodeResult_WifiPassword",arrs[2]);
               // Uri content_url = Uri.parse(obj.getText());
               // intent.setData(content_url);
                LauncherApplication.getContext().sendBroadcast(intent);
                //vincent add end
                finish();
            }
        });
        dialog.setPositiveButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.create().show();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}