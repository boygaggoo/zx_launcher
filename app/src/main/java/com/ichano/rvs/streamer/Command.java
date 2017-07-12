package com.ichano.rvs.streamer;

import com.ichano.cbp.CbpMessage;
import com.ichano.cbp.CbpSys;
import com.ichano.cbp.CbpSysCb;
import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.jni.InternalCommand;
import com.ichano.rvs.jni.InternalCommand.CommonCommand;
import com.ichano.rvs.jni.InternalCommand.StreamerFileCommand.Get;
import com.ichano.rvs.jni.InternalCommand.StreamerFileCommand.PartFileInfo;
import com.ichano.rvs.jni.InternalCommand.StreamerFileCommand.Send;
import com.ichano.rvs.jni.NativeCommand;
import com.ichano.rvs.jni.NativeCommand.InternalDataReceiverListener;
import com.ichano.rvs.streamer.callback.CommandCallback;
import com.ichano.rvs.streamer.callback.CustomCommandListener;
import com.ichano.rvs.streamer.callback.CustomDataReceiveListener;
import com.ichano.rvs.streamer.callback.CustomDataRecvCallback;
import com.ichano.rvs.streamer.callback.SwtichColorModeCmdCallback;
import com.ichano.rvs.streamer.exception.IllegalCommandIDException;
import com.ichano.rvs.streamer.exception.IllegalParamException;
import java.util.ArrayList;
import java.util.Arrays;

public final class Command implements CbpSysCb {
	private static final String TAG = "Command";
	public static final int PTZMOVECTRL_PTZ = 0;
	public static final int PTZMOVECTRL_MOVE = 1;
	private static Command instance;
	private NativeCommand nativeCommand;
	private CommandCallback callback;
	private SwtichColorModeCmdCallback colormodeCmdCallback;
	private CustomCommandListener internalCommandListener;
	public static final int PART_FILE_LENGTH = 2048;

	private Command() {
		this.nativeCommand = NativeCommand.getInstance();

		this.nativeCommand.setInternalDataReceiverListener(this.internalDataReceiverListener);
	}

	void setInternalCommandListener(CustomCommandListener listener) {
		this.internalCommandListener = listener;
		this.nativeCommand.setInternalCommandListener(this.internalCommandListener);
	}

	static Command getInstance() {
		if (instance == null) {
			instance = new Command();
		}
		return instance;
	}

	NativeCommand getNativeCommand() {
		return this.nativeCommand;
	}

	public void setCallback(CommandCallback callback) {
		this.callback = callback;
	}

	public void setColormodeCmdCallback(SwtichColorModeCmdCallback callback) {
		this.colormodeCmdCallback = callback;
	}

	@Deprecated
	public void setCustomDataRecvCallback(CustomDataRecvCallback callback) {
		this.nativeCommand.setCustomDataRecvCallback(callback);
	}

	@Deprecated
	public boolean sendCustomData(long remoteCID, byte[] pucData) {
		return this.nativeCommand.sendCustomData(remoteCID, pucData) == 0;
	}

	public boolean sendCustomData(long remoteCid, String dataDescription, byte[] data) {
		if (dataDescription == null) {
			throw new IllegalParamException("dataDescription should not be null!");
		}
		if (dataDescription.length() > 128) {
			throw new IllegalParamException("dataDescription's length should <= 128");
		}
		if (data == null) {
			throw new IllegalParamException("data should not be null!");
		}
		int end = 0;
		int start = 0;
		int len = data.length;
		boolean ret = true;
		int totalPart = len / 2048 + (len % 2048 == 0 ? 0 : 1);
		RvsLog.i(Command.class, "sendCustomData()", "file length=" + len + ", totalPart=" + totalPart);
		for (int i = 0; i < totalPart; i++) {
			if (i == totalPart - 1) {
				end = len;
			} else {
				end += 2048;
			}
			byte[] part = Arrays.copyOfRange(data, start, end);
			boolean success = sendInternalCustomData(remoteCid, 3, i, totalPart, dataDescription, part, null);
			start += 2048;
			if (!success) {
				ret = false;
			}
		}
		return ret;
	}

	private ArrayList<CustomDataReceiveListener> onCustomDataReceiveListeners = new ArrayList();

	@Deprecated
	public void setOnCustomDataReceiveListener(CustomDataReceiveListener listener) {
		if (listener != null) {
			this.onCustomDataReceiveListeners.add(listener);
		}
	}

