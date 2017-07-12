package com.ichano.rvs.streamer.callback;

public abstract interface RecordFilesCallback
{
  public abstract String onRecordFileOpen(String paramString);
  
  public abstract void onRecordFileClose(String paramString);
}
