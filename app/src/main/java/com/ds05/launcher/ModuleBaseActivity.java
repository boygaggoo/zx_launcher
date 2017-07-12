package com.ds05.launcher;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Chongyang.Hu on 2017/1/1 0001.
 */

public class ModuleBaseActivity extends Activity {

    private TitleBarManager mTitleBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_layout);

        Log.d("DS05", "ModuleBaseActivity onCreate");

        mTitleBar = new TitleBarManager(this);

        onInit(savedInstanceState);
    }

    public void replaceFragment(ModuleBaseFragment frag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, frag);
        fragmentTransaction.addToBackStack(frag.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    public void jumpToFragment(ModuleBaseFragment frag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, frag);
        fragmentTransaction.addToBackStack(frag.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    public void jumpToFragment(Fragment frag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, frag);
        fragmentTransaction.addToBackStack(frag.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        Log.d("DS05", "stack-count:" + getFragmentManager().getBackStackEntryCount());
        if(getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
            Log.d("DS05", "stack-count:" + getFragmentManager().getBackStackEntryCount());
        } else {
            getFragmentManager().popBackStack();
            super.onBackPressed();
        }
    }

    protected void onInit(Bundle savedInstanceState) { }

    public void showTitleBar() {
        mTitleBar.showTitleBar();
    }

    public void hideTitleBar() {
        mTitleBar.hideTitleBar();
    }

    public void setTitle(int resId) {
        mTitleBar.setTitle(resId);
    }

    public void setTitle(String title) {
        mTitleBar.setTitle(title);
    }
}
