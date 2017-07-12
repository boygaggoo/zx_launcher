package com.ds05.launcher.ui.media;

import android.os.Bundle;

import com.ds05.launcher.R;
import com.ds05.launcher.ModuleBaseActivity;

/**
 * Created by Chongyang.Hu on 2017/1/1 0001.
 */

public class MediaActivity extends ModuleBaseActivity {

    @Override
    protected void onInit(Bundle savedInstanceState) {
        showTitleBar();
        setTitle(R.string.string_photo);
    }
}
