package com.ichano.rvs.streamer.callback;

import com.ichano.rvs.streamer.bean.RvsTimeRecordInfo;

public abstract interface TimeRecordSettingsCallback {
	public abstract void onTimeRecordSettingUpdate(RvsTimeRecordInfo paramRvsTimeRecordInfo);
}