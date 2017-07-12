package com.ichano.rvs.streamer.constant;

@Deprecated
public enum RvsSessionState
{
  CONNECTED(2),  DISCONNECTED(3),  INVALID(-1);
  
  private int value;
  
  private RvsSessionState(int val)
  {
    this.value = val;
  }
  
  public int intValue()
  {
    return this.value;
  }
  
  public static RvsSessionState vauleOfInt(int value)
  {
    switch (value)
    {
    case 2: 
      return CONNECTED;
    case 3: 
      return DISCONNECTED;
    case -1: 
      return INVALID;
    }
    return INVALID;
  }
}
