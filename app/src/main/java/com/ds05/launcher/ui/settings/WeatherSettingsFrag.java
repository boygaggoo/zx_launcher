package com.ds05.launcher.ui.settings;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.common.manager.WeatherCityDataManager;
import com.ds05.launcher.weather.SinaWeather;
import com.ds05.launcher.weather.Weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chongyang.Hu on 2017/1/14 0014.
 */

public class WeatherSettingsFrag extends ModuleBaseFragment
    implements AdapterView.OnItemClickListener{

    private TextView mSelectCity;
    private GridView mGridView;

    private View mCitySettingView, mWeatherInfoView;
    private TextView mDayWeatherInfo;
    private ImageView mDayWeatherImg;
    private TextView mNightWeatherInfo;
    private ImageView mNightWeatherImg;
    private TextView mExceptionWeath;
    private View mWeatherView;
    private TextView mCurrCity;

    private ArrayAdapter mAdapter;
    private List<String> mCityListData = new ArrayList<String>();
    private List<String> mCityCode = new ArrayList<String>();

    private String mProvince, mCity, mTown;
    private Step mStep = Step.Province;

    private enum Step {
        Province, City, Town
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showTitleBar();
        setTitle(R.string.home_settings_weather_setting);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather_settings_frag, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDayWeatherImg = (ImageView)view.findViewById(R.id.id_day_weather_img);
        mDayWeatherInfo = (TextView)view.findViewById(R.id.id_day_weather_text);
        mNightWeatherImg = (ImageView)view.findViewById(R.id.id_night_weather_img);
        mNightWeatherInfo = (TextView)view.findViewById(R.id.id_night_weather_text);
        mWeatherView = view.findViewById(R.id.id_weather_view);
        mExceptionWeath = (TextView)view.findViewById(R.id.id_exception_weather);
        mCurrCity = (TextView)view.findViewById(R.id.id_curr_city);

        mCitySettingView = view.findViewById(R.id.id_weather_city_setting);
        mWeatherInfoView = view.findViewById(R.id.id_weather_setting_display);
        mCitySettingView.setVisibility(View.GONE);
        mWeatherInfoView.setVisibility(View.VISIBLE);
        Button btn = (Button)view.findViewById(R.id.id_change_weather_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityList();
            }
        });

        mSelectCity = (TextView)view.findViewById(R.id.id_curr_select);
        mGridView = (GridView)view.findViewById(R.id.id_city_list);

        mAdapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mCityListData);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);

        Button goBack = (Button)view.findViewById(R.id.id_back_btn);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mStep) {
                    case Province:
                        showWeather();
                        break;
                    case City:
                        mStep = Step.Province;
                        mCity = null;
                        sendEmptyMessage(EVT_LOAD_PROVINCE);
                        break;
                    case Town:
                        mStep = Step.City;
                        mTown = null;
                        sendEmptyMessage(EVT_LOAD_CITY);
                        break;
                }
            }
        });
        Button done = (Button)view.findViewById(R.id.id_done_btn);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStep == Step.Province) {
                    showWeather();
                    return;
                }

                String ret = mProvince;
                if(mCity != null) {
                    ret += "," + mCity;
                }

                PrefDataManager.setWeatherUpdateCity(ret);
                getActivity().sendBroadcast(new Intent(Weather.ACTION_SET_WEATHER_CITY));
                showWeather();
            }
        });

        showWeather();
    }

    private void showWeather() {
        mCitySettingView.setVisibility(View.GONE);
        mWeatherInfoView.setVisibility(View.VISIBLE);
        String city = PrefDataManager.getWeatherUpdateCity();
        Log.d("DS05", "[WeatherSettingsFrag][showWeather]city:" + city);
        mCurrCity.setText(city);
        mSelectCity.setText("");
        registerReceiver();
    }
    private void showCityList() {
        unregisterReceiver();
        mSelectCity.setText("");
        mCitySettingView.setVisibility(View.VISIBLE);
        mWeatherInfoView.setVisibility(View.GONE);
        mStep = Step.Province;
        mProvince = mCity = mTown = null;
        sendEmptyMessage(EVT_LOAD_PROVINCE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver();
        } catch (Exception e) {
            Log.d("DS05", "[WeatherSettingsFrag]onDestroy ERROR:" + e.getMessage());
        }
    }

    private void sendEmptyMessage(int what) {
        mInternalHandler.sendEmptyMessage(what);
    }
    private void sendMessage(int what, String obj) {
        mInternalHandler.obtainMessage(what, obj).sendToTarget();
    }

    private void registerReceiver() {
        IntentFilter fillter = new IntentFilter();
        fillter.addAction(Weather.ACTION_WEATHER_UPDATE);
        fillter.addAction(Weather.ACTION_WEATHER_SET_RESP);
        getActivity().registerReceiver(mReceiver, fillter);
    }
    private void unregisterReceiver() {
        getActivity().unregisterReceiver(mReceiver);
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
            else if(action.equals(Weather.ACTION_WEATHER_SET_RESP)) {
                showWeather();
                hideProgressDialog();
            }
        }
    };

    private static final int EVT_SHOW_PROGRESS_DIALOG = 0;
    private static final int EVT_HIDE_PROGRESS_DIALOG = 1;
    private static final int EVT_LOAD_PROVINCE = 2;
    private static final int EVT_LOAD_CITY = 3;
    private static final int EVT_LOAD_TOWN = 4;
    private static final int EVT_UPDATE_LIST = 5;
    private static final int EVT_UPDATE_WEATHER_FROM_NEW_CITY = 6;
    private static final int EVT_WEATHER_UPDATE = 7;
    private static final int EVT_WEATHER_EXCEPTION = 8;
    private Handler mInternalHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case EVT_SHOW_PROGRESS_DIALOG:
                    showProgressDialog();
                    break;
                case EVT_HIDE_PROGRESS_DIALOG:
                    hideProgressDialog();
                    break;
                case EVT_LOAD_PROVINCE:
                    loadProvince();
                    break;
                case EVT_LOAD_CITY:
                    loadCity((String)msg.obj);
                    break;
                case EVT_LOAD_TOWN:
                    loadTown((String)msg.obj);
                    break;
                case EVT_UPDATE_LIST:
                    mAdapter.notifyDataSetChanged();
                    sendEmptyMessage(EVT_HIDE_PROGRESS_DIALOG);
                    break;
                case EVT_UPDATE_WEATHER_FROM_NEW_CITY:
                    showProgressDialog();
                    showWeather();
                    getActivity().sendBroadcast(new Intent(Weather.ACTION_SET_WEATHER_CITY));
                    break;
                case EVT_WEATHER_UPDATE:
                    if(msg.obj != null) {
                        updateWeatherInfo((Bundle) msg.obj);
                    } else {
                        handlerExceptionWeather(Weather.REASON_SERVER_EXCEPTION);
                    }
                    break;
                case EVT_WEATHER_EXCEPTION:
                    handlerExceptionWeather(msg.arg1);
                    break;
            }
        }//handleMessage
    };

    private void updateWeatherInfo(Bundle data) {
        Log.d("DS05", "InformationFragment updateWeatherInfo:" + data.toString());
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

        Bitmap bmp = BitmapFactory.decodeFile(SinaWeather.IMAGE_PATH_180_180 + dayImg);
        mDayWeatherImg.setImageBitmap(bmp);
        bmp = BitmapFactory.decodeFile(SinaWeather.IMAGE_PATH_180_180 + nightImg);
        mNightWeatherImg.setImageBitmap(bmp);

        mDayWeatherInfo.setText(dayWeather + " " + dayTempr + "℃" + "\n" + dayWindDirection + " " + dayWindPower + "级");
        mNightWeatherInfo.setText(nightWeather + " " + nightTempr + "℃" + "\n" + nightWindDirection + " " + nightWindPower + "级");
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

    private ProgressDialog mProgressDialog = null;
    private void showProgressDialog() {
        if(mProgressDialog != null) {
            hideProgressDialog();
        }
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources()
                .getString(R.string.string_city_load_dialog_msg));
        mProgressDialog.show();
    }
    private void hideProgressDialog() {
        if(mProgressDialog == null) return;
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }

    private void loadProvince() {
        sendEmptyMessage(EVT_SHOW_PROGRESS_DIALOG);
        new Thread("load-province") {
            @Override
            public void run() {
                WeatherCityDataManager wcdm = new WeatherCityDataManager(
                        LauncherApplication.getContext());
                List<WeatherCityDataManager.City> data = wcdm.getAllProvince();
                if(data == null || data.size() == 0) {
                    sendEmptyMessage(EVT_HIDE_PROGRESS_DIALOG);
                    return;
                }

                mCityListData.clear();
                mCityCode.clear();
                int count = data.size();
                for(int i = 0; i < count; i++) {
                    mCityListData.add(data.get(i).name);
                    mCityCode.add(data.get(i).code);
                }
                sendEmptyMessage(EVT_UPDATE_LIST);
            }//run
        }.start();
    }
    private void loadCity(final String code) {
        sendEmptyMessage(EVT_SHOW_PROGRESS_DIALOG);
        new Thread("load-city") {
            @Override
            public void run() {
                WeatherCityDataManager wcdm = new WeatherCityDataManager(
                        LauncherApplication.getContext());
                List<WeatherCityDataManager.City> data = wcdm.getCityByCode(code);
                if(data == null || data.size() == 0) {
                    sendEmptyMessage(EVT_HIDE_PROGRESS_DIALOG);
                    return;
                }

                mCityListData.clear();
                mCityCode.clear();
                int count = data.size();
                for(int i = 0; i < count; i++) {
                    mCityListData.add(data.get(i).name);
                    mCityCode.add(data.get(i).code);
                }
                sendEmptyMessage(EVT_UPDATE_LIST);
            }//run
        }.start();
    }
    private void loadTown(final String code) {
        sendEmptyMessage(EVT_SHOW_PROGRESS_DIALOG);
        new Thread("load-town") {
            @Override
            public void run() {
                WeatherCityDataManager wcdm = new WeatherCityDataManager(
                        LauncherApplication.getContext());
                List<WeatherCityDataManager.City> data = wcdm.getTownbyCode(code);
                if(data == null || data.size() == 0) {
                    sendEmptyMessage(EVT_HIDE_PROGRESS_DIALOG);
                    return;
                }

                mCityListData.clear();
                mCityCode.clear();
                int count = data.size();
                for(int i = 0; i < count; i++) {
                    mCityListData.add(data.get(i).name);
                    mCityCode.add(data.get(i).code);
                }
                sendEmptyMessage(EVT_UPDATE_LIST);
            }//run
        }.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(mStep) {
            case Province:
                mStep = Step.City;
                mProvince = mCityListData.get(position);
                mSelectCity.append(mProvince);
                if(mProvince.equals("香港") || mProvince.equals("澳门")) {
                    mStep = Step.Town;
                    PrefDataManager.setWeatherUpdateCity(mSelectCity.getText().toString());
                    sendEmptyMessage(EVT_UPDATE_WEATHER_FROM_NEW_CITY);
                } else {
                    sendMessage(EVT_LOAD_CITY, mCityCode.get(position));
                }
                break;
            case City:
                mStep = Step.Town;
                mCity = mCityListData.get(position);
                mSelectCity.append("," + mCity);
                sendMessage(EVT_LOAD_TOWN, mCityCode.get(position));
                break;
            case Town:
                mTown = mCityListData.get(position);
                mSelectCity.append("," + mTown);
                PrefDataManager.setWeatherUpdateCity(mSelectCity.getText().toString());
                sendEmptyMessage(EVT_UPDATE_WEATHER_FROM_NEW_CITY);
                break;
        }
    }
}
