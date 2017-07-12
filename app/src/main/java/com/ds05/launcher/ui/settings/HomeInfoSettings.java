package com.ds05.launcher.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.service.WeatherService;

/**
 * Created by Chongyang.Hu on 2017/1/8 0008.
 */

public class HomeInfoSettings extends ModuleBaseFragment implements Preference.OnPreferenceChangeListener {

    public static final String KEY_SHOW_QRCODE = "key_show_qrcode";
    public static final String KEY_SHOW_WEATHER = "key_show_weather";
    public static final String KEY_WEATHER_SETTINGS = "key_weather_settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.home_info_settings_frag);

        findPreference(KEY_SHOW_WEATHER).setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_str_home_settings);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if(key.equals(KEY_SHOW_QRCODE)) {
        } else if(key.equals(KEY_SHOW_WEATHER)) {
        } else if(key.equals(KEY_WEATHER_SETTINGS)) {
            jumpToFragment(new WeatherSettingsFrag());
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if(key.equals(KEY_SHOW_WEATHER) && (preference instanceof SwitchPreference)) {
            boolean val = (boolean)newValue;
            Context ctx = LauncherApplication.getContext();
            if(val) {
                ctx.startService(new Intent(ctx, WeatherService.class));
            } else {
                ctx.stopService(new Intent(ctx, WeatherService.class));
            }
            return true;
        }
        return true;
    }
}
