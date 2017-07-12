package com.ds05.launcher.net;

import org.apache.mina.core.buffer.IoBuffer;

/** 
 * @author : Kabruce 
 * @date ：2015年6月17日 下午1:36:09 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public interface ResponseMsg {
	/**
	 * 设置消息号
	 * @param code
	 */
	public void setMsgCode(String code);

	/**
	 * 返回消息的整体封包
	 * @return
	 */
	public IoBuffer entireMsg();
	

	/**
	 * 释放资源(数据流、对象引用)
	 */
	public void release();
}
