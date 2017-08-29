package com.ds05.launcher.common.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.ds05.launcher.common.Constants;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils{
	private static boolean isCheckFile = false;
	private static final Uri ImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	private static final Uri VideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	private static Context mContext;

	public static void checkStorageSpace(Context context){
		mContext = context;
		if(isCheckFile){
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				checkStorage();
			}
		}).start();
	}


	private static void checkSDCardDirectory(File dirFile){
		if(dirFile == null){
			return;
		}

		Uri mUri = ImageUri;
		if(dirFile.getAbsolutePath().concat("/").contains(Constants.MANUAL_CAPTURE_PATH)){
			mUri = ImageUri;
		}else if(dirFile.getAbsolutePath().concat("/").contains(Constants.DOORBELL_PATH)){
			mUri = ImageUri;
		}else if(dirFile.getAbsolutePath().concat("/").contains(Constants.ALARM_CAPTURE_PATH)){
			mUri = ImageUri;
		}else if(dirFile.getAbsolutePath().concat("/").contains(Constants.ALARM_VIDEO_PATH)){
			mUri = VideoUri;
		}

		if(dirFile.isDirectory()){
			File[] files = dirFile.listFiles();
			if(null == files){
				return;
			}
			List<File> filesList = Arrays.asList(files);
			if( filesList.size() > 100){
				Map<String, String> fileMap = new HashMap<String, String>();
				Collections.sort(filesList, new FileComparator());
				ContentResolver cr = mContext.getContentResolver();
				Cursor mCursor = cr.query(mUri,null,null,null,null);
				if(mCursor == null){
					return;
				}
				while(mCursor.moveToNext()) {
					String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
					String id = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
					if(path.contains(dirFile.getAbsolutePath())){
						fileMap.put(path,id);
					}
				}
				mCursor.close();

				for(int i = 0; i < filesList.size() / 4; i++){
					if(filesList.get(i).isFile() && filesList.get(i).exists()){
						filesList.get(i).delete();
					}
					String deleteId = fileMap.get(filesList.get(i).getAbsolutePath());
					if( deleteId != null){
						Uri deleteIdUri = ContentUris.withAppendedId(mUri, Long.parseLong(deleteId));
						Log.d("ZXH","######## deleteIdUri = " + deleteIdUri);
						cr.delete(deleteIdUri, null, null);
					}
				}
			}
		}
	}

	private static void checkInnerDirectory(File dirFile){
		if(dirFile == null){
			return;
		}

		Uri mUri = ImageUri;
		int maxCount = 0;
		if(dirFile.getAbsolutePath().concat("/").contains(Constants.MANUAL_CAPTURE_PATH)){
			maxCount = 3;
			mUri = ImageUri;
		}else if(dirFile.getAbsolutePath().concat("/").contains(Constants.DOORBELL_PATH)){
			maxCount = 300;
			mUri = ImageUri;
		}else if(dirFile.getAbsolutePath().concat("/").contains(Constants.ALARM_CAPTURE_PATH)){
			maxCount =300;
			mUri = ImageUri;
		}else if(dirFile.getAbsolutePath().concat("/").contains(Constants.ALARM_VIDEO_PATH)){
			maxCount = 100;
			mUri = VideoUri;
		}

		if(maxCount == 0){
			return;
		}

		if(dirFile.isDirectory()){
			File[] files = dirFile.listFiles();
			if(null == files){
				return;
			}
			List<File> filesList = Arrays.asList(files);
			if( filesList.size() > maxCount){
				Map<String, String> fileMap = new HashMap<String, String>();
				Collections.sort(filesList, new FileComparator());
				ContentResolver cr = mContext.getContentResolver();
				Cursor mCursor = cr.query(mUri,null,null,null,null);
				if(mCursor == null){
					return;
				}
				while(mCursor.moveToNext()) {
					String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
					String id = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
					if(path.contains(dirFile.getAbsolutePath())){
						fileMap.put(path,id);
					}
				}
				mCursor.close();
//				for(int i = 0; i < filesList.size(); i++){
//					Log.d("ZXH","##############file["+ i + "] =" + filesList.get(i).getAbsolutePath());
//					Log.d("ZXH","##############file["+ i + "]  key =" + fileMap.get(filesList.get(i).getAbsolutePath()));
//				}
				int deleteCount =  filesList.size() - maxCount;
				for(int i = 0; i < deleteCount; i++){
					if(filesList.get(i).isFile() && filesList.get(i).exists()){
						filesList.get(i).delete();
					}
					String deleteId = fileMap.get(filesList.get(i).getAbsolutePath());
					if( deleteId != null){
						Uri deleteIdUri = ContentUris.withAppendedId(mUri, Long.parseLong(deleteId));
						Log.d("ZXH","######## deleteIdUri = " + deleteIdUri);
						cr.delete(deleteIdUri, null, null);
					}
				}
			}
		}

	}

	private static void checkStorage(){
		try{
			isCheckFile = true;
			boolean haveSDCard = Environment.isExternalStorageRemovable();
			if(haveSDCard){
				File storageFile = Environment.getExternalStorageDirectory();
				long  totalSpace = storageFile.getTotalSpace();
				long  usableSpace = storageFile.getUsableSpace();
				float ratio = (float) (usableSpace * 1.0 / totalSpace);
				if(ratio < 0.2){
					File dirFile;
					dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.MANUAL_CAPTURE_PATH);
					checkSDCardDirectory(dirFile);
					dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DOORBELL_PATH);
					checkSDCardDirectory(dirFile);
					dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.ALARM_CAPTURE_PATH);
					checkSDCardDirectory(dirFile);
					dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.ALARM_VIDEO_PATH);
					checkSDCardDirectory(dirFile);
				}
			}else{
				File dirFile;
				dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.MANUAL_CAPTURE_PATH);
				checkInnerDirectory(dirFile);
				dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DOORBELL_PATH);
				checkInnerDirectory(dirFile);
				dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.ALARM_CAPTURE_PATH);
				checkInnerDirectory(dirFile);
				dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.ALARM_VIDEO_PATH);
				checkInnerDirectory(dirFile);

			}
		}catch (Exception e){
			Log.d("ZXH","##################e = " + e);
		}finally {
			isCheckFile = false;
			Log.d("ZXH","##################finally");
		}
	}

	public static class FileComparator implements Comparator<File> {
		public int compare(File file1, File file2) {
			int flag = file1.getAbsolutePath().compareTo(file2.getAbsolutePath());
			return flag;
		}
	}
}
