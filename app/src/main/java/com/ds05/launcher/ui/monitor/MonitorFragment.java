package com.ds05.launcher.ui.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.service.HWSink;
import com.ds05.launcher.service.SoundManager;
import com.ds05.launcher.view.DialogSeekBarPreference;
import com.ds05.launcher.view.ListPreferenceExt;

/**
 * Created by Chongyang.Hu on 2017/1/1 0001.
 */

public class MonitorFragment extends ModuleBaseFragment
    implements Preference.OnPreferenceChangeListener{

    public static final String KEY_HUMAN_MONIOTOR = "key_human_monitor";
    public static final String KEY_INTELL_ALARM_TIME = "key_intelligent_alarm_time";
    public static final String KEY_ALARM_INTERVAL_TIME = "key_alarm_interval_time";
    public static final String KEY_MONITORING_SENS = "key_monitoring_sensitivity";
    public static final String KEY_ALARM_SOUND = "key_auto_alarm_sound";
    public static final String KEY_ALARM_VOLUME = "key_auto_alarm_volume";
    public static final String KEY_SHOOTING_MODE = "key_shooting_mode";

    private static final String[] KEYS = {
            KEY_INTELL_ALARM_TIME,
            KEY_MONITORING_SENS,
            KEY_SHOOTING_MODE
    };

    private SoundManager mSoundManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.monitor_fragment);
        //view.setBackgroundColor（getResources（).getColor（android.R.color.your_color））;
        mSoundManager = SoundManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.string_monitor);

        boolean humanMonitorState =PrefDataManager.getHumanMonitorState();
        if( humanMonitorState == true ){
                findPreference(KEY_INTELL_ALARM_TIME).setEnabled(true);
                findPreference(KEY_ALARM_INTERVAL_TIME).setEnabled(true);
                findPreference(KEY_MONITORING_SENS).setEnabled(true);
                findPreference(KEY_ALARM_SOUND).setEnabled(true);
                findPreference(KEY_ALARM_VOLUME).setEnabled(true);
                findPreference(KEY_SHOOTING_MODE).setEnabled(true);
        }else {
                findPreference(KEY_INTELL_ALARM_TIME).setEnabled(false);
                findPreference(KEY_ALARM_INTERVAL_TIME).setEnabled(false);
                findPreference(KEY_MONITORING_SENS).setEnabled(false);
                findPreference(KEY_ALARM_SOUND).setEnabled(false);
                findPreference(KEY_ALARM_VOLUME).setEnabled(false);
                findPreference(KEY_SHOOTING_MODE).setEnabled(false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findPreference(KEY_ALARM_INTERVAL_TIME).setOnPreferenceChangeListener(this);
        findPreference(KEY_HUMAN_MONIOTOR).setOnPreferenceChangeListener(this);
        findPreference(KEY_INTELL_ALARM_TIME).setOnPreferenceChangeListener(this);
        Preference preference;
        for(int i = 0; i < KEYS.length; i++) {
            preference = findPreference(KEYS[i]);
            preference.setOnPreferenceChangeListener(this);
            if(preference instanceof ListPreference) {
                ListPreference listPreference =  (ListPreference)preference;
                preference.setSummary(listPreference
                        .getEntries()[Integer.parseInt(listPreference.getValue())]);
            }
        }

        final ListPreferenceExt soundPref = (ListPreferenceExt)findPreference(KEY_ALARM_SOUND);
        soundPref.setOnPreferenceChangeListener(this);
        soundPref.setSummary(soundPref.getEntries()[Integer.parseInt(soundPref.getValue())]);
        soundPref.setOnListItemClickListener(new ListPreferenceExt.OnListItemClickListener() {
            @Override
            public void onItemClick(int index) {
                if(index == 0) return;

                mSoundManager.testAlarmSound(index - 1, mSoundManager.getAlarmSoundVolume());
            }
        });

        DialogSeekBarPreference volumePref = (DialogSeekBarPreference)findPreference(KEY_ALARM_VOLUME);
        volumePref.setOnPreferenceChangeListener(this);
        volumePref.setProgressChangedListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int index = Integer.parseInt(soundPref.getValue());
                if(index == 0) return;
                mSoundManager.testAlarmSound(index - 1, seekBar.getProgress() / 10f);
            }
        });
        volumePref.setMax(10);
        volumePref.setProgress((int)(PrefDataManager.getAlarmSoundVolume() * 10));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if(key.equals(KEY_ALARM_SOUND)) {
            ListPreferenceExt pref = (ListPreferenceExt)preference;
            pref.setSummary(pref.getEntries()[Integer.parseInt((String)newValue)]);
            mSoundManager.stopTestSound();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSoundManager.updateAlarmConfig();
                }
            }, 300);
            PrefDataManager.setAlarmSound(Integer.parseInt((String)newValue));
            Log.d("PP"," newValue_alarm_sound = " + newValue);
        } else if(key.equals(KEY_ALARM_VOLUME)) {
            PrefDataManager.setAlarmSoundVolume(Integer.parseInt((String)newValue) / 10f);
            mSoundManager.stopTestSound();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSoundManager.updateAlarmConfig();
                }
            }, 300);
            Log.d("PP"," newValue_alarm_volume= " + newValue);
        } else if(key.equals(KEY_HUMAN_MONIOTOR)) {
            Intent intent = new Intent();
            intent.putExtra(HWSink.EXTRA_DRV_CFG_HUMAN_MONITOR_STATE, (boolean)newValue);
            HWSink.updateDriverConfig(intent);
            if( (boolean)newValue==true ){
                findPreference(KEY_INTELL_ALARM_TIME).setEnabled(true);
                findPreference(KEY_ALARM_INTERVAL_TIME).setEnabled(true);
                findPreference(KEY_MONITORING_SENS).setEnabled(true);
                findPreference(KEY_ALARM_SOUND).setEnabled(true);
                findPreference(KEY_ALARM_VOLUME).setEnabled(true);
                findPreference(KEY_SHOOTING_MODE).setEnabled(true);
            }else {
                findPreference(KEY_INTELL_ALARM_TIME).setEnabled(false);
                findPreference(KEY_ALARM_INTERVAL_TIME).setEnabled(false);
                findPreference(KEY_MONITORING_SENS).setEnabled(false);
                findPreference(KEY_ALARM_SOUND).setEnabled(false);
                findPreference(KEY_ALARM_VOLUME).setEnabled(false);
                findPreference(KEY_SHOOTING_MODE).setEnabled(false);
            }
            PrefDataManager.setHumanMonitorState((boolean)newValue);
            Log.d("PP"," newValue_monitor " + newValue);
        } else if(key.equals(KEY_MONITORING_SENS)) {
            int index = Integer.parseInt((String)newValue);
            int val = -1;
            if(index == 0) {
                val = HWSink.MONITOR_SENSI_HIGHT;
            } else if(index == 1) {
                val = HWSink.MONITOR_SENSI_LOW;
            }
            if(val != -1) {
                int mval = 0;
                Intent intent = new Intent();
                if(val == 1){
                    mval = 100;
                }else if(val == 2){
                    mval = 50;
                }
                intent.putExtra(HWSink.EXTRA_DRV_CFG_MONITOR_SENSI, mval);
                HWSink.updateDriverConfig(intent);
            }
            PrefDataManager.setHumanMonitorSensi(val);
            Log.d("PP"," val_sens " + val);
        } else if(key.equals(KEY_INTELL_ALARM_TIME)) {
            int index = Integer.parseInt((String)newValue);
            int val = -1;
            if(index == 0) {
                val = HWSink.AUTO_ALARM_TIME_3SEC;
            } else if(index == 1) {
                val = HWSink.AUTO_ALARM_TIME_8SEC;
            } else if(index == 2) {
                val = HWSink.AUTO_ALARM_TIME_15SEC;
            } else if(index == 3) {
                val = HWSink.AUTO_ALARM_TIME_25SEC;
            }
            if(val != -1) {
                Intent intent = new Intent();
                intent.putExtra(HWSink.EXTRA_DRV_CFG_MONITOR_SENSI, val);
                HWSink.updateDriverConfig(intent);
            }
            PrefDataManager.setAutoAlarmTime(val);
            Log.d("PP"," val_alarm_time = " + val);
        }else if(key.equals(KEY_ALARM_INTERVAL_TIME)){
            int index = Integer.parseInt((String)newValue);
            int val = -1;
            if(index == 0) {
                val = HWSink.ALARM_INTERVAL_TIME_30SEC;
            } else if(index == 1) {
                val = HWSink.ALARM_INTERVAL_TIME_90SEC;
            } else if(index == 2) {
                val = HWSink.ALARM_INTERVAL_TIME_180SEC;
            }
            if(val != -1) {
                Intent intent = new Intent();
                intent.putExtra(HWSink.EXTRA_DRV_CFG_ALARM_INTERVAL_TIME, val);
                HWSink.updateDriverConfig(intent);
            }
            PrefDataManager.setAlarmIntervalTime(val);
            Log.d("PP"," val_interval_time = " + val);
        }else if(key.equals(KEY_SHOOTING_MODE)){
            int index = Integer.parseInt((String)newValue);
            int val = -1;
            if(index == 0) {
                val = HWSink.MONITOR_Capture;
            } else if(index == 1) {
                val = HWSink.MONITOR_Recorder;
            }
            if(val != -1) {
                Intent intent = new Intent();
                intent.putExtra(HWSink.EXTRA_DRV_CFG_SHOOTING_MODE, val);
                HWSink.updateDriverConfig(intent);
            }
            PrefDataManager.setAlarmMode(val);
            Log.d("PP"," val_shoot_mode = " + val);
        }

        if (preference instanceof ListPreference) {
            /* Update ListPreference summary */
            ListPreference pref = (ListPreference) preference;
            pref.setSummary(pref.getEntries()[Integer.parseInt((String) newValue)]);
        }
        return true;
    }
}
