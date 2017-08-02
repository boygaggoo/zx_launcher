/*
 * Copyright (C) 2015 iChano incorporation's Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ds05.launcher.common.config;

import android.content.Context;
import android.util.Log;

import com.ds05.launcher.common.Constants;
import com.ichano.rvs.streamer.constant.AuthState;
import com.ichano.rvs.streamer.constant.RemoteViewerState;
import com.ichano.rvs.streamer.constant.RvsError;
import com.ichano.rvs.streamer.ui.AvsInitHelper;

public class MyAvsHelper extends AvsInitHelper {

	private static final String TAG = MyAvsHelper.class.getSimpleName();
	public static final String DEFAULT_USER_NAME = "admin";

	public String[] userNameAndPwd;
	public String deviceName;
	public boolean haveLogin;

	private static MyAvsHelper mAvs;

	public static MyAvsHelper getInstance(Context applicationContext) {
		if (null == mAvs) {
			mAvs = new MyAvsHelper(applicationContext);
		}
		return mAvs;
	}

	public MyAvsHelper(Context applicationContext) {
		super(applicationContext);
	}


	// 设置AppID，从网站注册获得
	@Override
	public String getAppID() {
		return "31112017040614575901491447492820";
	}

	// 设置CompanyID，从网站注册获得
	@Override
	public String getCompanyID() {
		return "11112017040610173601491372729463";
	}

	// 设置CompanyKey，从网站注册获得
	@Override
	public String getCompanyKey() {
		return "cc0c589772a2453794173c8aea66f476";
	}

	// 设置License，从网站注册获得
	@Override
	public String getLicense() {
		
		return  Constants.ZHONGYUN_LINCESE;
	}

	// 最多允许多少观看同时连接
	@Override
	public int getMaxSessionNum() {
		return 5;
	}

	@Override
	public void onDeviceNameChange(String deviceName) {
		Log.i(TAG, "deviceName = " + deviceName);
		this.deviceName = deviceName;
//		mDeviceNameView.setText(deviceName);
	}

	@Override
	public void onAuthResult(AuthState state, RvsError error) {
		Log.i(TAG, "loginState = " + state+ ", error = " + error);

		if (AuthState.SUCCESS == state) {
			String cid = streamer.getCID();
			Log.i(TAG, "cid = " + cid);
			//mCidView.setText(cid);
			String[] namePwd = streamer.getUserNameAndPwd();
			if (null != namePwd) {
				userNameAndPwd = namePwd;
				//mUserView.setText(namePwd[0]);
				//mPwdView.setText(namePwd[1]);
			} else {
				//mUserView.setText(DEFAULT_USER_NAME);
				//mPwdView.setText(DEFAULT_USER_NAME);
				streamer.setUserNameAndPwd(DEFAULT_USER_NAME, DEFAULT_USER_NAME);
				userNameAndPwd = new String[] { DEFAULT_USER_NAME,DEFAULT_USER_NAME };
			}

			String deviceName = streamer.getDeviceName();
			if (null == deviceName) {
				this.deviceName = "android-" + cid;
				//mDeviceNameView.setText(deviceName);
			} else {
				this.deviceName = deviceName;
				//mDeviceNameView.setText(deviceName);
			}
			//mLogStateView.setText(context.getString(R.string.connected));
			haveLogin = true;
		} else if (AuthState.CONNECTING == state) {
			//mLogStateView.setText(context.getString(R.string.connecting));
			haveLogin = false;
		} else if (AuthState.FAIL == state) {
			//mLogStateView.setText(context.getString(R.string.disconnected));
			haveLogin = false;
		}
	}

	@Override
	public void onRemoteViewerStateChange(long arg0, RemoteViewerState arg1,
			RvsError arg2) {

	}

	@Override
	public void onUpdateCID(long cid) {
		//mCidView.setText(String.valueOf(cid));
		Log.i(TAG, "cid = " + cid);
	}

	@Override
	public void onUpdateUserName() {
		String[] namePwd = streamer.getUserNameAndPwd();
		if(null == namePwd) return;
		userNameAndPwd = namePwd;
		//mPwdView.setText(namePwd[1]);
		Log.i(TAG, "namePwd = " + namePwd);
	}

	@Override
	public boolean enableDebug() {
		return true;
	}

}
