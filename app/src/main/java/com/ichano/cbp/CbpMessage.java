package com.ichano.cbp;

public class CbpMessage {
	private int iSrcPid;
	  private int iDstPid;
	  private int iMsg;
	  private CbpBunch mbunch = new CbpBunch();
	  
	  public CbpMessage(int uiSrcPid, int uiDstPid, int uiMsg)
	  {
	    this.iSrcPid = uiSrcPid;
	    this.iDstPid = uiDstPid;
	    this.iMsg = uiMsg;
	  }
	  
	  public CbpMessage(int uiDstPid, int uiMsg)
	  {
	    this.iSrcPid = 3;
	    this.iDstPid = uiDstPid;
	    this.iMsg = uiMsg;
	  }
	  
	  public int getSrcPid()
	  {
	    return this.iSrcPid;
	  }
	  
	  public int getDstPid()
	  {
	    return this.iDstPid;
	  }
	  
	  public int getMsgID()
	  {
	    return this.iMsg;
	  }
	  
	  public int addUI(int iTag, int iVal)
	  {
	    return this.mbunch.addUI(iTag, iVal);
	  }
	  
	  public int addStr(int iTag, String strVal)
	  {
	    return this.mbunch.addString(iTag, strVal);
	  }
	  
	  public int addHandle(int iTag, long hVal)
	  {
	    return this.mbunch.addHandle(iTag, hVal);
	  }
	  
	  public int addXXLSIZE(int iTag, long iVal)
	  {
	    return this.mbunch.addXXLSIZE(iTag, iVal);
	  }
	  
	  public int addBool(int iTag, boolean iVal)
	  {
	    return this.mbunch.addBool(iTag, iVal);
	  }
	  
	  public int getUI(int iTag, int iDefault)
	  {
	    return this.mbunch.getUI(iTag, iDefault);
	  }
	  
	  public String getStr(int iTag)
	  {
	    return this.mbunch.getString(iTag);
	  }
	  
	  public long getHandle(int iTag)
	  {
	    return this.mbunch.getHandle(iTag);
	  }
	  
	  public long getXXLSIZE(int iTag, long iDefault)
	  {
	    return this.mbunch.getXXLSIZE(iTag, iDefault);
	  }
	  
	  public boolean getBool(int iTag, boolean iDefault)
	  {
	    return this.mbunch.getBool(iTag, iDefault);
	  }
	  
	  public boolean containsTag(int iTag)
	  {
	    return this.mbunch.containsTag(iTag);
	  }
	  
	  public boolean loopGetter(CbpBunch.GetRunner runner)
	  {
	    return CbpSys.sendMessage(this);
	  }
	  
	  public String toString()
	  {
	    return "iSrcPid :" + this.iSrcPid + ",iDstPid: " + this.iDstPid + ",iMsg:" + this.iMsg;
	  }
}
