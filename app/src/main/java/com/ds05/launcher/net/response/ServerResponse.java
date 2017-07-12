package com.ds05.launcher.net.response;

import com.ds05.launcher.net.ResponseMsg;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * @author : Kabruce 
 * @date ：2015年6月17日 下午1:26:36 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public class ServerResponse implements ResponseMsg {

	private static final Logger logger = LoggerFactory.getLogger(ResponseMsg.class);

	protected MsgBodyWrap output = MsgBodyWrap.newInstance4Out();
	private String msgCode;
	/**必须调用此方法设置消息号*/
	public ServerResponse(String msgCode) {
		setMsgCode(msgCode);
	}

	public void setMsgCode(String code) {
		msgCode = code;
	}

	public IoBuffer entireMsg() {
		
			byte[] body = output.toByteArray();
			/* 标志 byte 长度short */
			int length = body.length ; //MsgProtocol.flagSize+MsgProtocol.lengthSize+MsgProtocol.msgCodeSize+ body.length;
			
			logger.info("length = "+length);
			IoBuffer buf = IoBuffer.allocate(length);
//			buf.put(MsgProtocol.defaultFlag);//flag
//			buf.putInt(body.length+MsgProtocol.msgCodeSize);//lengh
//			buf.putInt(msgCode);
			buf.put(body);
			buf.flip();
//			byte[] msgBytes=message.toString().getBytes();
//			IoBuffer buf = IoBuffer.allocate(msgBytes.length);  
//		    buf.put(msgBytes);
//		    buf.flip();  

			return buf;
	}

	/**
	 * 释放资源(数据流、对象引用)
	 */
	public void release() {
		if (output != null) {
			output.close();
		}
		output = null;
	}


}
