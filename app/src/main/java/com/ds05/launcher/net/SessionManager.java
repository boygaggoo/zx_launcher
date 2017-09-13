package com.ds05.launcher.net;

import android.os.Handler;
import android.os.Looper;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.common.utils.ToastUtil;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

public class SessionManager {
	private static SessionManager mInstance=null;
    private static Handler mHandler = null;

    private IoSession mSession;
    public static SessionManager getInstance(){
        if(mInstance==null){
            synchronized (SessionManager.class){
                if(mInstance==null){
                    mInstance = new SessionManager();
                    mHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mInstance;
    }

    private SessionManager(){}

    public void setSession(IoSession session){
        this.mSession = session;
    }

    public void writeToServer(IoBuffer buffer){
        if(mSession!=null){
            buffer.flip();
            mSession.write(buffer);
        }else{
            //Toast.makeText( LauncherApplication.getContext(), "向服务器发送失败", Toast.LENGTH_SHORT).show();
            if(mHandler == null){
                mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(LauncherApplication.getContext(), "向服务器发送失败");
                }
            });
        }
    }

    public void closeSession(){
        if(mSession!=null){
            mSession.closeOnFlush();
        }
    }

    public void removeSession(){
        this.mSession=null;
    }
}
