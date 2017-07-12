package com.ichano.rvs.streamer.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public final class AvsPersistTool {
	private static File symbolDir;
	private static String symbolFile = ".key";
	private static Context context;
	private static final String LOGIN_INFO = "LOGIN_INFO";

	public static void init(Context _context, String persistentPath) {
		context = _context;

		String dir = persistentPath == null ? "/AvsAuth_" + context.getPackageName() : persistentPath;
		if (Environment.getExternalStorageState().equals("mounted")) {
			symbolDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + dir);
		} else {
			String ext = getStoragePath(context);
			if (ext != null) {
				symbolDir = new File(ext + dir);
			} else {
				symbolDir = new File(context.getFilesDir().getAbsolutePath() + dir);
			}
		}
		if (!symbolDir.exists()) {
			symbolDir.mkdir();
			try {
				new File(symbolDir, symbolFile).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void init(Context context) {
		init(context, null);
	}

	public static String getAvsSymbol() {
		File file = new File(symbolDir, symbolFile);
		if (!file.exists()) {
			return "";
		}
		String symbol = "";
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			symbol = br.readLine();
			br.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (symbol == null) {
			symbol = "";
		}
		return symbol;
	}

	public static void saveAvsSymbol(String symbol) {
		if (symbol == null) {
			return;
		}
		File file = new File(symbolDir, symbolFile);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write(symbol);
			bw.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getStoragePath(Context cxt) {
		if (Build.VERSION.SDK_INT <= 10) {
			return null;
		}
		StorageManager storageManager = (StorageManager) cxt.getSystemService("storage");
		try {
			Method method = StorageManager.class.getDeclaredMethod("getVolumePaths", new Class[0]);
			method.setAccessible(true);
			Object result = method.invoke(storageManager, new Object[0]);
			if ((result != null) && ((result instanceof String[]))) {
				String[] pathes = (String[]) result;
				for (String path : pathes) {
					if ((!TextUtils.isEmpty(path)) && (new File(path).exists())) {
						StatFs statFs = new StatFs(path);
						if (statFs.getBlockCount() * statFs.getBlockSize() != 0) {
							return path;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void clearAuthCache() {
		File file = new File(symbolDir, symbolFile);
		if (file.exists()) {
			file.delete();
		}
	}

	public static void saveLoginInfoHashCode(String companyId, String license) {
		String str = companyId + license;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt("LOGIN_INFO", str.hashCode());
		editor.commit();
	}

	public static int getLoginInfoHashCode() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		int ret = sp.getInt("LOGIN_INFO", 0);
		return ret;
	}

}
