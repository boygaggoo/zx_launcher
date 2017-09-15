package com.ds05.launcher.listener;

import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.common.utils.ToastUtil;
import com.ds05.launcher.net.SessionManager;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

public class HeartBeatListener implements IoServiceListener {
	
	private final static String TAG="NETWORK";
	public NioSocketConnector connector;

	public HeartBeatListener(NioSocketConnector connector) {
		this.connector = connector;
	}

	@Override
	public void serviceActivated(IoService arg0) throws Exception {
	}

	@Override
	public void serviceDeactivated(IoService arg0) throws Exception {
	}

	@Override
	public void serviceIdle(IoService arg0, IdleStatus arg1) throws Exception {
	}

	@Override
	public void sessionClosed(IoSession arg0) throws Exception {
		Log.i(TAG,"hahahaha");
	}

	@Override
	public void sessionCreated(IoSession arg0) throws Exception {
	}

	@Override
	public void sessionDestroyed(IoSession arg0) {
		repeatConnect("");
	}

	/*
	 * 断线重连操作
	 * 
	 * @param content
	 */
	public void repeatConnect(String content) {
		// 执行到这里表示Session会话关闭了，需要进行重连,我们设置每隔3s重连一次,如果尝试重连5次都没成功的话,就认为服务器端出现问题,不再进行重连操作

		Log.e(TAG, " HeartBeatListener ########################################################################   Session会话关闭了,需要进行重连,当前的网络状态是："+ConnectUtils.NETWORK_IS_OK);

		int count = 0;// 记录尝试重连的次数
		while (true) {
			if(ConnectUtils.NETWORK_IS_OK==false){
				//Toast.makeText(LauncherApplication.getContext(), "当前网络不可用", Toast.LENGTH_SHORT).show();
				ToastUtil.showToast(LauncherApplication.getContext(), "当前网络不可用");
				Log.i(TAG," >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>当前网络不可用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
				count = 0;
			}else{
				try {
					count++;// 重连次数加1
					ConnectFuture future = connector.connect(new InetSocketAddress(ConnectUtils.getHost(), ConnectUtils.PORT));
					future.awaitUninterruptibly();// 一直阻塞住等待连接成功
					IoSession session = future.getSession();// 获取Session对象
					if (session.isConnected()) {
						SessionManager.getInstance().setSession(session);
						// 表示重连成功
						Log.i(TAG,content + ConnectUtils.stringNowTime() + " : 断线重连" + count + "次之后成功.....");
						ConnectUtils.CONNECT_SERVER_STATUS=true;
						count = 0;
						break;
					}
				} catch (Exception e) {
					if (count == ConnectUtils.REPEAT_TIME) {
						Log.e(TAG,content + ConnectUtils.stringNowTime() + " : 断线重连" + ConnectUtils.REPEAT_TIME+ "次之后仍然未成功,结束重连.....");
						//break;
					} else {
						Log.e(TAG, content + ConnectUtils.stringNowTime() + " : 本次断线重连失败,3s后进行第" + (count + 1) + "次重连.....");
						try {
							Thread.sleep(5000);
							Log.e(TAG,content + ConnectUtils.stringNowTime() + " : 开始第" + (count + 1) + "次重连.....");
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}
}
