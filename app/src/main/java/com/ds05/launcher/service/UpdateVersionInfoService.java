package com.ds05.launcher.service;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.ds05.launcher.bean.UpdateDataBean;
import com.ds05.launcher.common.CommUtil;
import com.ds05.launcher.common.Constants;
import com.ds05.launcher.common.JsonSerializer;
import com.loopj.android.httpj.AsyncHttpClient;
import com.loopj.android.httpj.AsyncHttpResponseHandler;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class UpdateVersionInfoService extends Service {
	private static final String TAG = "ZXH";
	Timer checkTimer;
	TimerTask checkTask;
	private SharedPreferences userInfo;
	boolean isShowUpdateInfo = false;
	private Handler mHandler;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		userInfo = getSharedPreferences("", MODE_PRIVATE);
	}

	@Override
	public void onDestroy() {
		if(checkTimer != null){
			checkTimer.cancel();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mHandler = new Handler(Looper.getMainLooper());
		Log.d("ZXH","########### onStartCommand");

		checkTimer  = new Timer();
		checkTask = new TimerTask() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if(checkTime(0,1)){
						}
						updateInfo();
					}
				});
			}
		};
		checkTimer.scheduleAtFixedRate(checkTask,0, 50*60*1000);
		return START_NOT_STICKY;
	}

	Long mDownloadId;
	boolean downloadstatus = false;
	Timer timer;
	TimerTask task;

	private void updateInfo(){

		String requst = Constants.UPDATE_URL;
		Log.d(TAG,"####updateInfo httpRequest, requst = " + requst);
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(requst, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				// called before request is started
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try{
					Log.d("ZXH","########### onSuccess");
					String content = new String(responseBody);
					UpdateDataBean bean = JsonSerializer.deSerialize(content, UpdateDataBean.class);
					PackageManager pm = getPackageManager();
					PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
					int versionCode = pi.versionCode;
					int lastVersionCode = Integer.parseInt(bean.getLastVersion());

					Log.d(TAG,"#### onSuccess, versionCode = " + versionCode);
					Log.d(TAG,"#### onSuccess, lastVersionCode = " + lastVersionCode);

					if(lastVersionCode > versionCode){
						if(!downloadstatus){
							try{
								mDownloadId = CommUtil.downLoadFile(UpdateVersionInfoService.this, bean.getUrl());
								downloadstatus = true;
								timer = new Timer();
								task = new TimerTask() {
									@Override
									public void run() {
										setDownLoadStatus();
									}
								};
								timer.scheduleAtFixedRate(task, 0, 1000);
							}catch(Exception e){
								Log.d("ZXH","## e = " + e);
								e.printStackTrace();
							}
						}
					}
				}catch(Exception e){
					Log.d(TAG,"###e = " + e);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				Log.d(TAG,"#### onFailure error = " + error);
			}

			@Override
			public void onRetry(int retryNo) {
				// called when request is retried
			}
		});
	}

	private void setDownLoadStatus(){
		DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		Query query = new Query();
		query.setFilterById(mDownloadId);
		Cursor cursor = manager.query(query);
		while(cursor.moveToNext()) {
			int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			switch(status) {
				case DownloadManager.STATUS_RUNNING:
					break;
				case DownloadManager.STATUS_FAILED:
					task.cancel();
					break;
				case DownloadManager.STATUS_SUCCESSFUL:
					Log.d("ZXH","########### STATUS_SUCCESSFUL");
					task.cancel();
					installApk();
					break;
			}
		}
		cursor.close();
	}

	private void installApk(){
		downloadstatus = false;
		String ret = CommUtil.installApkBySilent();
		Log.d("ZXH","########## ret = " + ret);
	}

	private boolean checkTime(int begin, int end){
		Calendar cal = Calendar.getInstance();// 当前日期
		int hour = cal.get(Calendar.HOUR_OF_DAY);// 获取小时
		int minute = cal.get(Calendar.MINUTE);// 获取分钟
		int minuteOfDay = hour * 60 + minute;// 从0:00分开是到目前为止的分钟数

		int beginTime = begin * 60;
		int endTime   = end * 60;

		if(minuteOfDay >= beginTime && minuteOfDay <= endTime){
			return true;
		}
		return false;
	}
}
