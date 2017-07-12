package com.ichano.rvs.streamer.callback;

import com.ichano.rvs.streamer.bean.RvsAlarmRecordInfo;

public abstract interface MotionDetectSettingsCallback
{
  public abstract void onMotionDetectSettingUpdate(RvsAlarmRecordInfo paramRvsAlarmRecordInfo);
}
