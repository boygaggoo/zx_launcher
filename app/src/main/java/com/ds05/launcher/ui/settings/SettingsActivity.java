package com.ds05.launcher.ui.settings;

import android.os.Bundle;

import com.ds05.launcher.R;
import com.ds05.launcher.ModuleBaseActivity;

public class SettingsActivity extends ModuleBaseActivity {

	@Override
	protected void onInit(Bundle savedInstanceState) {
		showTitleBar();
		setTitle(R.string.string_system_settings);

		replaceFragment(new SettingsFragment());
	}
}
