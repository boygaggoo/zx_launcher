package com.ichano.rvs.streamer.constant;

public enum RvsError {
	UNKNOW(-1),  SUCESS(0),  PAREMETER_ERR(1),  DECRESPONSE_ERR(2),  NONEXIST(3),  AUTHERERR(1000),  AUTHER_PARMETER_ERR(1001),  AUTHER_VERIFYCODE_ERR(1002),  AUTHER_APPID_PAR_ERR(1003),  AUTHER_APPID_FULL_ERR(1004),  AUTHER_LICENSE_USED_ERR(1005),  AUTHER_USRINF_ERR(1007),  AUTHER_REQST_FREQUENTLY(1008),  AUTHER_GETCHECHCODE_ERR(1009),  AUTHER_DEVCOMPANY_ERR(1010),  AUTHER_DEVAPP_ERR(1011),  AUTHER_DEV_NOTREGST(1012),  AUTHER_EMAIL_HAVEBIND(1105),  AUTHER_EMAIL_HAVEEXIS(1106),  AUTHER_MOBILE_HAVEEXIST(1107),  AUTHER_MOBILE_HAVEBIND(1108),  AUTHER_VIVERCID_EXIST(1109),  AUTHER_STREAMCID_EXIST(1110),  AUTHER_COMPANY_NOTEXIST(1200),  AUTHER_APPID_NOTEXIST(1201),  AUTHER_LICENSE_NOTEXIST(1202),  AUTHER_VIEWERCID_NOTEXIST(1203),  AUTHER_STREAMCID_NOTEXIST(1204),  AUTHER_PRODUCT_NOTEXIST(1205),  AUTHER_USR_NOTEXIST(1206),  AUTHER_SESSION_NOTEXIST(1207),  AUTHER_RESISTQUERYFAILL(1208),  AUTHER_UNKNON(1299),  USR_LISTNODE_FULL(1301),  USR_SIGN_AGAIN(1302),  USR_AVS_EXISTED(1303),  USR_SESSION_NOTEXIST(1304),  USR_ACCOUNT_NOTEXIST(1305),  USR_LIST_NOTEXIST(1306),  USR_TASKID_NOTEXIST(1307),  USR_VERSION_MUSTUPDATE(1308),  INF_AVS_HAVEBINDBYOUTHER(1401),  INF_AVS_DSTHAVECLOUD(1402),  INF_AVS_SRCONCLOUD(1403),  INF_AVS_ENFORCE_BIND(1412),  INF_STREAMER_NOTEXIST(1413),  INF_VIEWER_NOTEXIST(1414),  INF_STREAMER_CAP_NOTEXIST(1415),  INF_VIEWER_CAP_NOTEXIST(1416),  INF_STREAMER_SUP_NOTEXIST(1417),  INF_VIEWER_SUP_NOTEXIST(1418),  INF_STREAMER_BUS_NOTEXIST(1419),  INF_VIEWER_BUS_NOTEXIST(1420),  INF_STREAMER_SERVICE_NOTEXIST(1421),  INF_VIEWER_SERVICE_NOTEXIST(1422),  SERVER_SYSTERM_ERR(1999),  NET_ERR(2000),  NET_SOCKET_ERR(2001),  NET_TIMEOUT(2002),  NET_CONNETERR(2003),  NET_RCVDATAERR(2004),  NET_REGIST_ERR(2011),  NET_ALLOCT_ERR(2012),  NET_INTERUPT(2013),  NET_LIVETIMEOUT(2014),  PEER_OFFLINE(2100),  PEER_ERRSECRET(2101),  PEER_NORIGHT(2102),  CFGERR(3000),  CFGERR_ENCR(3001),  CFGERR_SAVE(3002),  CFGERR_LOAD(3003),  CFGERR_DECR(3004),  CFGERR_PARSE(3005),  CFGERR_TIMEOUT(3006),  CFGERR_AUTH(3007),  CFGERR_SETERR(3008),  LOCAL_IP_ERR(9999);
	  
	  private int value;
	  
	  private RvsError(int val)
	  {
	    this.value = val;
	  }
	  
	  public int intValue()
	  {
	    return this.value;
	  }
	  
