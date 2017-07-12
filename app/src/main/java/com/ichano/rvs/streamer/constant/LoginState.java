package com.ichano.rvs.streamer.constant;

@Deprecated
public enum LoginState
{
  INIT(0),  CONNECTING(1),  CONNECTED(2),  DISCONNECT(3);
  
  private int value;
  
  private LoginState(int val)
  {
    this.value = val;
  }
  
  public int intValue()
  {
    return this.value;
  }
  
  public static LoginState vauleOfInt(int value)
  {
    switch (value)
    {
    case 0: 
      return INIT;
    case 1: 
      return CONNECTING;
    case 2: 
      return CONNECTED;
    case 3: 
      return DISCONNECT;
    }
    return CONNECTING;
  }
}
