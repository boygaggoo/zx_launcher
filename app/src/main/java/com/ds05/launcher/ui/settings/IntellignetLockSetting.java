package com.ds05.launcher.ui.settings;

import android.os.Bundle;

import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;

/**
 * Created by Chongyang.Hu on 2017/1/21 0021.
 */

public class IntellignetLockSetting extends ModuleBaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.intelligent_lock_settings);
    }
}
