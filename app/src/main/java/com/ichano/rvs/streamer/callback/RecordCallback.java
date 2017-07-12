package com.ichano.rvs.streamer.callback;



import com.ichano.rvs.streamer.constant.RvsRecordState;
import com.ichano.rvs.streamer.constant.RvsRecordType;

public abstract interface RecordCallback
{
  public abstract void onRecordState(RvsRecordType paramRvsRecordType, RvsRecordState paramRvsRecordState);
}