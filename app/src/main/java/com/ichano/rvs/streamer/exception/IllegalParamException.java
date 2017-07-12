package com.ichano.rvs.streamer.exception;

public class IllegalParamException extends RuntimeException
{
	  private static final long serialVersionUID = 1L;
	  
	  public IllegalParamException() {}
	  
	  public IllegalParamException(String ex)
	  {
	    super(ex);
	  }
	}
