package com.ds05.launcher.net;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsgEncoder extends ProtocolEncoderAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MsgEncoder.class);
	private final Charset charset;

	public Charset getCharset() {
		return charset;
	}

	public MsgEncoder() {
		this(Charset.forName("utf-8"));
	}

	public MsgEncoder(Charset charset) {
		this.charset = charset;

	}

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		
		ResponseMsg value = (ResponseMsg) message;
		logger.info(value.entireMsg().getHexDump());
		out.write(value.entireMsg());
		value.release();
	}
}
