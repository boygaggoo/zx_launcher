package com.ds05.launcher;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.ds05.launcher.ui.home.BaseFragment;
import com.ds05.launcher.ui.home.DesktopFragment;
import com.ds05.launcher.ui.settings.SettingsActivity;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getContentResolver().registerContentObserver(Uri.parse(BaseFragment.FRAG_SWITCH_AUTHORITIES), true, mObserver);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.container, new DesktopFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(mObserver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d("MainActivity", "按返回键也没什么用的，不要按了。");

    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            String uriString = uri.toString();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (uriString.equals(BaseFragment.getFragmentUri(DesktopFragment.class))) {
                ft.replace(R.id.container, new DesktopFragment());
                ft.commit();
            } else if (uriString.equals(BaseFragment.getFragmentUri(SettingsActivity.class))) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        }
    };

}
