package com.ds05.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.common.Constants;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.common.utils.AppUtil;
import com.ds05.launcher.listener.HeartBeatListener;
import com.ds05.launcher.listener.MinaHandler;
import com.ds05.launcher.net.MsgProtocolcodecFactory;
import com.ds05.launcher.net.SessionManager;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

/**
 * create by vincent
 */
public class ConnectSocketService extends Service {

    private final static String TAG = "ConnectSocketService";

    @Override
    public void onCreate() {

        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>开启单独的线程，因为Service是位于主线程的，为了避免主线程被阻塞<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        // 开启单独的线程，因为Service是位于主线程的，为了避免主线程被阻塞
        HeartBeatThread thread = new HeartBeatThread();
        thread.start();
        super.onCreate();
    }

    class HeartBeatThread extends Thread {
        @Override
        public void run() {
            initClientMina(ConnectUtils.getHost(), ConnectUtils.PORT);
        }
    }

    /**
     * 初始化客户端MINA
     *
     * @param host
     * @param port
     */
    public void initClientMina(String host, int port) {

        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>initClientMina<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        NioSocketConnector connector = null;
        try {
            connector = new NioSocketConnector();
            MinaHandler handler = new MinaHandler();// 创建handler对象，用于业务逻辑处理
            connector.setHandler(handler);
            connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MsgProtocolcodecFactory()));// 添加Filter对象
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.i(TAG, e2.toString());
        }
        connector.setConnectTimeoutMillis(ConnectUtils.TIMEOUT); // 设置连接超时时间
        int count = 0;// 记录连接次数

        Log.e(TAG, "########################################################################  initClientMina ,当前的网络状态是：" + ConnectUtils.NETWORK_IS_OK);

        while (true) {
            if (ConnectUtils.NETWORK_IS_OK == false) {
                Log.e(TAG, "当前的网络还不可用，等待可用之后进行重连");
                return;
            }
            try {
                count++;
                // 执行到这里表示客户端刚刚启动需要连接服务器,第一次连接服务器的话是没有尝试次数限制的，但是随后的断线重连就有次数限制了
                ConnectFuture future = connector.connect(new InetSocketAddress(ConnectUtils.getHost(), ConnectUtils.PORT));
                future.awaitUninterruptibly();// 一直阻塞,直到连接建立
                IoSession session = future.getSession();// 获取Session对象
                if (session.isConnected()) {
                    SessionManager.getInstance().setSession(session);
                    // 表示连接成功
                    Log.i(TAG, ConnectUtils.stringNowTime() + " : 客户端连接服务器成功.....");
                    ConnectUtils.CONNECT_SERVER_STATUS = true;

                    int alarmSensi = 2;
                    if(PrefDataManager.MonitorSensitivity.High.equals(PrefDataManager.getHumanMonitorSensi())){
                        alarmSensi = 1;
                    }else {
                        alarmSensi = 2;
                    }
                    int alarmMode = 0;
                    if(PrefDataManager.AlarmMode.Capture.equals(PrefDataManager.getAlarmMode())){
                        alarmMode = 0;
                    }else if(PrefDataManager.AlarmMode.Recorder.equals(PrefDataManager.getAlarmMode())){
                        alarmMode = 1;
                    }
                    int alarmsound = 0;
                    if(PrefDataManager.AutoAlarmSound.Silence.equals(PrefDataManager.getAlarmSound())){
                        alarmsound = 1;
                    }else if(PrefDataManager.AutoAlarmSound.Alarm.equals(PrefDataManager.getAlarmSound())){
                        alarmsound = 2;
                    }else if(PrefDataManager.AutoAlarmSound.Scream.equals(PrefDataManager.getAlarmSound())){
                        alarmsound = 3;
                    }
                    int length = (int)PrefDataManager.getAlarmSoundVolume()*10;

                    String msg = "[" + System.currentTimeMillis() + ",T1," + Constants.SOFT_VERSION + "," + AppUtil.getZYLicense() + "," + PrefDataManager.getHumanMonitorState() + "," +
                            PrefDataManager.getAutoAlarmTime() + "," + alarmSensi+","+ alarmMode +","+ 1 +","+ alarmsound +","+length+","+PrefDataManager.getDoorbellLight()+","+
                            PrefDataManager.getDoorbellSoundIndex()+","+PrefDataManager.getAlarmIntervalTime()+","+ AppUtil.BATTERY_LEVEL + "," + AppUtil.getWifiSSID(LauncherApplication.getContext()) + "]";
                    IoBuffer buffer = IoBuffer.allocate(msg.length());
                    buffer.put(msg.getBytes());
                    SessionManager.getInstance().writeToServer(buffer);
                    break;
                }
            } catch (RuntimeIoException e) {
                Log.e(TAG, ConnectUtils.stringNowTime() + " : 第" + count + "次客户端连接服务器失败，因为" + ConnectUtils.TIMEOUT + "s没有连接成功");
                try {
                    Thread.sleep(5000);// 如果本次连接服务器失败，则间隔2s后进行重连操作
                    Log.e(TAG, ConnectUtils.stringNowTime() + " : 开始第" + (count + 1) + "次连接服务器");
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        // 为MINA客户端添加监听器，当Session会话关闭的时候，进行自动重连
        connector.addListener(new HeartBeatListener(connector));
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
