package com.ichano.rvs.streamer.callback;

public abstract interface CustomDataReceiveListener
{
  public abstract void onReceiveCustomData(long paramLong, String paramString, byte[] paramArrayOfByte);
}
