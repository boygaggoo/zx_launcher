package com.ds05.launcher.ui.settings;

import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.widget.SeekBar;

import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.service.SoundManager;
import com.ds05.launcher.view.DialogSeekBarPreference;
import com.ds05.launcher.view.ListPreferenceExt;

/**
 * Created by Chongyang.Hu on 2017/1/16 0016.
 */

public class DoorbellSettings extends ModuleBaseFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_DOORBELL_LIGHT = "key_doorbell_light";
    public static final String KEY_DOORBELL_SOUND = "key_doorbell_sound";
    public static final String KEY_DOORBELL_VOLUME = "key_doorbell_volume";

    private SoundManager mSoundManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.doorbell_settings);

        mSoundManager = SoundManager.getInstance();

        final ListPreferenceExt soundPref = (ListPreferenceExt)findPreference(KEY_DOORBELL_SOUND);
        soundPref.setOnPreferenceChangeListener(this);
        soundPref.setSummary(soundPref.getEntries()[Integer.parseInt(soundPref.getValue())]);
        soundPref.setOnListItemClickListener(new ListPreferenceExt.OnListItemClickListener() {
            @Override
            public void onItemClick(int index) {
                mSoundManager.testDoorbellSound(index, mSoundManager.getDoorbellVolume());
            }
        });

        DialogSeekBarPreference volumePref = (DialogSeekBarPreference)findPreference(KEY_DOORBELL_VOLUME);
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
                mSoundManager.testDoorbellSound(Integer.parseInt(soundPref.getValue()), seekBar.getProgress() / 10f);
            }
        });
        volumePref.setMax(10);
        volumePref.setProgress((int)(PrefDataManager.getDoorbellVolume() * 10));
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_str_doorbell_settings);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if(key.equals(KEY_DOORBELL_LIGHT)) {
          //  PrefDataManager.setDoorbellLight();
        } else if(key.equals(KEY_DOORBELL_SOUND)) {
            ListPreferenceExt pref = (ListPreferenceExt) preference;
            pref.setSummary(pref.getEntries()[Integer.parseInt((String) newValue)]);
            mSoundManager.stopTestSound();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSoundManager.updateDoorbellConfig();
                }
            }, 300);

            return true;
        } else if (key.equals(KEY_DOORBELL_VOLUME)) {
            PrefDataManager.setDoorbellVolume(Integer.parseInt((String) newValue) / 10f);
            mSoundManager.stopTestSound();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSoundManager.updateDoorbellConfig();
                }
            }, 300);
            return true;
        }
        return true;
    }
}
