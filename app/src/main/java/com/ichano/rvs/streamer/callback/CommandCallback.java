package com.ichano.rvs.streamer.callback;

public abstract interface CommandCallback
{
  public abstract void onSetUserInfo(String paramString1, String paramString2);
  
  public abstract void onSetStreamQuality(long paramLong1, long paramLong2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7);
  
  public abstract void onSwitchFrontRearCamera(long paramLong1, long paramLong2, int paramInt);
  
  public abstract void onSwitchTorch(long paramLong1, long paramLong2, int paramInt);
  
  public abstract void onPTZorMove(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5);
}
