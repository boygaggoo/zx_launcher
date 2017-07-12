package com.ichano.rvs.streamer.constant;

@Deprecated
public enum LoginError
{
  NO_ERR(0),  SERVICEGET_ERR(
    1),  APPID_ERR(
    2),  COMPANYINFO_ERR(
    3),  LICENSE_ERR(
    4),  FULLLICENSE_ERR(
    5),  TIMEOUT_ERR(
    6),  CONNECT_ERR(
    7),  REGISTER_ERR(
    8),  ALLOCATE_ERR(
    9),  GETSYSCONFIG_ERR(
    10),  UPLOADINFO_ERR(
    11),  CONNECT_INTERUPT(
    12),  ERR_WRONG_PACKAGE(
  
    999),  LOCAL_IP_ERR(
    9999),  ERR_UNDEFINED(
    13);
  
  private int value;
  
  private LoginError(int val)
  {
    this.value = val;
  }
  
  public int intValue()
  {
    return this.value;
  }
  
  public static LoginError valueOfInt(int value)
  {
    switch (value)
    {
    case 0: 
      return NO_ERR;
    case 1: 
      return SERVICEGET_ERR;
    case 2: 
      return APPID_ERR;
    case 3: 
      return COMPANYINFO_ERR;
    case 4: 
      return LICENSE_ERR;
    case 5: 
      return FULLLICENSE_ERR;
    case 6: 
      return TIMEOUT_ERR;
    case 7: 
      return CONNECT_ERR;
    case 8: 
      return REGISTER_ERR;
    case 9: 
      return ALLOCATE_ERR;
    case 10: 
      return GETSYSCONFIG_ERR;
    case 11: 
      return UPLOADINFO_ERR;
    case 12: 
      return CONNECT_INTERUPT;
    case 999: 
      return ERR_WRONG_PACKAGE;
    case 9999: 
      return LOCAL_IP_ERR;
    }
    return ERR_UNDEFINED;
  }
}
