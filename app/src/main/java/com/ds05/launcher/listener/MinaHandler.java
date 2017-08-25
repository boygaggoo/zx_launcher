package com.ds05.launcher.listener;

import android.content.Intent;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.common.Constants;
import com.ds05.launcher.common.utils.AppUtil;
import com.ds05.launcher.net.response.HeartBeatMsg;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaHandler extends IoHandlerAdapter {

	private final static String TAG="NETWORK";
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		Log.e(TAG, ConnectUtils.stringNowTime() + " : 客户端调用exceptionCaught"+cause.getMessage());
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		//Log.i(TAG,ConnectUtils.stringNowTime() + " : ￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥客户端收到消息："+message);
		Intent myIntent = new Intent();
		if(message.toString().equals("[S7]")){
			myIntent.setAction(Constants.BROADCAST_ACTION_OPEN_CAMERA);
			LauncherApplication.getContext().sendBroadcast(myIntent);
		}else if(message.toString().equals("[S8]")){
			myIntent.setAction(Constants.BROADCAST_ACTION_CLOSE_CAMERA);
			LauncherApplication.getContext().sendBroadcast(myIntent);
		}else if(message.toString().equals("[S9]")){
			myIntent.setAction(Constants.BROADCAST_ACTION_TEST_CAMERA);
			LauncherApplication.getContext().sendBroadcast(myIntent);
		}else if(message.toString().equals("[S3]")){
			myIntent.setAction(Constants.BROADCAST_ACTION_RECEIVE_CONFIG_FROM_SERVER);
			myIntent.putExtra(Constants.MSG_FROM_SERVER, message.toString());
			LauncherApplication.getContext().sendBroadcast(myIntent);
		}else if(message.toString().equals("[T3]")){
			myIntent.setAction(Constants.BROADCAST_ACTION_RESPONSE_UPLOAD_CONFIG);
			myIntent.putExtra(Constants.MSG_FROM_SERVER, message.toString());
			LauncherApplication.getContext().sendBroadcast(myIntent);
		}else if(message.toString().equals("[S10]")){
			myIntent.setAction(Constants.BROADCAST_ACTION_RESPONSE_UPLOAD_CONFIG);
			myIntent.putExtra(Constants.MSG_FROM_SERVER, message.toString());
			LauncherApplication.getContext().sendBroadcast(myIntent);
		}


	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		//Log.i(TAG,ConnectUtils.stringNowTime() + " : 客户端调用messageSent");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// System.out.println(ConnectUtils.stringNowTime()+" :
		// 客户端调用sessionClosed");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		Log.i(TAG,ConnectUtils.stringNowTime() + " : 客户端调用sessionIdle");
		if(session!=null){
			String heartbeat="[T0,"+Constants.ZHONGYUN_LINCESE+","+ System.currentTimeMillis() +","+ AppUtil.BATTERY_LEVEL+","+ AppUtil.getWifiLevel(LauncherApplication.getContext())+","+ AppUtil.getWifiSSID(LauncherApplication.getContext()) + "]";
			session.write(new HeartBeatMsg(heartbeat));
		}
	}

}
