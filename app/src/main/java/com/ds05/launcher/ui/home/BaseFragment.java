package com.ds05.launcher.ui.home;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;

public abstract class BaseFragment extends Fragment {
	public static final String FRAG_SWITCH_AUTHORITIES = 
			"content://com.ds05.launcher.FRAG_SWITCH_AUTHORITIES";

	public Context getContext() {
		return getActivity();
	}
	
	public void switchFragment(String uriString) {
		getContext().getContentResolver().notifyChange(
				Uri.parse(uriString), null);
	}

	public static String getFragmentUri(Class cls) {
		return FRAG_SWITCH_AUTHORITIES + "/" + cls.getSimpleName();
	}
}
