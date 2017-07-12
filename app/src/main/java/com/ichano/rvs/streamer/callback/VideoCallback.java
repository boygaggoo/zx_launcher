package com.ichano.rvs.streamer.callback;

public abstract interface VideoCallback
{
  public abstract void onKeyFrameRequired();
  
  public abstract byte[] onGetOneJpegFrame(int paramInt);
  
  public abstract void onVideoDataNotify(boolean paramBoolean);
  
  public abstract void onYuvDataNotify(boolean paramBoolean);
}
