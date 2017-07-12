package com.ichano.rvs.jni;

import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.streamer.callback.CustomCommandListener;
import com.ichano.rvs.streamer.callback.CustomDataRecvCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class NativeCommand {
	
	private static NativeCommand instance;
	private CustomDataRecvCallback customDataRcvCallback;

	public static NativeCommand getInstance() {
		if (instance == null) {
			instance = new NativeCommand();
		}
		return instance;
	}

	public void setCustomDataRecvCallback(CustomDataRecvCallback callback) {
		this.customDataRcvCallback = callback;
	}

	public native int init();

	public native int destroy();

	public native int submitResult(long paramLong1, long paramLong2, int paramInt1, int paramInt2);

	public native int sendCustomData(long paramLong, byte[] paramArrayOfByte);

	private native int setOnRecvCustomDataCB();

	private void onRecvCustomData(long remoteCID, byte[] data) {
		
		RvsLog.i(NativeCommand.class, "onRecvCustomData()","data LENGTH = " + data.length);
		
		boolean isInternalCommand = false;
		String cmd = null;
		for (int i = 0; i < data.length; i++) {
			if (data[i] == 0) {
				break;
			}
			if(i==512){
				byte[] header = Arrays.copyOf(data, i);
				cmd = new String(header);
			}else{
				i=0;
			}
			
		}
		
		
		//byte[] header = Arrays.copyOf(data, i);

		boolean newCommand = false;
		int commandId;
		try {
			JSONObject json = new JSONObject(cmd);
			commandId = json.getInt("zy_command_id");
			int commandType = json.getInt("zy_command_type");
			RvsLog.i(NativeCommand.class, "onRecvCustomData()","commandID = " + commandId + ", commandType = " + commandType);

			byte[] commandByte = Arrays.copyOfRange(data, 512, data.length);
			if (InternalCommand.isInternalCommand(commandId)) {
				isInternalCommand = true;
			}
			if ((isInternalCommand) && (this.internalCommandListener != null) && (commandType == 0)) {
				this.internalCommandListener.onCustomCommandListener(remoteCID, commandId, new String(commandByte));
				RvsLog.i(NativeCommand.class, "onRecvCustomData()", "InternalCommand.COMMAND_TYPE_STRING");
				return;
			}
			if ((isInternalCommand) && (this.internalDataReceiverListener != null) && (1 == commandType)) {
				this.internalDataReceiverListener.onReceiveCustomData(remoteCID, commandId, cmd, commandByte);
				RvsLog.i(NativeCommand.class, "onRecvCustomData()", "InternalCommand.COMMAND_TYPE_BYTE");
				return;
			}
			if (isInternalCommand) {
				return;
			}
			RvsLog.i(NativeCommand.class, "onRecvCustomData()", "custom command begin...");
			if (this.customDataRcvCallback != null) {
				this.customDataRcvCallback.onCustomCommandListener(remoteCID, commandId, new String(commandByte));
				RvsLog.i(NativeCommand.class, "onRecvCustomData()", "customDataRcvCallback.onCustomCommandListener()");
			}
			CustomCommandListener l = (CustomCommandListener) this.commandMap.get(Integer.valueOf(commandId));
			if (l != null) {
				l.onCustomCommandListener(remoteCID, commandId, new String(commandByte));
				RvsLog.i(NativeCommand.class, "onRecvCustomData()", "specified CustomCommandListener");
			}
			for (CustomCommandListener listener : this.onUserCustomCommandListeners) {
				if (listener != null) {
					listener.onCustomCommandListener(remoteCID, commandId, new String(commandByte));
					RvsLog.i(NativeCommand.class, "onRecvCustomData()", "onUserCustomCommandListener");
				}
			}
			newCommand = true;
		} catch (JSONException e) {
			e.printStackTrace();
			RvsLog.e(NativeCommand.class, "onRecvCustomData()", "not new command");
			if (this.customDataRcvCallback != null) {
				this.customDataRcvCallback.onCustomCommandListener(remoteCID, -1, cmd);
			}
		}
		if (this.customDataRcvCallback != null) {
			this.customDataRcvCallback.onReceiveCustomData(remoteCID, data);
			RvsLog.i(NativeCommand.class, "onRecvCustomData()", "customDataRcvCallback.onReceiveCustomData()");
		} else {
			RvsLog.i(NativeCommand.class, "onRecvCustomData()", "customDataRcvCallback == null");
		}
		if (!newCommand) {
			this.customDataRcvCallback.onCustomCommandListener(remoteCID, -1, new String(data));
			for (CustomCommandListener listener : this.onUserCustomCommandListeners) {
				if (listener != null) {
					listener.onCustomCommandListener(remoteCID, -1, new String(data));
				}
			}
			RvsLog.i(NativeCommand.class, "onRecvCustomData()", "old command: onUserCustomCommandListener");
		}
	}

	private static final String TAG = NativeCommand.class.getSimpleName();
	private static final int NO_COMMAND_ID = -1;
	private HashMap<Integer, CustomCommandListener> commandMap = new HashMap();
	private CustomCommandListener internalCommandListener;

	public int sendCustomData(long remoteCID, byte[] pucData, int commandID, CustomCommandListener listener) {
		if (this.commandMap.get(Integer.valueOf(commandID)) != null) {
			this.commandMap.remove(Integer.valueOf(commandID));
		}
		if (listener != null) {
			this.commandMap.put(Integer.valueOf(commandID), listener);
		}
		return sendCustomData(remoteCID, pucData);
	}

	public void setInternalCommandListener(CustomCommandListener listener) {
		this.internalCommandListener = listener;
	}

	private ArrayList<CustomCommandListener> onUserCustomCommandListeners = new ArrayList();
	private InternalDataReceiverListener internalDataReceiverListener;

	public void addUserCustomCommandListener(CustomCommandListener listener) {
		if (listener != null) {
			this.onUserCustomCommandListeners.add(listener);
		}
	}

	public void removeUserCustomCommandListener(CustomCommandListener listener) {
		if (this.onUserCustomCommandListeners.contains(listener)) {
			this.onUserCustomCommandListeners.remove(listener);
		}
	}

	public void setInternalDataReceiverListener(InternalDataReceiverListener listener) {
		this.internalDataReceiverListener = listener;
	}

	public static abstract interface InternalDataReceiverListener {
		public abstract void onReceiveCustomData(long paramLong1, long paramLong2, String paramString,byte[] paramArrayOfByte);
	}
}