	public void addCustomDataReceiveListener(CustomDataReceiveListener listener) {
		if (listener != null) {
			this.onCustomDataReceiveListeners.add(listener);
		}
	}

	public void removeCustomDataReceiveListener(CustomDataReceiveListener listener) {
		if (this.onCustomDataReceiveListeners.contains(listener)) {
			this.onCustomDataReceiveListeners.remove(listener);
		}
	}

	@Deprecated
	public void setOnCustomCommandListener(CustomCommandListener listener) {
		this.nativeCommand.addUserCustomCommandListener(listener);
	}

	public void addCustomCommandListener(CustomCommandListener listener) {
		this.nativeCommand.addUserCustomCommandListener(listener);
	}

	public void removeCustomCommandListener(CustomCommandListener listener) {
		this.nativeCommand.removeUserCustomCommandListener(listener);
	}

	private ArrayList<ReceiveFileDataInfo> fileDataList = new ArrayList();

	private ReceiveFileDataInfo addFileData(long remoteCid, InternalCommand.StreamerFileCommand.PartFileInfo info) {
		boolean isNewPart = true;
		ReceiveFileDataInfo r = null;
		for (int i = 0; i < this.fileDataList.size(); i++) {
			r = (ReceiveFileDataInfo) this.fileDataList.get(i);
			if ((r.remoteCid == remoteCid) && (r.fileDiscription.equals(info.fileDiscription))) {
				r.parts.add(info);
				isNewPart = false;
				break;
			}
		}
		if (isNewPart) {
			r = new ReceiveFileDataInfo(remoteCid, info.fileDiscription, info);
			this.fileDataList.add(r);
		}
		return r;
	}

	private void removeFileData(long remoteCid, InternalCommand.StreamerFileCommand.PartFileInfo info) {
		ReceiveFileDataInfo r = null;
		for (int i = 0; i < this.fileDataList.size(); i++) {
			r = (ReceiveFileDataInfo) this.fileDataList.get(i);
			if ((r.remoteCid == remoteCid) && (r.fileDiscription.equals(info.fileDiscription))) {
				this.fileDataList.remove(r);
				r = null;
				break;
			}
		}
	}

	private NativeCommand.InternalDataReceiverListener internalDataReceiverListener = new NativeCommand.InternalDataReceiverListener() {
		public void onReceiveCustomData(long remoteCID, long commandID, String commandHeader, byte[] data) {
			if (3L == commandID) {
				RvsLog.i(Command.class, "InternalDataReceiverListener : onReceiveCustomData()",
						"get part file, header= :" + commandHeader);
				InternalCommand.StreamerFileCommand.PartFileInfo info = InternalCommand.StreamerFileCommand.Get
						.getFile(commandHeader, data);
				Command.ReceiveFileDataInfo r = Command.this.addFileData(remoteCID, info);
				if (r == null) {
					return;
				}
				if (info.index == info.totalPart - 1) {
					byte[] out = null;
					boolean getAllPart = true;
					byte[] temp;
					for (int i = 0; i < info.totalPart; i++) {
						if (r.parts.get(i) == null) {
							getAllPart = false;
							break;
						}
						if (out == null) {
							out = ((InternalCommand.StreamerFileCommand.PartFileInfo) r.parts.get(i)).fileData;
						} else {
							temp = ((InternalCommand.StreamerFileCommand.PartFileInfo) r.parts.get(i)).fileData;
							byte[] data2 = new byte[out.length + temp.length];
							System.arraycopy(out, 0, data2, 0, out.length);
							System.arraycopy(temp, 0, data2, out.length, temp.length);

							out = data2;
						}
					}
					if (getAllPart) {
						for (CustomDataReceiveListener l : Command.this.onCustomDataReceiveListeners) {
							if (l != null) {
								l.onReceiveCustomData(remoteCID, info.fileDiscription, out);
							}
						}
						RvsLog.i(Command.class, "InternalDataReceiverListener : onReceiveCustomData()",
								"get complete file.");
					} else {
						for (CustomDataReceiveListener l : Command.this.onCustomDataReceiveListeners) {
							if (l != null) {
								l.onReceiveCustomData(remoteCID, info.fileDiscription, null);
							}
						}
						RvsLog.i(Command.class, "InternalDataReceiverListener : onReceiveCustomData()",
								"get file error.");
					}
					Command.this.removeFileData(remoteCID, info);
				}
			}
		}
	};

