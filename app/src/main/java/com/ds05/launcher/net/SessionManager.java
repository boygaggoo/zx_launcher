package com.ds05.launcher.net;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import android.widget.Toast;

import com.ds05.launcher.LauncherApplication;

public class SessionManager {
	private static SessionManager mInstance=null;

    private IoSession mSession;
    public static SessionManager getInstance(){
        if(mInstance==null){
            synchronized (SessionManager.class){
                if(mInstance==null){
                    mInstance = new SessionManager();
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
            Toast.makeText( LauncherApplication.getContext(), "向服务器发送失败", Toast.LENGTH_SHORT).show();
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
