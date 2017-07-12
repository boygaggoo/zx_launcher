package com.ichano.rvs.streamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Build.VERSION;
import com.ichano.cbp.CbpMessage;
import com.ichano.cbp.CbpSys;
import com.ichano.cbp.CbpSysCb;
import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.jni.InternalCommand;
import com.ichano.rvs.jni.InternalCommand.StreamerLocationCommand.Send;
import com.ichano.rvs.jni.NativeAuth;
import com.ichano.rvs.jni.NativeBase;
import com.ichano.rvs.jni.NativeBase.NativeCrashListener;
import com.ichano.rvs.jni.NativeDetect;
import com.ichano.rvs.jni.NativeDeviceInfo;
import com.ichano.rvs.streamer.bean.RvsAlarmRecordInfo;
import com.ichano.rvs.streamer.callback.CustomCommandListener;
import com.ichano.rvs.streamer.callback.LocationReqListener;
import com.ichano.rvs.streamer.callback.MotionDetectCallback;
import com.ichano.rvs.streamer.callback.MotionDetectSettingsCallback;
import com.ichano.rvs.streamer.callback.PayStatusCallback;
import com.ichano.rvs.streamer.callback.StreamerCallback;
import com.ichano.rvs.streamer.constant.AuthState;
import com.ichano.rvs.streamer.constant.LoginError;
import com.ichano.rvs.streamer.constant.LoginState;
import com.ichano.rvs.streamer.constant.MotionDetectState;
import com.ichano.rvs.streamer.constant.RemoteViewerState;
import com.ichano.rvs.streamer.constant.RvsError;
import com.ichano.rvs.streamer.constant.RvsSessionState;
import com.ichano.rvs.streamer.param.Capacity;
import com.ichano.rvs.streamer.util.AppUtil;
import com.ichano.rvs.streamer.util.AvsPersistTool;
import com.ichano.rvs.streamer.util.LogUtil;
import com.ichano.rvs.streamer.util.NetUtil;

public final class Streamer implements CbpSysCb, NativeBase.NativeCrashListener {
	private static final String TAG = "Streamer";
	private static final String VERSION = "4.3.1";
	private Context appContext;
	private static Streamer instance;
	private StreamerCallback callback;
	private Command command;
	private Media media;
	private NativeBase nativeBase;
	private NativeAuth nativeAuth;
	private NativeDeviceInfo nativeDevice;
	private NativeDetect nativeDetect;
	private String appVersion;
	private String cachePath;
	private String configPath;
	private boolean registRecerver;
	private PayStatusCallback payStatusCallback;
	private boolean wrongPackage = false;
	private LocationReqListener locationReqListener;

	private Streamer() {
		this.media = Media.getInstance();
		this.command = Command.getInstance();
		this.nativeBase = new NativeBase();
		this.nativeAuth = new NativeAuth();
		this.nativeDevice = NativeDeviceInfo.getInstance();
		this.nativeDetect = new NativeDetect();

		this.nativeBase.setNativeCrashListener(this);

		this.command.setInternalCommandListener(this.internalCommandLister);
	}

	public static Streamer getStreamer() {
		if (instance == null) {
			instance = new Streamer();
		}
		return instance;
	}

	public Media getMedia() {
		return this.media;
	}

	public Command getCommand() {
		return this.command;
	}

	public boolean init(Context context, String appVersion, String configPath, String cachePath) {
		return init(context, appVersion, configPath, cachePath, null);
	}

