package com.ds05.launcher.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ds05.launcher.common.utils.ImageUtil;
import com.ds05.launcher.common.utils.UploadUtils;

import java.io.File;

import static com.ds05.launcher.common.utils.ImageUtil.getFileNameNoEx;

public class UploadFileTask extends AsyncTask<String, Void, String>{

	public static final String requestURL="http://139.196.191.140:9090/fileUpload";
	public static final String IMAGETYPE = "image";
	public static final String VIDEOTYPE = "video";

	/**
	 *  可变长的输入参数，与AsyncTask.exucute()对应
	 */
	private  Context context=null;
	public UploadFileTask(Context ctx){
		this.context=ctx;
	}
	@Override
	protected void onPostExecute(String result) {
		// 返回HTML页面的内容
		Log.d("ZXH","##############result  = " + result);
		if(UploadUtils.SUCCESS.equalsIgnoreCase(result)){
			Toast.makeText(context, "上传成功!",Toast.LENGTH_LONG ).show();
		}else{
			Toast.makeText(context, "上传失败!",Toast.LENGTH_LONG ).show();
		}
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected String doInBackground(String... params) {
		//File file=new File(params[0]);
		Log.d("ZXH","############## params[1] = " + params[1]);
		if(IMAGETYPE.equals(params[1])){
			Log.d("ZXH","###### upload IMAGETYPE");
			Bitmap bitmap = ImageUtil.compressImageFromFile(params[0], 1024f);// 按尺寸压缩图片
			String fileName=ImageUtil.getFileNameNoEx(params[0].substring(params[0].lastIndexOf("/")+1));
			File file = ImageUtil.compressImage(bitmap,fileName);  //按质量压缩图片
			return UploadUtils.uploadFile( file, requestURL);
		}else if(VIDEOTYPE.equals(params[1])){
			Log.d("ZXH","###### upload VIDEOTYPE");
			File file = new File(params[0]);
			return UploadUtils.uploadFile( file, requestURL);
		}
		return null;
	}
	@Override
	protected void onProgressUpdate(Void... values) {
	}



}