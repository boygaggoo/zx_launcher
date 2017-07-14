package com.ds05.launcher;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.common.Constants;
import com.ds05.launcher.common.utils.MacUtils;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.service.ConnectSocketService;
import com.ds05.launcher.service.LauncherService;
import com.ds05.launcher.service.WeatherService;
import com.ds05.launcher.service.ZhongyunService;

import org.jsoup.helper.StringUtil;
import org.weixvn.wae.manager.EngineManager;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 *
 * update by Vincent add “implements CustomCommandListener”
 */

public class LauncherApplication extends Application  {
    private static Context mContext;
    private final static String TAG = "LauncherApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> LauncherApplication onCreate<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        mContext = getApplicationContext();

        //管理WIFI
        WifiManager  wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        Constants.MAC_ADDRESS= MacUtils.getMac();
        if(!StringUtil.isBlank(Constants.MAC_ADDRESS)){
            Constants.ZHONGYUN_LINCESE=Constants.MAC_ADDRESS.replaceAll(":","");
        }

        EngineManager.getInstance().setContext(getApplicationContext()).setDB(null);
        startService(new Intent(LauncherApplication.getContext(), LauncherService.class));
        if(PrefDataManager.isShowWeather()) {
            startService(new Intent(LauncherApplication.getContext(), WeatherService.class));
        }

        //此处Vincent 添加  check网络状态
        ConnectUtils.NETWORK_IS_OK=ConnectUtils.isNetAvailable(mContext);
        if(ConnectUtils.NETWORK_IS_OK){
            startService(new Intent(LauncherApplication.getContext(), ConnectSocketService.class));
//            startService(new Intent(LauncherApplication.getContext(), ZhongyunService.class));
        }
    }


    public static Context getContext() {
        return mContext;
    }



    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate");
        super.onTerminate();
//        ConnectUtils.mMyAvsHelper.logout();
    }

}