	public boolean init(Context context, String appVersion, String configPath, String cachePath,
			String persistentPath) {
		this.appContext = context;
		this.appVersion = appVersion;
		this.configPath = configPath;
		this.cachePath = cachePath;

		AvsPersistTool.init(context, persistentPath);
		RvsLog.i(Streamer.class, "init()", "sdk sysInit");
		int ret = -1;

		ret = this.nativeBase.sysInit(context);
		if (ret != 0) {
			return false;
		}
		if (!this.registRecerver) {
			this.appContext.getApplicationContext().registerReceiver(this.connectionChangeReceiver,
					new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
			this.registRecerver = true;
		}
		RvsLog.i(Streamer.class, "init()", "sdk init success");
		return true;
	}

	public String getRvsVersion() {
		return "4.3.1" + this.nativeBase.getSDKVersion();
	}

	public void destroy() {
		if (this.registRecerver) {
			try {
				this.appContext.getApplicationContext().unregisterReceiver(this.connectionChangeReceiver);
			} catch (Exception e) {
				RvsLog.e(Streamer.class, "destroy()", "unregisterReceiver fail:" + e.getMessage());
			}
			this.registRecerver = false;
		}
		this.command.destroy();
		this.media.destroy();

		this.nativeAuth.destroy();

		this.nativeDetect.destroy();

		CbpSys.unregisterCallBack(0, this);
		CbpSys.unregisterCallBack(15, this);
		CbpSys.unregisterCallBack(7, this);
		CbpSys.destroyMsgLoop();

		this.nativeBase.sysDestroy();

		RvsLog.i(Streamer.class, "destroy()", "exit sdk.");
	}

	public void setLoginInfo(String companyid, String companykey, String appid, String license) {
		if (companyid == null) {
			companyid = "";
		}
		if (appid == null) {
			appid = "";
		}
		if (license == null) {
			license = "";
		}
		RvsLog.i(Streamer.class, "setLoginInfo()", "companyid = " + companyid + ", companykey = " + companykey+ ", appid = " + appid + ", license = " + license);

		int hashCode = AvsPersistTool.getLoginInfoHashCode();
		if (hashCode != 0) {
			String str = companyid + license;
			if (hashCode != str.hashCode()) {
				AvsPersistTool.clearAuthCache();
				RvsLog.i(Streamer.class, "setLoginInfo()", "companyid or license changed, clear auth cache.");
				AvsPersistTool.saveLoginInfoHashCode(companyid, license);
			} else {
				RvsLog.i(Streamer.class, "setLoginInfo()", "same companyid and license");
			}
		} else {
			RvsLog.i(Streamer.class, "setLoginInfo()", "auth first time.");
			AvsPersistTool.saveLoginInfoHashCode(companyid, license);
		}
		String pucSymbol = AvsPersistTool.getAvsSymbol();

		int ret = this.nativeAuth.setAuthInfo(this.configPath, this.cachePath, companyid, companykey, appid, license,
				pucSymbol);
		if (999 == ret) {
			this.wrongPackage = true;
		}
		RvsLog.i(Streamer.class, "setLoginInfo()", "sdk msgloop init");
		CbpSys.initMsgLoop();
		CbpSys.registerCallBack(0, this);
		CbpSys.registerCallBack(15, this);
		CbpSys.registerCallBack(7, this);

		RvsLog.i(Streamer.class, "setLoginInfo()", "sdk auth init");
		this.nativeAuth.init();

		RvsLog.i(Streamer.class, "setLoginInfo()", "sdk detect init");
		this.nativeDetect.init();

		int availMem = AppUtil.getAvailMemory(this.appContext);
		RvsLog.i(Streamer.class, "setLoginInfo()", "availMem: " + availMem);
		LogUtil.writeLog("availMem: " + availMem);
		if ((availMem != -1) && (availMem <= 256)) {
			this.nativeDevice.setDeviceAbility(2);
		}
		this.media.init();
		this.command.init();

		setLocalIp(this.appContext);
		this.nativeDevice.setOsVersion("Android-" + Build.VERSION.RELEASE);
		this.nativeDevice.setLanguage(AppUtil.getDefaultRvsLanguage());
	}

	public void setCapacity(Capacity capacity) {
		this.nativeDevice.setRecordMode(capacity.getRecordMode());

		RvsLog.i(Streamer.class, "setCapacity()",
				"RunMode = " + capacity.getRunMode() + ", RecordMode = " + capacity.getRecordMode()
						+ ", TimeZoneMode = " + capacity.getTimeZoneMode() + ", EchoCancelMode = "
						+ capacity.getEchoCancelMode());
	}

	public void login() {
		if ((this.wrongPackage) && (this.callback != null)) {
			this.callback.onLoginResult(LoginState.vauleOfInt(3), 0, LoginError.valueOfInt(999));
			RvsLog.i(Streamer.class, "login()", "wrong Package");
		}
		this.nativeAuth.start();
		RvsLog.i(Streamer.class, "login()", "login");
	}

	public void logout() {
		this.nativeAuth.stop();
		RvsLog.i(Streamer.class, "logout()", "logout");
	}

	public String getCID() {
		String cid = "";
		try {
			cid = String.valueOf(this.nativeDevice.getCid());
		} catch (Exception ex) {
			RvsLog.i(Streamer.class, "getCID()", "cid is not long type.");
		}
		return cid;
	}

	public void setUserNameAndPwd(String username, String password) {
		if (username == null) {
			username = "";
		}
		if (password == null) {
			password = "";
		}
		this.nativeAuth.setOwnSecret(username, password);
	}

	public String[] getUserNameAndPwd() {
		Object[] ret = this.nativeAuth.getOwnSecret();
		if ((ret != null) && (ret.length == 2)) {
			return new String[] { (String) ret[0], (String) ret[1] };
		}
		return null;
	}

	public void setDeviceName(String deviceName) {
		if (deviceName != null) {
			this.nativeDevice.setName(deviceName);
		}
	}

	public String getDeviceName() {
		return this.nativeDevice.getName();
	}

	public int getAvsCloudFlag() {
		return this.nativeDevice.getLocalCloudFlag();
	}

	public void setMaxSessionNum(int maxSessionNum) {
		this.nativeAuth.setMaxSessionNum(maxSessionNum);
		RvsLog.i(Streamer.class, "setMaxSessionNum()", "maxSessionNum = " + maxSessionNum);
	}

	public int getSessionCurNum() {
		return this.nativeAuth.getCurrentSessionNum();
	}

	/**
	 * @deprecated
	 */
	public int getSessionCidList(long[] cidList) {
		return this.nativeAuth.getCurrentSessionCidList(cidList);
	}

	public long[] getSessionCidList() {
		long[] cids = new long[''];
		int count = this.nativeAuth.getCurrentSessionCidList(cids);
		if (count <= 0) {
			return null;
		}
		long[] ret = new long[count];
		System.arraycopy(cids, 0, ret, 0, count);
		return ret;
	}

	public void setCallback(StreamerCallback streamerCallback) {
		this.callback = streamerCallback;
	}

	public void setDebugEnable(boolean debugEnable) {
		this.nativeBase.enableDebug(debugEnable);
		RvsLog.enableLog(debugEnable);
	}

	public void closeLog() {
		this.nativeBase.closeLog();
		RvsLog.enableLog(false);
	}

	public void setLocationReqListener(LocationReqListener l) {
		this.locationReqListener = l;
	}

	private CustomCommandListener internalCommandLister = new CustomCommandListener() {
		public void onCustomCommandListener(long remoteCID, int commandId, String command) {
			RvsLog.i(Streamer.class, "internalCommandLister",
					"remoteCID = " + remoteCID + "commandId = " + remoteCID + "command = " + command);
			if ((1 == commandId) && (Streamer.this.locationReqListener != null)) {
				Streamer.this.locationReqListener.onLocationReqListener(remoteCID);
			}
			if (Streamer.this.fileManagerCustomCmdListener != null) {
				Streamer.this.fileManagerCustomCmdListener.onCustomCommandListener(remoteCID, commandId, command);
			}
		}
	};

	public boolean sendLocation(long remoteCID, double longitude, double latitude) {
		return this.command.sendInternalCommand(remoteCID, 2, 0,
				InternalCommand.StreamerLocationCommand.Send.getCommandString(latitude, longitude).getBytes(), null);
	}

	/**
	 * @deprecated
	 */
	public int onRecvMsg(CbpMessage uspMsg) {
		switch (uspMsg.getSrcPid()) {
		case 0:
			handleBaseMsg(uspMsg);

			break;
		case 15:
			handleLoginMsg(uspMsg);

			break;
		case 7:
			handleDetectMsg(uspMsg);

			break;
		default:
			RvsLog.w(Streamer.class, "onRecvMsg()", "unkonwn msg from source id: " + uspMsg.getSrcPid());
		}
		return 0;
	}

	private void handleBaseMsg(CbpMessage uspMsg) {
		RvsLog.i(Streamer.class, "handleBaseMsg()", "get msg = " + uspMsg.getMsgID());
		if (this.callback == null) {
			return;
		}
		long cid = uspMsg.getXXLSIZE(0, -1L);
		int tag = uspMsg.getUI(1, 0);
		RvsLog.i(Streamer.class, "handleBaseMsg()", "get tag = " + tag);
		int pid = uspMsg.getUI(2, 0);
		switch (tag) {
		case 0:
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_AUTH begin;");
			long updateCid = this.nativeDevice.getCid();
			this.callback.onUpdateCID(updateCid);
			this.callback.onUpdateUserName();
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_AUTH end;");
			break;
		case 4:
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_SYMBOL begin;");
			AvsPersistTool.saveAvsSymbol(this.nativeBase.getSymbol());
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_SYMBOL end;");
			break;
		case 2:
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_DEVICEINF begin;");
			String deviceName = this.nativeDevice.getName();
			this.callback.onDeviceNameChange(deviceName);
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_DEVICEINF end;");
			break;
		case 3:
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_SERVICE begin;");
			int push = this.nativeDevice.getPushFlag();
			this.callback.onPushStateChange(1 == push);
			int email = this.nativeDevice.getEmailFlag();
			this.callback.onEmailStateChange(1 == email);
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_SERVICE end;");
			break;
		case 6:
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_CFGBUS begin;");
			if (4 != pid) {
				if (33 != pid) {
					if (7 == pid) {
					}
				}
			}
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_CFGBUS end;");
			break;
		case 8:
			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_CHARGE begin;");

			RvsLog.i(Streamer.class, "handleBaseMsg()", "EN_CBBS_NTY_TYPE_CHARGE end;");
			break;
		case 1:
		case 5:
		case 7:
		default:
			RvsLog.w(Streamer.class, "handleBaseMsg()", "no case msg tag: " + tag);
		}
	}

	private void handleLoginMsg(CbpMessage uspMsg) {
		if (this.callback == null) {
			return;
		}
		switch (uspMsg.getMsgID()) {
		case 0:
			if (this.callback != null) {
				int progress = uspMsg.getUI(0, 0);
				int error = uspMsg.getUI(3, 0);
				RvsLog.w(Streamer.class, "handleLoginMsg()", "auth progess: " + progress + ", error=" + error);
				LoginState state = LoginState.CONNECTING;
				if ((AuthState.SUCCESS.intValue() == progress) && (error == RvsError.SUCESS.intValue())) {
					state = LoginState.CONNECTED;
				} else if (error != RvsError.SUCESS.intValue()) {
					state = LoginState.DISCONNECT;
				}
				if (state == LoginState.CONNECTED) {
					if (this.appVersion != null) {
						this.nativeDevice.setVersion(this.appVersion);
					}
					NativeDeviceInfo.getInstance().setAlarmRecordFlag(1);
				}
				this.callback.onLoginResult(state, progress, LoginError.valueOfInt(error == 0 ? 0 : 13));
				this.callback.onAuthResult(AuthState.valueOfInt(progress), RvsError.valueOfInt(error));
			}
			break;
		case 5:
			if (this.callback != null) {
				int progress = uspMsg.getUI(0, 0);
				long cid = uspMsg.getXXLSIZE(4, 0L);
				int error = uspMsg.getUI(3, 0);
				RvsSessionState state = RvsSessionState.DISCONNECTED;
				if ((RemoteViewerState.CANUSE.intValue() == progress)
						|| (RemoteViewerState.CONNECTED.intValue() == progress)) {
					state = RvsSessionState.CONNECTED;
				}
				this.callback.onSessionStateChange(cid, state);
				this.callback.onRemoteViewerStateChange(cid, RemoteViewerState.valueOfInt(progress),
						RvsError.valueOfInt(error));
			}
			break;
		}
	}

	private void handleDetectMsg(CbpMessage uspMsg) {
		int type = -1;
		switch (uspMsg.getMsgID()) {
		case 3:
			MotionDetectCallback callback1 = this.media.getMotionDetectCallback();
			if (callback1 == null) {
				return;
			}
			type = uspMsg.getUI(0, -1);
			int status = uspMsg.getUI(9, -1);
			if (type == 1) {
				callback1.onMotionDetectState(MotionDetectState.vauleOfInt(status));
			}
			break;
		case 2:
			MotionDetectCallback callback2 = this.media.getMotionDetectCallback();
			if (callback2 == null) {
				return;
			}
			type = uspMsg.getUI(0, -1);
			if (type == 1) {
				callback2.onMotionDetectState(MotionDetectState.MOTIONDECTED);
			}
			break;
		case 1:
			MotionDetectSettingsCallback callback3 = this.media.getMotionDetectSettingsCallback();
			if (callback3 == null) {
				return;
			}
			int camId = uspMsg.getUI(1, -1);

			type = uspMsg.getUI(0, -1);
			if (type == 1) {
				RvsAlarmRecordInfo schedule = this.nativeDevice.getStreamerMotionSchedule(camId);
				callback3.onMotionDetectSettingUpdate(schedule);
			}
			break;
		default:
			RvsLog.w(Streamer.class, "handleDetectMsg()", "no case msg type: " + uspMsg.getMsgID());
		}
	}

	private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			RvsLog.i(Streamer.class, "handleDetectMsg()", "network changed...");
			Streamer.this.setLocalIp(context);
		}
	};
	private CustomCommandListener fileManagerCustomCmdListener;

	private void setLocalIp(Context context) {
		String ip = NetUtil.getLocalIp();
		this.nativeAuth.setLoacalIp(ip);
		if (("0.0.0.0".equals(ip)) && (this.callback != null)) {
			this.callback.onLoginResult(LoginState.DISCONNECT, 0, LoginError.LOCAL_IP_ERR);
			this.callback.onAuthResult(AuthState.FAIL, RvsError.LOCAL_IP_ERR);
		}
	}

	public void onNativeCrash() {
		Intent it = new Intent("com.ichano.avs.action.NativeCrash");
		this.appContext.sendBroadcast(it);
	}

	PayStatusCallback getPayStatusCallback() {
		return this.payStatusCallback;
	}

	public void setPayStatusCallback(PayStatusCallback payStatusCallback) {
		this.payStatusCallback = payStatusCallback;
	}

	public void setFileManagerCustomCommandListener(CustomCommandListener listener) {
		this.fileManagerCustomCmdListener = listener;
	}

	public String getCachePath() {
		return this.cachePath;
	}

	public boolean isInSameLocalNetwork(long avsCid) {
		return this.nativeAuth.checkSameLanNetWork(avsCid);
	}
}
