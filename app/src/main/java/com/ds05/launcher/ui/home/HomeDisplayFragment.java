package com.ds05.launcher.ui.home;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

import com.ds05.launcher.R;
import com.ds05.launcher.common.LunarCalendar;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class HomeDisplayFragment extends Fragment {
    private TextClock mTextColock;
    private TextView mDateView;
    private LunarCalendar mLunar;

    private Activity mAct;


    @Override
    public void onResume() {
        super.onResume();
        if(mAct == null) mAct = getActivity();
        registerReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d("DS05", "HomeDisplayFragment  onCreateView");
        View rootView = inflater.inflate(R.layout.home_display_fragment, container, false);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        Log.d("DS05", "HomeDisplayFragment  onDestroyView");
        FragmentManager fragmentManager = getFragmentManager();
        InformationFragment informationFragment = (InformationFragment)fragmentManager.findFragmentById(R.id.id_info_frag);
        if(informationFragment != null && fragmentManager != null && !fragmentManager.isDestroyed()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(informationFragment).commit();
        }
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLunar = new LunarCalendar();
        mTextColock = (TextClock) view.findViewById(R.id.id_time_view);
        mDateView = (TextView) view.findViewById(R.id.id_date_view);
/*
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
*/
        if(mAct == null) mAct = getActivity();

        updateDate();
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver();
    }

    private void registerReceiver() {
        IntentFilter fillter = new IntentFilter();
        fillter.addAction(Intent.ACTION_TIME_TICK);

        mAct.registerReceiver(mReceiver, fillter);
    }

    private void unregisterReceiver() {
        mAct.unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_TIME_TICK)) {
                Log.d("DS05", "ACTION_TIME_TICK");
                updateDate();
            }
        }
    };

    private synchronized void updateDate() {
        new Thread() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                //calendar.setTimeZone();
                final int year = calendar.get(calendar.YEAR);
                final int month = calendar.get(calendar.MONTH) + 1;
                final int day = calendar.get(calendar.DAY_OF_MONTH);

                mLunar.setGregorian(year, month, day);
                mLunar.computeChineseFields();
                mLunar.computeSolarTerms();

                final StringBuilder sb = new StringBuilder();
                sb.append(year);
                sb.append("-");
                if(month < 10) {
                    sb.append("0" + month);
                } else {
                    sb.append(month);
                }
                sb.append("-");
                if(day < 10) {
                    sb.append("0" + day);
                } else {
                    sb.append(day);
                }

                String[] weeks = getResources().getStringArray(R.array.array_week_string);
                int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                sb.append(" ");
                sb.append(weeks[week]);
                sb.append(" ");

                sb.append(mLunar.getChineseMonth(year, month, day));
                sb.append(mLunar.getChineseDay(year, month, day));

                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDateView.setText(sb.toString());
                    }
                });
            }
        }.start();
    }

}
