package com.ds05.launcher.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ds05.launcher.CameraActivity_ZY;
import com.ds05.launcher.R;
import com.ds05.launcher.common.Constants;
import com.ds05.launcher.common.config.MyAvsHelper;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.weather.SinaWeather;
import com.ds05.launcher.weather.Weather;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Chongyang.Hu on 2017/1/8 0008.
 */

public class InformationFragment extends BaseFragment {
    private TextView mWeaterCity;
    private TextView mDayWeatherInfo;
    private ImageView mDayWeatherImg;
    private TextView mNightWeatherInfo;
    private ImageView mNightWeatherImg;
    private TextView mExceptionWeath;
    private TextView mDayHeader, mNightHeader;

    private View mWeatherView;
    private ImageView mLogoView;

    private Bundle mWeatherData = null;

    private boolean mIsExceptionWeather = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        registerReceiver();
        View rootView = inflater.inflate(R.layout.information_frag, container, false);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
    }

    private boolean mViewCreateFlag = false;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intent = new Intent(getActivity(), CameraActivity.class);
                if(!Constants.cameraIsDestroy){
                    Toast.makeText(getActivity(), "打开摄像头太频繁，2秒后再试", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), CameraActivity_ZY.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                Toast.makeText(getActivity(), "当前设备CID ： "+ MyAvsHelper.zy_cid, Toast.LENGTH_LONG).show();

            }
        });

        mWeaterCity = (TextView)view.findViewById(R.id.id_weather_city);
        mDayWeatherImg = (ImageView)view.findViewById(R.id.id_day_weather_img);
        mDayWeatherInfo = (TextView)view.findViewById(R.id.id_day_weather_text);
        mNightWeatherImg = (ImageView)view.findViewById(R.id.id_night_weather_img);
        mNightWeatherInfo = (TextView)view.findViewById(R.id.id_night_weather_text);

        mExceptionWeath = (TextView)view.findViewById(R.id.id_exception_weather);

        mWeatherView = view.findViewById(R.id.id_weather_view);
        mLogoView = (ImageView)view.findViewById(R.id.id_logo_img);

        mDayHeader = (TextView)view.findViewById(R.id.id_day_header);
        mNightHeader = (TextView)view.findViewById(R.id.id_night_header);

        Log.d("DS05", "InformationFragment onViewCreated "
                + "isShowQRCode:" + PrefDataManager.isShowQRCode()
                + " , isShowWeather:" + PrefDataManager.isShowWeather());
        if(!PrefDataManager.isShowQRCode()) {
            mLogoView.setVisibility(View.GONE);
        } else {
            mLogoView.setVisibility(View.GONE);
        }

        if(PrefDataManager.isShowWeather()) {
            mWeatherView.setVisibility(mIsExceptionWeather ? View.GONE : View.VISIBLE);
            mExceptionWeath.setVisibility(mIsExceptionWeather ? View.VISIBLE : View.GONE);
        } else {
            mWeatherView.setVisibility(View.GONE);
            mExceptionWeath.setVisibility(View.GONE);
        }
        mViewCreateFlag = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("DS05", "InformationFragment onResume "
        + "isShowQRCode:" + PrefDataManager.isShowQRCode()
        + " , isShowWeather:" + PrefDataManager.isShowWeather());

        if(!mViewCreateFlag && mLogoView != null) {
            if (PrefDataManager.isShowQRCode()) {
                mLogoView.setVisibility(View.GONE);
            } else {
                mLogoView.setVisibility(View.GONE);
            }
        }

        if(!mViewCreateFlag && mWeatherView != null) {
            if (PrefDataManager.isShowWeather()) {
                mWeatherView.setVisibility(mIsExceptionWeather ? View.GONE : View.VISIBLE);
                mExceptionWeath.setVisibility(mIsExceptionWeather ? View.VISIBLE : View.GONE);
                if(mWeatherData != null) {
                    updateWeatherInfo(mWeatherData);
                }
            } else {
                mWeatherView.setVisibility(View.GONE);
                mExceptionWeath.setVisibility(View.GONE);
            }
        }

        mViewCreateFlag = false;
    }

    private void updateWeatherInfo(Bundle data) {
        Log.d("ZXH", "InformationFragment updateWeatherInfo:" + data.toString());
        String city = data.getString(Weather.BUNDLE_CITY);
        String dayWeather = data.getString(Weather.BUNDLE_DAY_WEATHER);
        String nightWeather = data.getString(Weather.BUNDLE_NIGHT_WEATHER);
        String dayTempr = data.getString(Weather.BUNDLE_DAY_TEMPER);
        String nightTempr = data.getString(Weather.BUNDLE_NIGHT_TEMPER);
        String dayImg = data.getString(Weather.BUNDLE_DAY_WEATHER_IMG);
        String nightImg = data.getString(Weather.BUNDLE_NIGHT_WEATHER_IMG);
        String dayWindDirection = data.getString(Weather.BUNDLE_DAY_WIND_DIRECTION);
        String nightWindDirection = data.getString(Weather.BUNDLE_NIGHT_WIND_DIRECTION);
        String dayWindPower = data.getString(Weather.BUNDLE_DAY_WIND_POWER);
        String nightWindPower = data.getString(Weather.BUNDLE_NIGHT_WIND_POWER);

        mWeatherView.setVisibility(View.VISIBLE);
        mExceptionWeath.setVisibility(View.GONE);

        mWeaterCity.setText(city);
        Bitmap dayBmp, nightBmp;
        String dayInfo = "", nightInfo = "";
        if(PrefDataManager.isShowQRCode()) {
            dayBmp = BitmapFactory.decodeFile(SinaWeather.IMAGE_PATH + dayImg);
            nightBmp = BitmapFactory.decodeFile(SinaWeather.IMAGE_PATH + nightImg);
            dayInfo = dayWeather + "\n" + dayTempr + "℃";// + "\n" + dayWindDirection + "\n" + dayWindPower + "级";
            nightInfo = nightWeather + "\n" + nightTempr + "℃";// + "\n" + nightWindDirection + "\n" + nightWindPower + "级";
            mWeaterCity.setVisibility(View.VISIBLE);
            mDayHeader.setText(R.string.string_home_day_weather);
            mNightHeader.setText(R.string.string_home_night_weather);
        } else {
            dayBmp = BitmapFactory.decodeFile(SinaWeather.IMAGE_PATH_180_180 + dayImg);
            nightBmp = BitmapFactory.decodeFile(SinaWeather.IMAGE_PATH_180_180 + nightImg);
            dayInfo = dayWeather + " " + dayTempr + "℃" + "\n" + dayWindDirection + " " + dayWindPower + "级";
            nightInfo = nightWeather + " " + nightTempr + "℃" + "\n" + nightWindDirection + " " + nightWindPower + "级";
            mWeaterCity.setVisibility(View.GONE);
            mDayHeader.setText(city + " - " + getString(R.string.string_home_day_weather));
            mNightHeader.setText(city + " - " + getString(R.string.string_home_night_weather));
        }

        mDayWeatherImg.setImageBitmap(dayBmp);
        mNightWeatherImg.setImageBitmap(nightBmp);
        mDayWeatherInfo.setText(dayInfo);
        mNightWeatherInfo.setText(nightInfo);
    }
    private void handlerExceptionWeather(int reason) {
        Log.d("DS05", "InformationFragment handlerExceptionWeather:" + reason);
        switch (reason) {
            case Weather.REASON_SERVER_EXCEPTION:
                mWeatherView.setVisibility(View.GONE);
                mExceptionWeath.setVisibility(View.VISIBLE);
                mExceptionWeath.setText(R.string.string_weather_server_exception);
                break;
            case Weather.REASON_NETWORK_EXCEPTION:
                mWeatherView.setVisibility(View.GONE);
                mExceptionWeath.setVisibility(View.VISIBLE);
                mExceptionWeath.setText(R.string.string_weather_network_exception);
                break;
            case Weather.REASON_CITY_ERROR:
                mWeatherView.setVisibility(View.GONE);
                mExceptionWeath.setVisibility(View.VISIBLE);
                mExceptionWeath.setText(R.string.string_not_get_weather_from_this_city);
                break;
        }
    }

    private static final int EVT_WEATHER_UPDATE = 0;
    private static final int EVT_WEATHER_EXCEPTION = 1;
    private Handler mInternalHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("ZXH", "InformationFragment handleMessage:" + msg.what);
                switch (msg.what) {
                case EVT_WEATHER_UPDATE: {
                    if(msg.obj != null) {
                        mWeatherData = (Bundle) msg.obj;
                        mIsExceptionWeather = false;
                        updateWeatherInfo((Bundle) msg.obj);
                    } else {
                        mIsExceptionWeather = true;
                        handlerExceptionWeather(Weather.REASON_SERVER_EXCEPTION);
                    }
                    break;
                }
                case EVT_WEATHER_EXCEPTION: {
                    mIsExceptionWeather = true;
                    handlerExceptionWeather(msg.arg1);
                }
            }
        }//handleMessage
    };
    private void registerReceiver() {
        IntentFilter fillter = new IntentFilter();
        if(PrefDataManager.isShowWeather()) {
        }
        fillter.addAction(Weather.ACTION_WEATHER_UPDATE);
        getContext().registerReceiver(mReceiver, fillter);
    }
    private void unregisterReceiver() {
        getContext().unregisterReceiver(mReceiver);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(Weather.ACTION_WEATHER_UPDATE)) {
                boolean result = intent.getBooleanExtra(Weather.EXTRA_RESULT_STATE, false);
                if(result) { //获取天气数据成功
                    Bundle data = intent.getBundleExtra(Weather.EXTRA_WEATHER_DATA);
                    if(data == null) {
                        Message msg = new Message();
                        msg.what = EVT_WEATHER_EXCEPTION;
                        msg.arg1 = Weather.REASON_SERVER_EXCEPTION;
                        mInternalHandler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = EVT_WEATHER_UPDATE;
                        msg.obj = data;
                        mInternalHandler.sendMessage(msg);
                    }
                } else { //获取天气数据失败
                    int reason = intent.getIntExtra(Weather.EXTRA_REASON, Weather.REASON_SERVER_EXCEPTION);
                    Message msg = new Message();
                    msg.what = EVT_WEATHER_EXCEPTION;
                    msg.arg1 = reason;
                    mInternalHandler.sendMessage(msg);
                }
            }//ACTION_WEATHER_UPDATE
        }
    };
}
