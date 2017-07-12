package com.ds05.launcher.service.rs;

/**
 * Created by Chongyang.Hu on 2017/4/13 0013.
 */

interface InternalObserver {
    void onReceiverChanged(String key, Object obj, boolean sendBroadcast);
}
