package com.ds05.launcher.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.ds05.launcher.R;

@SuppressLint("NewApi")
public class CommUtil {
	private static CommUtil instace;
	public static CommUtil getInstance(){
		if(null == instace){
			instace = new CommUtil();
		}
		return instace;
	}

	public static final String randomString(int length) {
		Random randGen = null;
		char[] numbersAndLetters = null;
		Object initLock = new Object();
		if (length < 1) {
			return null;
		}
		if (randGen == null) {
			synchronized (initLock) {
				if (randGen == null) {
					randGen = new Random();
					numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz"
							+ "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
				}
			}
		}
		char[] randBuffer = new char[length];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
		}
		return new String(randBuffer);
	}
	public static void main(String[] args) {
	}
	
	/**
	 * 获取设备宽度
	 */
	public static int getPixelsWidth(Activity context){
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}
	
	/**
	 * 获取设备高度
	 */
	public static int getPixelsHeight(Activity context){
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}
	/**
	* 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	*/
	public static int dip2px(Context context, float dpValue) {
	  final float scale = context.getResources().getDisplayMetrics().density;
	  return (int) (dpValue * scale + 0.5f);
	}

	/**
	* 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	*/
	public static int px2dip(Context context, float pxValue) {
	  final float scale = context.getResources().getDisplayMetrics().density;
	  return (int) (pxValue / scale + 0.5f);
	}
	/**
	* 根据手机的分辨率从 px(像素) 的单位 转成为 sp
	*/
	public static int px2sp(Context context, float pxValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (pxValue / fontScale + 0.5f);  
    }  
	/**
	* 判断是手机还是pad
	* @param context
	* @return
	*/
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	//获取NavigationBar高度
	public static int getNavigationBarHeight(Context context) {  
        Resources resources = context.getResources();  
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");  
        //获取NavigationBar的高度  
        int height = resources.getDimensionPixelSize(resourceId);  
        return height;
    }  
	
	//获取是否有NavigationBar
	public static boolean checkDeviceHasNavigationBar(Context context) {   
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();  
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);  
  
        if (!hasMenuKey && !hasBackKey) {  
            return true;  
        }  
        return false;  
    }
	//获取statusBar高度
	public static int getStatusBarHeight(Context context) {
		  int result = 0;
		  int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		  if (resourceId > 0) {
		      result = context.getResources().getDimensionPixelSize(resourceId);
		  }
		  return result;
		}
	
	/**
	 * 获取系统版本号
	 */
	
	public static int getAndroidVersion(){
		return android.os.Build.VERSION.SDK_INT;
	}
	/**
	 * 获取当前分辨率下指定单位对应的像素大小（根据设备信息）
	 * px,dip,sp -> px
	 * 
	 * Paint.setTextSize()单位为px
	 * 
	 * 代码摘自：TextView.setTextSize()
	 * 
	 * @param unit  TypedValue.COMPLEX_UNIT_*
	 * @param size
	 * @return
	 */
	public static float getRawSize(Context context,int unit, float size) {
	       Resources r;

	       if (context == null)
	           r = Resources.getSystem();
	       else
	           r = context.getResources();
	        
	       return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
	}
	/**
	 * 刷新sd内容
	 */
	public static void refreshContent(Context context,String path){
		Uri data = Uri.parse("file://"+path);
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
	}
	
	/**
	 * 获取图片缩略图
	 */
	public static Bitmap getPictureImage(String urlPath) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(urlPath, options);
		options.inJustDecodeBounds = false;
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / 100;
		int beHeight = h / 80;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(urlPath, options);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 80,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	
	/**
	 * 获取视频缩略图
	 */
	public static Bitmap getVideoImage(String urlPath){
		Bitmap bitmap = null;
		bitmap = ThumbnailUtils.createVideoThumbnail(urlPath, MediaStore.Images.Thumbnails.MICRO_KIND);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 80,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	
	public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
	
	
	//安装更新文件
	static String apkName;
	public static String local_apk_url = "DS05/downLoad";
	private static boolean flag = false;

	public static String installApkBySilent(){
		Log.d("ZXH","##############installApkBySilent ");

		if(flag){
			Log.d("ZXH","##############return null ");
			return null;
		}
		flag = true;
//		String apkAbsolutePath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(local_apk_url).concat("/").concat(apkName);
		String apkAbsolutePath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(local_apk_url).concat("/").concat("DS05 Launcher1.apk");
		String[] args = { "pm", "install", "-r", apkAbsolutePath };
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write('\n');
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}
		flag = false;
		Log.d("ZXH","##############return result = " + result);
		return result;
	}

	public static void installApk(Context context){
		File cacheDir = new File(Environment.getExternalStorageDirectory(), local_apk_url.concat("/").concat(apkName));
		Intent tent = new Intent();
		tent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tent.setAction(Intent.ACTION_VIEW);
		tent.setDataAndType(Uri.fromFile(cacheDir),"application/vnd.android.package-archive");
		context.startActivity(tent);
	}
	
	//下载新版本
	public static Long downLoadFile(Context context,String downLoadUrl){
		apkName = context.getString(R.string.app_name).concat(".apk");
		DownloadManager manager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(downLoadUrl);
		Request request = new Request(uri);
		File cacheDir = new File(Environment.getExternalStorageDirectory(), local_apk_url);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		File f = new File(cacheDir, apkName);
		if(null != f){
			f.delete();
		}
		request.setDestinationInExternalPublicDir(local_apk_url, apkName);
		request.setAllowedNetworkTypes(Request.NETWORK_MOBILE| Request.NETWORK_WIFI);
		return manager.enqueue(request);
	}
}
