package com.ds05.launcher.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.ds05.launcher.common.manager.PrefDataManager;

/**
 * Created by chongyanghu on 17-4-12.
 */
public class DS05DataProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if(method.equals("getHumanMonitorState")) {
            boolean state = PrefDataManager.getHumanMonitorState();
            Bundle ret = new Bundle();
            ret.putBoolean("RET_STATE", state);
            return ret;
        } else if(method.equals("getAutoAlarmTime")) {
            long time = PrefDataManager.getAutoAlarmTime();
            Bundle ret = new Bundle();
            ret.putLong("RET_AUTO_ALARM_TIME", time);
            return ret;
        }
        return null;
    }
}



















































