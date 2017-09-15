package com.ds05.launcher.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ds05.launcher.common.config.MyAvsHelper;
import com.ds05.launcher.common.manager.PrefDataManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConnectUtils {
	public static final int REPEAT_TIME = 5000;//表示重连次数
	public static final String TEST_HOST = "139.196.191.140";//表示IP地址  192.168.4.31   139.196.191.140
	public static final String OFFICIAL_HOST = "server.secaiot.cn";
	public static final int PORT = 3005;//表示端口号
	public static final int IDLE_TIME = 10;//客户端10s内没有向服务端发送数据
	public static final long TIMEOUT = 5000;//设置连接超时时间,超过5s还没连接上便抛出异常

	public static boolean NETWORK_IS_OK=false;
	public static boolean CONNECT_SERVER_STATUS=false;

	public static boolean isConnectintSocket=false;


	public static MyAvsHelper mMyAvsHelper;

	/**
	 * 获取当前时间
	 * @return
	 */
	public static String stringNowTime()
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}

	public static boolean isNetAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return (info != null && info.isAvailable());
	}

	public static String getHost() {
		int val = PrefDataManager.getServerType();
		if(val == 0){
			return OFFICIAL_HOST;
		}else{
			return TEST_HOST;
		}
	}
}
