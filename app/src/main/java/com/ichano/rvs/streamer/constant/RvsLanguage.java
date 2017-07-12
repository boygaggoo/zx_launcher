package com.ichano.rvs.streamer.constant;

public enum RvsLanguage {
	  zh_CN(1),  en(2),  zh_TW(3),  fr(4),  ja(5),  es(6),  ko(7),  it(8),  pt(9),  ru(10),  th(11),  de(12),  ar(13),  el(14),  udf(-1);
	  
	  private int value;
	  
	  private RvsLanguage(int val)
	  {
	    this.value = val;
	  }
	  
	  public int intValue()
	  {
	    return this.value;
	  }
	  
	  public static RvsLanguage valueOfInt(int value)
	  {
	    switch (value)
	    {
	    case 1: 
	      return zh_CN;
	    case 2: 
	      return en;
	    case 3: 
	      return zh_TW;
	    case 4: 
	      return fr;
	    case 5: 
	      return ja;
	    case 6: 
	      return es;
	    case 7: 
	      return ko;
	    case 8: 
	      return it;
	    case 9: 
	      return pt;
	    case 10: 
	      return ru;
	    case 11: 
	      return th;
	    case 12: 
	      return de;
	    case 13: 
	      return ar;
	    case 14: 
	      return el;
	    }
	    return en;
	  }
}