	private class ReceiveFileDataInfo {
		public long remoteCid;
		public String fileDiscription;
		public ArrayList<InternalCommand.StreamerFileCommand.PartFileInfo> parts = new ArrayList();

		public ReceiveFileDataInfo(long remoteCid, String fileDiscription,
				InternalCommand.StreamerFileCommand.PartFileInfo partFile) {
			this.remoteCid = remoteCid;
			this.fileDiscription = fileDiscription;
			this.parts.add(partFile);
		}
	}

	public boolean sendCustomCommand(long remoteCID, int commandID, String command) {
		if (commandID <= 1000) {
			throw new IllegalCommandIDException("commandID must > 1000.");
		}
		return sendInternalCustomCommand(remoteCID, commandID, command);
	}

	boolean sendInternalCustomCommand(long remoteCID, int commandID, String command) {
		String commandHead = InternalCommand.CommonCommand.getCommandHeadString(commandID, 0);
		if (commandHead == null) {
			RvsLog.e(Command.class, "sendCustomCommand()",
					"getCommandHeadString() : can not cust command head to json.");
			return false;
		}
		byte[] head = new byte[512];
		byte[] src = commandHead.getBytes();
		System.arraycopy(src, 0, head, 0, src.length);

		byte[] cmd = command.getBytes();
		byte[] commandByte = new byte[512 + cmd.length];
		System.arraycopy(head, 0, commandByte, 0, head.length);
		System.arraycopy(cmd, 0, commandByte, 512, cmd.length);

		return this.nativeCommand.sendCustomData(remoteCID, commandByte) == 0;
	}

	public boolean sendCustomCommand(long remoteCID, int commandID, String command, CustomCommandListener listener) {
		if (commandID <= 1000) {
			throw new IllegalCommandIDException("commandID must > 1000.");
		}
		String commandHead = InternalCommand.CommonCommand.getCommandHeadString(commandID, 0);
		if (commandHead == null) {
			RvsLog.e(Command.class, "sendCustomCommand()",
					"getCommandHeadString() : can not cust command head to json.");
			return false;
		}
		byte[] head = new byte[512];
		byte[] src = commandHead.getBytes();
		System.arraycopy(src, 0, head, 0, src.length);

		byte[] cmd = command.getBytes();
		byte[] commandByte = new byte[512 + cmd.length];
		System.arraycopy(head, 0, commandByte, 0, head.length);
		System.arraycopy(cmd, 0, commandByte, 512, cmd.length);
		return this.nativeCommand.sendCustomData(remoteCID, commandByte, commandID, listener) == 0;
	}

	boolean sendInternalCommand(long remoteCID, int commandID, int commandType, byte[] cmd,
			CustomCommandListener listener) {
		String commandHead = InternalCommand.CommonCommand.getCommandHeadString(commandID, commandType);
		if (commandHead == null) {
			RvsLog.e(Command.class, "sendCustomCommand()",
					"sendInternalCommand() : can not cust command head to json.");
			return false;
		}
		byte[] head = new byte[512];
		byte[] src = commandHead.getBytes();
		System.arraycopy(src, 0, head, 0, src.length);

		byte[] commandByte = new byte[512 + cmd.length];
		System.arraycopy(head, 0, commandByte, 0, head.length);
		System.arraycopy(cmd, 0, commandByte, 512, cmd.length);
		return this.nativeCommand.sendCustomData(remoteCID, commandByte, commandID, listener) == 0;
	}

	boolean sendInternalCustomData(long remoteCID, int commandID, int partIndex, int totalParts, String dataDescription,
			byte[] cmd, CustomCommandListener listener) {
		String commandHead = InternalCommand.StreamerFileCommand.Send.getSendCommandHeader(totalParts, partIndex,
				dataDescription);
		if (commandHead == null) {
			RvsLog.e(Command.class, "sendInternalCustomData()",
					"sendInternalCommand() : can not cust command head to json.");
			return false;
		}
		byte[] head = new byte[512];
		byte[] src = commandHead.getBytes();
		System.arraycopy(src, 0, head, 0, src.length);

		byte[] commandByte = new byte[512 + cmd.length];
		System.arraycopy(head, 0, commandByte, 0, head.length);
		System.arraycopy(cmd, 0, commandByte, 512, cmd.length);
		return this.nativeCommand.sendCustomData(remoteCID, commandByte, commandID, listener) == 0;
	}