	  public static RvsError valueOfInt(int val)
	  {
	    switch (val)
	    {
	    case 0: 
	      return SUCESS;
	    case 1: 
	      return PAREMETER_ERR;
	    case 2: 
	      return DECRESPONSE_ERR;
	    case 3: 
	      return NONEXIST;
	    case 1000: 
	      return AUTHERERR;
	    case 1001: 
	      return AUTHER_PARMETER_ERR;
	    case 1002: 
	      return AUTHER_VERIFYCODE_ERR;
	    case 1003: 
	      return AUTHER_APPID_PAR_ERR;
	    case 1004: 
	      return AUTHER_APPID_FULL_ERR;
	    case 1005: 
	      return AUTHER_LICENSE_USED_ERR;
	    case 1007: 
	      return AUTHER_USRINF_ERR;
	    case 1008: 
	      return AUTHER_REQST_FREQUENTLY;
	    case 1009: 
	      return AUTHER_GETCHECHCODE_ERR;
	    case 1010: 
	      return AUTHER_DEVCOMPANY_ERR;
	    case 1011: 
	      return AUTHER_DEVAPP_ERR;
	    case 1012: 
	      return AUTHER_DEV_NOTREGST;
	    case 1105: 
	      return AUTHER_EMAIL_HAVEBIND;
	    case 1106: 
	      return AUTHER_EMAIL_HAVEEXIS;
	    case 1107: 
	      return AUTHER_MOBILE_HAVEEXIST;
	    case 1108: 
	      return AUTHER_MOBILE_HAVEBIND;
	    case 1109: 
	      return AUTHER_VIVERCID_EXIST;
	    case 1110: 
	      return AUTHER_STREAMCID_EXIST;
	    case 1200: 
	      return AUTHER_COMPANY_NOTEXIST;
	    case 1201: 
	      return AUTHER_APPID_NOTEXIST;
	    case 1202: 
	      return AUTHER_LICENSE_NOTEXIST;
	    case 1203: 
	      return AUTHER_VIEWERCID_NOTEXIST;
	    case 1204: 
	      return AUTHER_STREAMCID_NOTEXIST;
	    case 1205: 
	      return AUTHER_PRODUCT_NOTEXIST;
	    case 1206: 
	      return AUTHER_USR_NOTEXIST;
	    case 1207: 
	      return AUTHER_SESSION_NOTEXIST;
	    case 1208: 
	      return AUTHER_RESISTQUERYFAILL;
	    case 1299: 
	      return AUTHER_UNKNON;
	    case 1301: 
	      return USR_LISTNODE_FULL;
	    case 1302: 
	      return USR_SIGN_AGAIN;
	    case 1303: 
	      return USR_AVS_EXISTED;
	    case 1304: 
	      return USR_SESSION_NOTEXIST;
	    case 1305: 
	      return USR_ACCOUNT_NOTEXIST;
	    case 1306: 
	      return USR_LIST_NOTEXIST;
	    case 1307: 
	      return USR_TASKID_NOTEXIST;
	    case 1308: 
	      return USR_VERSION_MUSTUPDATE;
	    case 1401: 
	      return INF_AVS_HAVEBINDBYOUTHER;
	    case 1402: 
	      return INF_AVS_DSTHAVECLOUD;
	    case 1403: 
	      return INF_AVS_SRCONCLOUD;
	    case 1412: 
	      return INF_AVS_ENFORCE_BIND;
	    case 1413: 
	      return INF_STREAMER_NOTEXIST;
	    case 1414: 
	      return INF_VIEWER_NOTEXIST;
	    case 1415: 
	      return INF_STREAMER_CAP_NOTEXIST;
	    case 1416: 
	      return INF_VIEWER_CAP_NOTEXIST;
	    case 1417: 
	      return INF_STREAMER_SUP_NOTEXIST;
	    case 1418: 
	      return INF_VIEWER_SUP_NOTEXIST;
	    case 1419: 
	      return INF_STREAMER_BUS_NOTEXIST;
	    case 1420: 
	      return INF_VIEWER_BUS_NOTEXIST;
	    case 1421: 
	      return INF_STREAMER_SERVICE_NOTEXIST;
	    case 1422: 
	      return INF_VIEWER_SERVICE_NOTEXIST;
	    case 1999: 
	      return SERVER_SYSTERM_ERR;
	    case 2000: 
	      return NET_ERR;
	    case 2001: 
	      return NET_SOCKET_ERR;
	    case 2002: 
	      return NET_TIMEOUT;
	    case 2003: 
	      return NET_CONNETERR;
	    case 2004: 
	      return NET_RCVDATAERR;
	    case 2011: 
	      return NET_REGIST_ERR;
	    case 2012: 
	      return NET_ALLOCT_ERR;
	    case 2013: 
	      return NET_INTERUPT;
	    case 2014: 
	      return NET_LIVETIMEOUT;
	    case 2100: 
	      return PEER_OFFLINE;
	    case 2101: 
	      return PEER_ERRSECRET;
	    case 2102: 
	      return PEER_NORIGHT;
	    case 3000: 
	      return CFGERR;
	    case 3001: 
	      return CFGERR_ENCR;
	    case 3002: 
	      return CFGERR_SAVE;
	    case 3003: 
	      return CFGERR_LOAD;
	    case 3004: 
	      return CFGERR_DECR;
	    case 3005: 
	      return CFGERR_PARSE;
	    case 3006: 
	      return CFGERR_TIMEOUT;
	    case 3007: 
	      return CFGERR_AUTH;
	    case 3008: 
	      return CFGERR_SETERR;
	    case 9999: 
	      return LOCAL_IP_ERR;
	    }
	    return UNKNOW;
	  }
}
