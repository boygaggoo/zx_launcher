/**
 * 
 */
package com.ds05.launcher.net;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kabruce
 *
 */
public class MsgDecoder extends CumulativeProtocolDecoder {

	private static final Logger logger = LoggerFactory.getLogger(MsgDecoder.class);
	private final Charset charset;

	public Charset getCharset() {
		return charset;
	}

	public MsgDecoder() {
		this(Charset.forName("utf-8"));
	}

	public MsgDecoder(Charset charset) {
		this.charset = charset;
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		logger.info("start receive msg...");
		// 初始位置
		int start = in.position();
		int left = -1;
		// 查找'[' ']'标记

		while (in.hasRemaining()) {
			byte current = in.get();
			if (current == '[') {
				left = in.position();
			}
			// 找到了\r\n
			if (current == ']' && left >= 0) {
				// Remember the current position and limit.
				int position = in.position();
				int limit = in.limit();
				try {
					in.position(left);
					in.limit(position);// 设置当前的位置为limit
					// position和limit之间是一个完整的CRLF消息
					String msg = new String(in.slice().array(), left - 1, position - left + 1, charset);
					out.write(msg);
					// 调用slice方法获得positon和limit之间的子缓冲区->调用write方法加入消息队列(因为网络层一个包可能有多个完整消息)->后经调用flush(遍历消息队列的消息)->nextFilter.messageReceived
					// filter
				} finally {
					// 设置position为解码后的position.limit设置为旧的limit
					in.position(position);
					in.limit(limit);
				}

				// 直接返回true.因为在父类的decode方法中doDecode是循环执行的直到不再有完整的消息返回false.
				logger.info("end receive msg...");
				return true;
			}

		}

		// 没有找到\r\n,则重置position并返回false.使得父类decode->for跳出break.
		in.position(start);

		return false;
	}

}
