package com.ichano.rvs.streamer.exception;

public class IllegalCommandIDException extends RuntimeException
{
	  private static final long serialVersionUID = 1L;
	  
	  public IllegalCommandIDException() {}
	  
	  public IllegalCommandIDException(String ex)
	  {
	    super(ex);
	  }
	}

