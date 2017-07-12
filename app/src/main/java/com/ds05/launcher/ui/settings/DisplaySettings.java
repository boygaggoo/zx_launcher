package com.ds05.launcher.ui.settings;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.view.DialogSeekBarPreference;

/**
 * Created by Chongyang.Hu on 2017/1/16 0016.
 */

public class DisplaySettings extends ModuleBaseFragment
        implements Preference.OnPreferenceChangeListener {
    public static final String KEY_SCREEN_BRIGHTNESS = "key_screen_brightness";
    public static final String KEY_SCREEN_TIMEOUT = "key_screen_timeout";
    public static final String KEY_WALLPAPER = "key_wallpaper";

    private DialogSeekBarPreference mBrightnessPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.display_settings);

        findPreference(KEY_SCREEN_TIMEOUT).setOnPreferenceChangeListener(this);

        /* 配置亮度调节的Preference */
        ContentResolver cr = getActivity().getContentResolver();
        mBrightnessPref = (DialogSeekBarPreference)findPreference(KEY_SCREEN_BRIGHTNESS);
        int brightnessMode = Settings.System.getInt(cr,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        int brightness = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, 50);
        mBrightnessPref.setNegativeButtonText("");
        mBrightnessPref.showCheckBox(true);
        mBrightnessPref.setMax(255);
        mBrightnessPref.setProgress(brightness);
        mBrightnessPref.setCheckBoxString(R.string.string_auro_brightness);
        mBrightnessPref.setOnCheckedChangeListener(mAutoBrightnessChangedListener);
        mBrightnessPref.setProgressChangedListener(mBrightnessChangedListener);
        if(brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            mBrightnessPref.setChecked(true);
            mBrightnessPref.enabledProgress(false);
        } else if(brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
            mBrightnessPref.setChecked(false);
            mBrightnessPref.enabledProgress(true);
        }

        getPreferenceScreen().removePreference(findPreference(KEY_WALLPAPER));
    }

    private CompoundButton.OnCheckedChangeListener mAutoBrightnessChangedListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ContentResolver cr = getActivity().getContentResolver();
            Log.d("DS05", "{DisplaySettings}onCheckedChanged isChecked:" + isChecked);
            if(isChecked) {
                Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                mBrightnessPref.enabledProgress(false);
            } else {
                Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                mBrightnessPref.enabledProgress(true);
            }
        }
    };
    private SeekBar.OnSeekBarChangeListener mBrightnessChangedListener
            = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int value = seekBar.getProgress();
            Log.d("DS05", "{DisplaySettings}onStopTrackingTouch value:" + value);
            ContentResolver cr = getActivity().getContentResolver();
            if(value < 10) {
                value = 10;
                mBrightnessPref.setProgress(value);
            }
            Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, value);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_str_display);

        ListPreference screenTimeoutPref = (ListPreference)findPreference(KEY_SCREEN_TIMEOUT);
        String value = screenTimeoutPref.getValue();
        CharSequence[] values = screenTimeoutPref.getEntryValues();
        int i;
        for(i = 0; i < values.length; i++) {
            if(values[i].equals(value)) {
                break;
            }
        }
        if(i >= values.length) i = 0;
        screenTimeoutPref.setSummary(screenTimeoutPref.getEntries()[i]);

        ContentResolver cr = getActivity().getContentResolver();
        try {
            long currSleep = Settings.System.getLong(cr, Settings.System.SCREEN_OFF_TIMEOUT);
            Log.d("DS05", "{DisplaySettings}{onResume}App select time:" + value + "  system time:" + currSleep);
            if(currSleep != Long.parseLong(value)) {
                Settings.System.putLong(cr, Settings.System.SCREEN_OFF_TIMEOUT, Long.parseLong(value));
                Log.d("DS05", "{DisplaySettings}{onResume}Reset screen off time to " + value);
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.d("DS05", "{DisplaySettings}{onResume}ERROR:" + e.getMessage());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if(key.equals(KEY_SCREEN_BRIGHTNESS)) {
            ContentResolver cr = getActivity().getContentResolver();
            int brightnessMode = Settings.System.getInt(cr,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            int brightness = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, 50);
            mBrightnessPref.setNegativeButtonText("");
            mBrightnessPref.showCheckBox(true);
            mBrightnessPref.setMax(255);
            mBrightnessPref.setProgress(brightness);
            mBrightnessPref.setCheckBoxString(R.string.string_auro_brightness);
            mBrightnessPref.setOnCheckedChangeListener(mAutoBrightnessChangedListener);
            mBrightnessPref.setProgressChangedListener(mBrightnessChangedListener);
            if(brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mBrightnessPref.setChecked(true);
                mBrightnessPref.enabledProgress(false);
            } else if(brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                mBrightnessPref.setChecked(false);
                mBrightnessPref.enabledProgress(true);
            }
            return true;
        }//KEY_SCREEN_BRIGHTNESS
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if(key.equals(KEY_SCREEN_TIMEOUT)) {
            ListPreference pref = (ListPreference)preference;
            String value = (String)newValue;
            CharSequence[] values = pref.getEntryValues();
            int i;
            for(i = 0; i < values.length; i++) {
                if(values[i].equals(value)) {
                    break;
                }
            }
            pref.setSummary(pref.getEntries()[i]);

            ContentResolver cr = getActivity().getContentResolver();
            try {
                long currSleep = Settings.System.getLong(cr, Settings.System.SCREEN_OFF_TIMEOUT);
                if(currSleep != Long.parseLong(value)) {
                    Settings.System.putLong(cr, Settings.System.SCREEN_OFF_TIMEOUT, Long.parseLong(value));
                    Log.d("DS05", "{DisplaySettings}{onPreferenceChange}Reset screen off time to " + value);
                }
            } catch (Settings.SettingNotFoundException e) { }
        }

        return true;
    }
}
