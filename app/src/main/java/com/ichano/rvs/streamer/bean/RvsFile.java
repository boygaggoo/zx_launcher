package com.ichano.rvs.streamer.bean;

public class RvsFile {
	  private String fileName;
	  private String filePath;
	  private String createTime;
	  private long fileSize;
	  private boolean isFolder;
	  
	  public String getFileName()
	  {
	    return this.fileName;
	  }
	  
	  public void setFileName(String fileName)
	  {
	    this.fileName = fileName;
	  }
	  
	  public String getFilePath()
	  {
	    return this.filePath;
	  }
	  
	  public void setFilePath(String filePath)
	  {
	    this.filePath = filePath;
	  }
	  
	  public String getCreateTime()
	  {
	    return this.createTime;
	  }
	  
	  public void setCreateTime(String createTime)
	  {
	    this.createTime = createTime;
	  }
	  
	  public long getFileSize()
	  {
	    return this.fileSize;
	  }
	  
	  public void setFileSize(long fileSize)
	  {
	    this.fileSize = fileSize;
	  }
	  
	  public boolean isFolder()
	  {
	    return this.isFolder;
	  }
	  
	  public void setFolder(boolean isFolder)
	  {
	    this.isFolder = isFolder;
	  }
	}
