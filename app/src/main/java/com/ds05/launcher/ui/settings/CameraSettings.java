package com.ds05.launcher.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.View;

import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.service.HWSink;

/**
 * Created by Chongyang.Hu on 2017/1/16 0016.
 */

public class CameraSettings extends ModuleBaseFragment
        implements Preference.OnPreferenceChangeListener {
    public static final String KEY_BACKLIGHT_LIGHT = "key_backlight_light";
    public static final String KEY_NIGHT_FILL_LIGHT_SENSI = "key_night_vision_light_sens";
    public static final String KEY_LIGHT_SOURCE_FREQ = "key_light_source_freq";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.camera_settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_str_camera_settings);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchPreference prefBackLight = (SwitchPreference) findPreference(KEY_BACKLIGHT_LIGHT);
        ListPreference prefFillLightSensi = (ListPreference) findPreference(KEY_NIGHT_FILL_LIGHT_SENSI);
        ListPreference prefLightSourceFreq = (ListPreference) findPreference(KEY_LIGHT_SOURCE_FREQ);

        prefBackLight.setOnPreferenceChangeListener(this);
        prefFillLightSensi.setOnPreferenceChangeListener(this);
        prefLightSourceFreq.setOnPreferenceChangeListener(this);

        prefFillLightSensi.setSummary(prefFillLightSensi
                .getEntries()[Integer.parseInt(prefFillLightSensi.getValue())]);
        prefLightSourceFreq.setSummary(prefLightSourceFreq
                .getEntries()[Integer.parseInt(prefLightSourceFreq.getValue())]);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if(key.equals(KEY_BACKLIGHT_LIGHT)) {
            Intent intent = new Intent();
            intent.putExtra(HWSink.EXTRA_DRV_CFG_BACK_LIGHT_STATE, (boolean)newValue);
            HWSink.updateDriverConfig(intent);
        } else if(key.equals(KEY_NIGHT_FILL_LIGHT_SENSI)) {
            int index = Integer.parseInt((String)newValue);
            int val = -1;
            if(index == 0)
                val = HWSink.NIGHT_LIGHT_SENSI_LOW;
            else if(index == 1)
                val = HWSink.NIGHT_LIGHT_SENSI_MIDDLE;
            else if(index == 2)
                val = HWSink.NIGHT_LIGHT_SENSI_HIGHT;

            if(val != -1) {
                Intent intent = new Intent();
                intent.putExtra(HWSink.EXTRA_DRV_CFG_NIGHT_LIGHT_SENSI, val);
                HWSink.updateDriverConfig(intent);
            }
        } else if(key.equals(KEY_LIGHT_SOURCE_FREQ)) {
            int index = Integer.parseInt((String)newValue);
            int val = -1;
            if(index == 0)
                val = HWSink.LIGHT_SRC_FREQ_50HZ;
            else if(index == 1)
                val = HWSink.LIGHT_SRC_FREQ_60HZ;

            if(val != -1) {
                Intent intent = new Intent();
                intent.putExtra(HWSink.EXTRA_DRV_CFG_LIGHT_SRC_FREQ, val);
                HWSink.updateDriverConfig(intent);
            }
        }

        if (preference instanceof ListPreference) {
            /* Update ListPreference summary */
            ListPreference pref = (ListPreference) preference;
            Log.d("TEST", "==>newValue:" + Integer.parseInt((String) newValue));
            Log.d("TEST", "==>len:" + pref.getEntries().length);
            pref.setSummary(pref.getEntries()[Integer.parseInt((String) newValue)]);
        }
        return true;
    }
}
