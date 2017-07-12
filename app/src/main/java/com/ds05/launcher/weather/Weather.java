package com.ds05.launcher.weather;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 */

public final class Weather {
    public static final String ACTION_GET_WEATHER_FAIL = "com.ds05.launcher.weather.ACTION_GET_WEATHER_FAIL";
    public static final String ACTION_GET_WEATHER_SUCCESS = "com.ds05.launcher.weather.ACTION_GET_WEATHER_SUCCESS";

    public static final String ACTION_WEATHER_UPDATE = "com.ds05.launcher.weather.ACTION_WEATHER_UPDATE";
    public static final String ACTION_SET_WEATHER_CITY = "com.ds05.launcher.weather.ACTION_SET_WEATHER_CITY";
    public static final String ACTION_WEATHER_SET_RESP = "com.ds05.launcher.weather.ACTION_WEATHER_SET_RESP";

    public static final String EXTRA_RESULT_STATE = "ResultState";
    public static final String EXTRA_REASON = "Reason";
    public static final int REASON_NETWORK_EXCEPTION = 1;
    public static final int REASON_SERVER_EXCEPTION = 2;
    public static final int REASON_CITY_ERROR = 3;

    public static final String EXTRA_WEATHER_DATA = "WeatherData";
    public static final String BUNDLE_CITY = "City";
    public static final String BUNDLE_DAY_WEATHER = "DayWeather";
    public static final String BUNDLE_NIGHT_WEATHER = "NightWeather";
    public static final String BUNDLE_DAY_TEMPER = "HightTemper";
    public static final String BUNDLE_NIGHT_TEMPER = "LowTemper";
    public static final String BUNDLE_DAY_WEATHER_IMG = "DayWeatherImage";
    public static final String BUNDLE_NIGHT_WEATHER_IMG = "NightWeatherImage";
    public static final String BUNDLE_DAY_WIND_DIRECTION = "DayWindDirection";
    public static final String BUNDLE_NIGHT_WIND_DIRECTION = "NightWindDirection";
    public static final String BUNDLE_DAY_WIND_POWER = "DayWindPower";
    public static final String BUNDLE_NIGHT_WIND_POWER = "NightWindPower";
}