	public void submitProcessResult(long remoteCID, long msgId, int msgType, ResultCode resultCode) {
		this.nativeCommand.submitResult(remoteCID, msgId, msgType, resultCode.intValue());
	}

	boolean init() {
		RvsLog.i(Command.class, "init()", "sdk cmd init");
		int ret = this.nativeCommand.init();
		if (ret != 0) {
			return false;
		}
		CbpSys.registerCallBack(6, this);
		return true;
	}

	void destroy() {
		CbpSys.unregisterCallBack(6, this);

		this.nativeCommand.setInternalDataReceiverListener(null);

		this.nativeCommand.destroy();
	}

	/**
	 * @deprecated
	 */
	public int onRecvMsg(CbpMessage uspMsg) {
		RvsLog.i(Command.class, "onRecvMsg()", uspMsg.toString());
		switch (uspMsg.getSrcPid()) {
		case 6:
			if (this.callback == null) {
				return 0;
			}
			handleCmdMsg(uspMsg);

			break;
		default:
			RvsLog.w(Command.class, "onRecvMsg()", "unkonwn msg from source id: " + uspMsg.getSrcPid());
		}
		return 0;
	}

	private void handleCmdMsg(CbpMessage uspMsg) {
		
		long remoteCid = 0L;
		long msgId = 0L;
		int msgType = 0;
		int type = 0;
		
		switch (uspMsg.getMsgID()) {
		case 0:
			String userName = uspMsg.getStr(200);
			String password = uspMsg.getStr(201);
			this.callback.onSetUserInfo(userName, password);

			break;
		case 110:
			 remoteCid = uspMsg.getXXLSIZE(0, 0L);
			 msgId = uspMsg.getXXLSIZE(2, 0L);
			 msgType = uspMsg.getUI(1, 0);
			int camId = uspMsg.getUI(100, 0);
			int streamId = uspMsg.getUI(101, 0);
			int frameRate = uspMsg.getUI(300, 0);
			int bitrate = uspMsg.getUI(301, 0);
			int streamQuality = uspMsg.getUI(302, 0);
			int iframeInterval = uspMsg.getUI(303, 0);

			this.callback.onSetStreamQuality(remoteCid, msgId, msgType, camId, streamId, frameRate, bitrate,
					streamQuality, iframeInterval);

			break;
		case 140:
			 remoteCid = uspMsg.getXXLSIZE(0, 0L);
			 msgId = uspMsg.getXXLSIZE(2, 0L);
			 msgType = uspMsg.getUI(1, 0);
			this.callback.onSwitchFrontRearCamera(remoteCid, msgId, msgType);

			break;
		case 150:
			 remoteCid = uspMsg.getXXLSIZE(0, 0L);
			 msgId = uspMsg.getXXLSIZE(2, 0L);
			 msgType = uspMsg.getUI(1, 0);
			this.callback.onSwitchTorch(remoteCid, msgId, msgType);

			break;
		case 230:
			if (this.colormodeCmdCallback != null) {
				 remoteCid = uspMsg.getXXLSIZE(0, 0L);
				 msgId = uspMsg.getXXLSIZE(2, 0L);
				 msgType = uspMsg.getUI(1, 0);
				this.colormodeCmdCallback.onSwtichColorMode(remoteCid, msgId, msgType);
			}
			break;
		case 220:
			 remoteCid = uspMsg.getXXLSIZE(0, 0L);
			 type = uspMsg.getUI(800, 0);
			 camId = uspMsg.getUI(100, 0);

			int px = uspMsg.getUI(801, 0);
			int ty = uspMsg.getUI(802, 0);
			int zz = uspMsg.getUI(803, 0);

			this.callback.onPTZorMove(remoteCid, camId, type, px, ty, zz);

			break;
		default:
			RvsLog.w(Command.class, "handleCmdMsg()", "no case msg type: " + uspMsg.getMsgID());
		}
	}

	public static enum ResultCode {
		OK(0), ERR(30001), INVALID_PARAM(30002), UNKNOWN(30003), UNSUPPORT(30004);

		private int value;

		private ResultCode(int val) {
			this.value = val;
		}

		public int intValue() {
			return this.value;
		}
	}
}
