package com.ichano.rvs.streamer.callback;

import com.ichano.rvs.streamer.constant.MotionDetectState;

public abstract interface MotionDetectCallback
{
  public abstract void onMotionDetectState(MotionDetectState paramMotionDetectState);
}
