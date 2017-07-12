package com.ichano.rvs.streamer.bean;

public class RvsTimeRecordInfo {
	public static final int FLAG_NO_SET = 0;
	  public static final int FLAG_SET_OPEN = 1;
	  public static final int FLAG_SET_CLOSE = 2;
	  private int camIndex;
	  private int setFlag;
	  private ScheduleSetting[] scheduleSettings;
	  
	  public int getCamIndex()
	  {
	    return this.camIndex;
	  }
	  
	  public int getSetFlag()
	  {
	    return this.setFlag;
	  }
	  
	  public ScheduleSetting[] getScheduleSettings()
	  {
	    return this.scheduleSettings;
	  }
	  
	  public void setCamIndex(int camIndex)
	  {
	    this.camIndex = camIndex;
	  }
	  
	  public void setSetFlag(int setFlag)
	  {
	    this.setFlag = setFlag;
	  }
	  
	  public void setScheduleSettings(ScheduleSetting[] scheduleSettings)
	  {
	    this.scheduleSettings = scheduleSettings;
	  }
}
