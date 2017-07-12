package com.ichano.rvs.streamer.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Process;
import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.streamer.Media;
import com.ichano.rvs.streamer.Streamer;
import com.ichano.rvs.streamer.callback.StreamerCallback;
import com.ichano.rvs.streamer.constant.AuthState;
import com.ichano.rvs.streamer.constant.LoginError;
import com.ichano.rvs.streamer.constant.LoginState;
import com.ichano.rvs.streamer.constant.RemoteViewerState;
import com.ichano.rvs.streamer.constant.RvsError;
import com.ichano.rvs.streamer.constant.RvsSessionState;
import java.io.File;

public abstract class AvsInitHelper implements StreamerCallback {
	private static final String TAG = AvsInitHelper.class.getSimpleName();
	protected Context context;
	protected Streamer streamer;
	protected Media media;
	private static boolean hasInit = false;
	private static boolean hasLogin = false;
	private static boolean hasLogout = false;

	public AvsInitHelper(Context applicationContext) {
		this.context = applicationContext;
		if (hasInit) {
			RvsLog.w(AvsInitHelper.class, "AvsInitHelper()", "Has init, should init once.");
		} else {
			init();
			hasLogout = false;
		}
	}

	private void init() {
		loadLibs();
		this.streamer = Streamer.getStreamer();
		this.media = this.streamer.getMedia();
		hasInit = true;
	}

	private void loadLibs() {
		try {
			System.loadLibrary("gnustl_shared");
			System.loadLibrary("yuvrotate");
			System.loadLibrary("x264");
			System.loadLibrary("sdk30");
			System.loadLibrary("streamer30");
			System.loadLibrary("aaccodec");
			System.loadLibrary("ichaudio");
		} catch (UnsatisfiedLinkError ule) {
			ule.printStackTrace();
			Process.killProcess(Process.myPid());
		}
	}

	private String getAppVersion() {
		try {
			PackageManager packageManager = this.context.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(this.context.getPackageName(), 0);
			String version = packInfo.versionName;
			RvsLog.i(AvsInitHelper.class, "getAppVersion()", "Developer's app version = " + version);
			return version;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getConfigPath() {
		return this.context.getFilesDir().getAbsolutePath();
	}

	public String getCachePath() {
		String pkg = this.context.getPackageName();
		String dir = "/AVS_" + pkg;
		if (Environment.getExternalStorageState().equals("mounted")) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + dir;
		}
		return getConfigPath();
	}

	public String getPersistentPath() {
		return null;
	}

	public void login() {
		if (hasLogin) {
			RvsLog.w(AvsInitHelper.class, "login()", "Has login, should login once.");
			return;
		}
		RvsLog.i(AvsInitHelper.class, "login()", "login begin!");
		String companyID = getCompanyID();
		if (companyID == null) {
			throw new NullPointerException("getCompanyID() can not return null.");
		}
		if ("".equals(companyID)) {
			throw new NullPointerException("getConfigPath() can not return \"\".");
		}
		String appID = getAppID();
		if (appID == null) {
			throw new NullPointerException("getAppID() can not return null.");
		}
		if ("".equals(appID)) {
			throw new NullPointerException("getAppID() can not return \"\".");
		}
		String license = getLicense();
		if (license == null) {
			throw new NullPointerException("getLicense() can not return null.");
		}
		boolean ret = this.streamer.init(this.context, getAppVersion(), getConfigPath(), getCachePath(),getPersistentPath());
		if (!ret) {
			RvsLog.i(AvsInitHelper.class, "login()", "streamer init error!");
		}
		if (enableDebug()) {
			this.streamer.setDebugEnable(true);
		}
		this.streamer.setLoginInfo(companyID, getCompanyKey(), appID, license);
		this.streamer.setMaxSessionNum(getMaxSessionNum());
		RvsLog.i(AvsInitHelper.class, "login()", "companyID = " + companyID + ", companyKey = " + getCompanyKey()+ ", appID = " + appID + ", license = " + license + ", maxSessionNum = " + getMaxSessionNum());

		this.streamer.setCallback(this);
		this.streamer.login();
		hasLogin = true;
		hasLogout = false;
		RvsLog.i(AvsInitHelper.class, "login()", "login end!");
	}

	public void logout() {
		if (hasLogout) {
			RvsLog.w(AvsInitHelper.class, "logout()", "Has logout.");
			return;
		}
		this.streamer.logout();
		this.streamer.destroy();
		hasLogout = true;
		hasInit = false;
		hasLogin = false;
	}

	public abstract String getCompanyID();

	public abstract String getCompanyKey();

	public abstract String getAppID();

	public abstract String getLicense();

	public abstract int getMaxSessionNum();

	public abstract boolean enableDebug();

	public abstract void onDeviceNameChange(String paramString);

	@Deprecated
	public void onLoginResult(LoginState loginState, int progressRate, LoginError errorCode) {
	}

	public abstract void onAuthResult(AuthState paramAuthState, RvsError paramRvsError);

	@Deprecated
	public void onSessionStateChange(long remoteCID, RvsSessionState sessionState) {
	}

	public abstract void onRemoteViewerStateChange(long paramLong, RemoteViewerState paramRemoteViewerState,
			RvsError paramRvsError);

	public abstract void onUpdateCID(long paramLong);

	public abstract void onUpdateUserName();

	public void onPushStateChange(boolean pushEnable) {
	}

	public void onEmailStateChange(boolean emailAlertEnable) {
	}
}
