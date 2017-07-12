package com.ichano.rvs.streamer.callback;

import com.ichano.rvs.streamer.constant.AuthState;
import com.ichano.rvs.streamer.constant.LoginError;
import com.ichano.rvs.streamer.constant.LoginState;
import com.ichano.rvs.streamer.constant.RemoteViewerState;
import com.ichano.rvs.streamer.constant.RvsError;
import com.ichano.rvs.streamer.constant.RvsSessionState;

public abstract interface StreamerCallback
{
	  @Deprecated
	  public abstract void onLoginResult(LoginState paramLoginState, int paramInt, LoginError paramLoginError);
	  
	  public abstract void onAuthResult(AuthState paramAuthState, RvsError paramRvsError);
	  
	  public abstract void onUpdateCID(long paramLong);
	  
	  public abstract void onDeviceNameChange(String paramString);
	  
	  public abstract void onUpdateUserName();
	  
	  @Deprecated
	  public abstract void onSessionStateChange(long paramLong, RvsSessionState paramRvsSessionState);
	  
	  public abstract void onRemoteViewerStateChange(long paramLong, RemoteViewerState paramRemoteViewerState, RvsError paramRvsError);
	  
	  public abstract void onPushStateChange(boolean paramBoolean);
	  
	  public abstract void onEmailStateChange(boolean paramBoolean);
	}
