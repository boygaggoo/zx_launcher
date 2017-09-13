package com.ds05.launcher.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.common.config.MyAvsHelper;
import com.ds05.launcher.common.utils.AppUtil;
import com.ds05.launcher.common.utils.ToastUtil;
import com.ds05.launcher.service.ConnectSocketService;
import com.ds05.launcher.service.ZhongyunService;

import org.jsoup.helper.StringUtil;

public class NetCheckReceiver extends BroadcastReceiver {

    private final static String TAG="NetCheckReceiver";
	// android 中网络变化时所发的Intent的名字
	private static final String netACTION = "android.net.conn.CONNECTIVITY_CHANGE";

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(netACTION)) {
			// Intent中ConnectivityManager.EXTRA_NO_CONNECTIVITY这个关键字表示着当前是否连接上了网络
			// true 代表网络断开      false 代表网络没有断开
			if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
				ToastUtil.showToast(context,"網絡不可用");
                ConnectUtils.NETWORK_IS_OK =false;
				ConnectUtils.CONNECT_SERVER_STATUS=false;
				ConnectUtils.isConnectintSocket=false;
                Log.e(TAG, "########################################################################   網絡不可用："+ConnectUtils.NETWORK_IS_OK);
				context.stopService(new Intent(context, ConnectSocketService.class));
//				context.stopService(new Intent(context, ZhongyunService.class));
			} else {
				ToastUtil.showToast(context,"網絡可用");
				ConnectUtils.NETWORK_IS_OK = true;
                Log.e(TAG, "########################################################################   網絡可用："+ConnectUtils.NETWORK_IS_OK+" ,  "+ConnectUtils.isConnectintSocket);

				if(ConnectUtils.CONNECT_SERVER_STATUS==false && ConnectUtils.isConnectintSocket==false){
					ConnectUtils.isConnectintSocket=true;
					context.startService(new Intent(context, ConnectSocketService.class));
//					context.startService(new Intent(context, ZhongyunService.class));
				}
				MyAvsHelper mMyAvsHelper = MyAvsHelper.getInstance(context);
				if(!StringUtil.isBlank(AppUtil.getZYLicense())){
					mMyAvsHelper.login();
				}
			}
		}
	}
}
