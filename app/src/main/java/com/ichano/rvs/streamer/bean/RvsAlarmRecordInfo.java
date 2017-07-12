package com.ichano.rvs.streamer.bean;

public class RvsAlarmRecordInfo {
	private int camIndex;
	  private ScheduleSetting[] scheduleSettings;
	  
	  public int getCamIndex()
	  {
	    return this.camIndex;
	  }
	  
	  public void setCamIndex(int camIndex)
	  {
	    this.camIndex = camIndex;
	  }
	  
	  public ScheduleSetting[] getScheduleSettings()
	  {
	    return this.scheduleSettings;
	  }
	  
	  public void setScheduleSettings(ScheduleSetting[] scheduleSettings)
	  {
	    this.scheduleSettings = scheduleSettings;
	  }
}
