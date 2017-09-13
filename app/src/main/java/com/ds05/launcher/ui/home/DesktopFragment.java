package com.ds05.launcher.ui.home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.ds05.launcher.R;
import com.ds05.launcher.common.CommUtil;
import com.ds05.launcher.ui.help.HelpActivity;
import com.ds05.launcher.ui.monitor.MonitorActivity;
import com.ds05.launcher.ui.settings.SettingsActivity;

public class DesktopFragment extends BaseFragment
        implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.desktop_frag, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View settings, monitor, photo, help;
        settings = view.findViewById(R.id.id_settings);
        monitor = view.findViewById(R.id.id_monitor);
        photo = view.findViewById(R.id.id_photo);
        help = view.findViewById(R.id.id_help);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentById(R.id.fragment);
        if(fragment != null){

        }else{
            transaction.add(R.id.fragment, new HomeDisplayFragment());
            transaction.commit();
        }


//        settings.setFocusable(true);
//        settings.setFocusableInTouchMode(true);
//        settings.requestFocus();
//        monitor.setFocusable(true);
//        monitor.setFocusableInTouchMode(true);
//        photo.setFocusable(true);
//        photo.setFocusableInTouchMode(true);
//        help.setFocusable(true);
//        help.setFocusableInTouchMode(true);
//
//        settings.setNextFocusDownId(R.id.id_help);
//        settings.setNextFocusUpId(R.id.id_photo);
//        settings.setNextFocusLeftId(R.id.id_photo);
//        settings.setNextFocusRightId(R.id.id_monitor);
//        monitor.setNextFocusDownId(R.id.id_photo);
//        monitor.setNextFocusUpId(R.id.id_help);
//        monitor.setNextFocusLeftId(R.id.id_settings);
//        monitor.setNextFocusRightId(R.id.id_help);
//        photo.setNextFocusDownId(R.id.id_settings);
//        photo.setNextFocusUpId(R.id.id_monitor);
//        photo.setNextFocusLeftId(R.id.id_help);
//        photo.setNextFocusRightId(R.id.id_settings);
//        help.setNextFocusDownId(R.id.id_monitor);
//        help.setNextFocusUpId(R.id.id_settings);
//        help.setNextFocusLeftId(R.id.id_monitor);
//        help.setNextFocusRightId(R.id.id_photo);

        settings.setOnClickListener(this);
        monitor.setOnClickListener(this);
        photo.setOnClickListener(this);
        help.setOnClickListener(this);

//        settings.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) return;
//                v.setNextFocusDownId(R.id.id_help);
//                v.setNextFocusUpId(R.id.id_photo);
//                v.setNextFocusLeftId(R.id.id_photo);
//                v.setNextFocusRightId(R.id.id_monitor);
//            }
//        });
//
//        monitor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) return;
//                v.setNextFocusDownId(R.id.id_photo);
//                v.setNextFocusUpId(R.id.id_help);
//                v.setNextFocusLeftId(R.id.id_settings);
//                v.setNextFocusRightId(R.id.id_help);
//            }
//        });
//
//        photo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) return;
//                v.setNextFocusDownId(R.id.id_settings);
//                v.setNextFocusUpId(R.id.id_monitor);
//                v.setNextFocusLeftId(R.id.id_help);
//                v.setNextFocusRightId(R.id.id_settings);
//            }
//        });
//
//        help.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) return;
//                v.setNextFocusDownId(R.id.id_monitor);
//                v.setNextFocusUpId(R.id.id_settings);
//                v.setNextFocusLeftId(R.id.id_monitor);
//                v.setNextFocusRightId(R.id.id_photo);
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                break;
            case R.id.id_help:
                startActivity(new Intent(getContext(), HelpActivity.class));
//                Log.d("ZXH","##############help installApkBySilent ");
//                CommUtil.installApkBySilent();
                break;
            case R.id.id_monitor:
                startActivity(new Intent(getContext(), MonitorActivity.class));
                break;
            case R.id.id_photo:
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName( "com.yancy.photobrowser", "com.yancy.photobrowser.GalleryPicker");
                intent.setComponent(componentName);
                startActivity(intent);
                break;
        }
    }
}



































