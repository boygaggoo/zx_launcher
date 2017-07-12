package com.ds05.launcher.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.common.utils.Utils;
import com.ds05.launcher.weather.SinaWeather;
import com.ds05.launcher.weather.Weather;

import org.weixvn.wae.manager.EngineManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 */

public class WeatherService extends Service {

    private static final int GET_WEATHER_FAIL_TRY_AGAIN_COUNT = 4;
    private static final long GET_WEATHER_FAIL_TRY_AGAIN_INTERVAL_TIME = 1 * 60 * 1000; // 1 min

    private boolean mIsWaitWeatherResponse = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d("DS05", "WeatherService onCreate");
        if(PrefDataManager.isShowWeather()) {
            registerReceiver();
            startWeatherUpdateTimer();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("DS05", "WeatherService onDestroy");
        stopWeatherUpdateTimer();
        unregisterReceiver();

        super.onDestroy();
    }

    private Timer mTimer = null;
    private void startWeatherUpdateTimer() {
        if(mTimer != null) {
            stopWeatherUpdateTimer();
        }

        long period = PrefDataManager.getWeatherUpdateInterval();
        Log.d("DS05", "WeatherService startWeatherUpdateTimer period:" + period);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mInternalHandler.removeMessages(EVT_GET_WEATHER_TRY_AGAIN);
                mInternalHandler.sendEmptyMessage(EVT_UPDATE_WEATHER);
            }
        }, 0, period);

        Log.d("DS05", "WeatherService startWeatherUpdateTimer");
    }
    private void stopWeatherUpdateTimer() {
        if(mTimer == null) {
            mTimer.cancel();
            mTimer = null;
        }
        Log.d("DS05", "WeatherService stopWeatherUpdateTimer");
    }

    private void registerReceiver() {
        IntentFilter fillter = new IntentFilter();
        fillter.addAction(Weather.ACTION_GET_WEATHER_FAIL);
        fillter.addAction(Weather.ACTION_GET_WEATHER_SUCCESS);
        fillter.addAction(Weather.ACTION_SET_WEATHER_CITY);
        registerReceiver(mReceiver, fillter);
    }
    private void unregisterReceiver() {
        unregisterReceiver(mReceiver);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("DS05", "WeatherService mReceiver intent:" + intent.toString());
            if(action.equals(Weather.ACTION_GET_WEATHER_FAIL)) {
                int reason = intent.getIntExtra(Weather.EXTRA_REASON,
                        Weather.REASON_SERVER_EXCEPTION);
                if(reason == Weather.REASON_CITY_ERROR) {
                    mInternalHandler.sendEmptyMessage(EVT_CITY_ERROR);
                } else if(tryAgainGetWeather()) {
                    mInternalHandler.sendEmptyMessageDelayed(
                            EVT_GET_WEATHER_TRY_AGAIN,
                            GET_WEATHER_FAIL_TRY_AGAIN_INTERVAL_TIME);
                } else {
                    cleanGetWeatherFailCount();
                    Message msg = new Message();
                    msg.what = EVT_NOTIFY_WEATHER_UPDATE_FAIL;
                    msg.arg1 = intent.getIntExtra(Weather.EXTRA_REASON,
                            Weather.REASON_SERVER_EXCEPTION);
                    mInternalHandler.sendMessage(msg);
                }
            } else if(action.equals(Weather.ACTION_GET_WEATHER_SUCCESS)) {
                if(mIsWaitWeatherResponse) {
                    mIsWaitWeatherResponse = false;
                    sendBroadcast(new Intent(Weather.ACTION_WEATHER_SET_RESP));
                }
                cleanGetWeatherFailCount();
            } else if(action.equals(Weather.ACTION_SET_WEATHER_CITY)) {
                mIsWaitWeatherResponse = true;
                mInternalHandler.removeMessages(EVT_GET_WEATHER_TRY_AGAIN);
                mInternalHandler.removeMessages(EVT_UPDATE_WEATHER);
                mInternalHandler.sendEmptyMessage(EVT_UPDATE_WEATHER);
            }
        }
    };//mReceiver

    private static final int EVT_GET_WEATHER_TRY_AGAIN = 0;
    private static final int EVT_NOTIFY_WEATHER_UPDATE_FAIL = 1;
    private static final int EVT_UPDATE_WEATHER = 2;
    private static final int EVT_CITY_ERROR = 3;
    private Handler mInternalHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("DS05", "WeatherService handleMessage:" + msg.what);
            switch (msg.what) {
                case EVT_GET_WEATHER_TRY_AGAIN:
                    updateWeather();
                    break;
                case EVT_NOTIFY_WEATHER_UPDATE_FAIL:
                    Intent intent = new Intent(Weather.ACTION_WEATHER_UPDATE);
                    intent.putExtra(Weather.EXTRA_RESULT_STATE, false);
                    intent.putExtra(Weather.EXTRA_REASON, msg.arg1);
                    LauncherApplication.getContext().sendStickyBroadcast(intent);
                    if(mIsWaitWeatherResponse) {
                        mIsWaitWeatherResponse = false;
                        sendBroadcast(new Intent(Weather.ACTION_WEATHER_SET_RESP));
                    }
                    break;
                case EVT_UPDATE_WEATHER:
                    updateWeather();
                    break;
                case EVT_CITY_ERROR:
                    String city = PrefDataManager.getWeatherUpdateCity();
                    String[] ret = city.split(",");
                    int count = ret.length;
                    if(count == 1) {
                        PrefDataManager.setWeatherUpdateCity("--");
                        cleanGetWeatherFailCount();
                        Message m = new Message();
                        m.what = EVT_NOTIFY_WEATHER_UPDATE_FAIL;
                        m.arg1 = Weather.REASON_CITY_ERROR;
                        mInternalHandler.sendMessage(m);
                    } else if(count == 2) {
                        PrefDataManager.setWeatherUpdateCity(ret[1]);
                        mInternalHandler.sendEmptyMessage(EVT_UPDATE_WEATHER);
                    } else if(count == 3) {
                        PrefDataManager.setWeatherUpdateCity(ret[0] + "," + ret[1]);
                        mInternalHandler.sendEmptyMessage(EVT_UPDATE_WEATHER);
                    } else {
                        PrefDataManager.setWeatherUpdateCity("");
                        Message m = new Message();
                        m.what = EVT_NOTIFY_WEATHER_UPDATE_FAIL;
                        m.arg1 = Weather.REASON_SERVER_EXCEPTION;
                        mInternalHandler.sendMessage(m);
                    }
                    break;
            }
        }
    };

    private void updateWeather() {
        Log.d("DS05", "WeatherService updateWeather");
        if(!Utils.checkNetwork()) {
            Log.d("DS05", "WeatherService updateWeather. check network fail");
            Message msg = new Message();
            msg.what = EVT_NOTIFY_WEATHER_UPDATE_FAIL;
            msg.arg1 = Weather.REASON_NETWORK_EXCEPTION;
            mInternalHandler.sendMessage(msg);
            return;
        }

        String city = PrefDataManager.getWeatherUpdateCity();
        if(TextUtils.isEmpty(city) || city.equals("--")) {
            Log.d("DS05", "WeatherService updateWeather. City error");
            Message msg = new Message();
            msg.what = EVT_NOTIFY_WEATHER_UPDATE_FAIL;
            msg.arg1 = Weather.REASON_CITY_ERROR;
            mInternalHandler.sendMessage(msg);
            return;
        }

        Log.d("DS05", "[WeatherService][updateWeather]city-0:" + city);
        String[] ret = city.split(",");
        city = ret[ret.length - 1];
        Log.d("DS05", "[WeatherService][updateWeather]city-1:" + city);
        if(city.equals("省直辖行政单位")
                || city.equals("省直辖县级行政单位")) {
            city = ret[ret.length - 2];
        }
        city = city.replaceAll("市", "");
        if(!city.contains("自治县")) {
            city = city.replaceAll("县", "");
        }
        city = city.replaceAll("镇", "");
        city = city.replaceAll("省", "");
        if(!city.contains("自治区")) {
            city = city.replaceAll("区", "");
        }
        city = city.replaceAll("下属县", "");
        city = city.replaceAll("地区", "");
        Log.d("DS05", "[WeatherService][updateWeather]city-2:" + city);
        EngineManager.getInstance().getWebPageMannger()
                .getWebPage(SinaWeather.class).setHtmlValue("city", city);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EngineManager.getInstance().getWebPageMannger()
                        .updateWebPage(SinaWeather.class, true);
            }
        });
        thread.start();
    }

    private int mGetWeatherFailCount = 0;
    private boolean tryAgainGetWeather() {
        try {
            return mGetWeatherFailCount <= GET_WEATHER_FAIL_TRY_AGAIN_COUNT;
        } finally {
            mGetWeatherFailCount++;
        }
    }
    private void cleanGetWeatherFailCount() {
        mGetWeatherFailCount = 0;
    }
}
