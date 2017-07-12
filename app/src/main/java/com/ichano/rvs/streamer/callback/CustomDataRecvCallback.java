package com.ichano.rvs.streamer.callback;

public abstract interface CustomDataRecvCallback
{
  @Deprecated
  public abstract void onReceiveCustomData(long paramLong, byte[] paramArrayOfByte);
  
  public abstract void onCustomCommandListener(long paramLong, int paramInt, String paramString);
}
