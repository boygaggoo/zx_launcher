package com.ichano.rvs.streamer.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.ichano.rvs.internal.RvsLog;
import com.ichano.rvs.streamer.constant.RvsLanguage;

import java.util.Locale;

public class AppUtil {
	public static int getDefaultRvsLanguage() {
		String lan = Locale.getDefault().getLanguage();
		String ctr = Locale.getDefault().getCountry();
		int ret = 2;
		try {
			if ("zh".equals(lan)) {
				ret = RvsLanguage.valueOf(lan + "_" + ctr).intValue();
			} else {
				ret = RvsLanguage.valueOf(lan).intValue();
			}
		} catch (Exception e) {
			RvsLog.e(AppUtil.class, "getDefaultRvsLanguage()", "defaut language not defined in sdk");
		}
		return ret;
	}

	public static long getAvailableSpace(String path) {
		StatFs sf = new StatFs(path);

		int blockSize = sf.getBlockSize();
		int availCount = sf.getAvailableBlocks();

		return blockSize * availCount;
	}

	public static int getAvailMemory(Context context) {
		try {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
			am.getMemoryInfo(mi);
			return (int) (mi.availMem / 1048576L);
		} catch (Exception e) {
		}
		return -1;
	}

	public static String getSDCardPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

}
