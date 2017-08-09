package com.ds05.launcher.ui.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.ds05.launcher.CameraActivity_ZY;
import com.ds05.launcher.R;
import com.ds05.launcher.ModuleBaseActivity;
import com.ds05.launcher.common.Constants;

/**
 * Created by Chongyang.Hu on 2017/1/1 0001.
 */

public class MonitorActivity extends ModuleBaseActivity {

    @Override
    protected void onInit(Bundle savedInstanceState) {
        showTitleBar();
        setTitle(R.string.string_monitor);

        replaceFragment(new MonitorFragment());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("ZXH","######### keyCode = " + keyCode);
        if(keyCode == Constants.HOME_KEY){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }else if(keyCode ==  Constants.CAPTURE_KEY){
            Intent intent = new Intent(this, CameraActivity_ZY.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
