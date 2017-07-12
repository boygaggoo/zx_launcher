package com.ichano.rvs.streamer.bean;

public class ScheduleSetting {
	private boolean enable;
	  private int weekFlag;
	  private int startSecond;
	  private int endSecond;
	  private int intervalValue;
	  
	  public boolean isEnable()
	  {
	    return this.enable;
	  }
	  
	  public void setEnable(boolean enable)
	  {
	    this.enable = enable;
	  }
	  
	  public int getWeekFlag()
	  {
	    return this.weekFlag;
	  }
	  
	  public void setWeekFlag(int weekFlag)
	  {
	    this.weekFlag = weekFlag;
	  }
	  
	  public int getStartSecond()
	  {
	    return this.startSecond;
	  }
	  
	  public void setStartSecond(int startSecond)
	  {
	    this.startSecond = startSecond;
	  }
	  
	  public int getEndSecond()
	  {
	    return this.endSecond;
	  }
	  
	  public void setEndSecond(int endSecond)
	  {
	    this.endSecond = endSecond;
	  }
	  
	  public int getIntervalValue()
	  {
	    return this.intervalValue;
	  }
	  
	  public void setIntervalValue(int intervalValue)
	  {
	    this.intervalValue = intervalValue;
	  }
}
