package com.ds05.launcher.common.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.R;
import com.ds05.launcher.ui.monitor.MonitorFragment;
import com.ds05.launcher.ui.settings.CameraSettings;
import com.ds05.launcher.ui.settings.DoorbellSettings;
import com.ds05.launcher.ui.settings.HomeInfoSettings;

/**
 * Created by Chongyang.Hu on 2017/1/7 0007.
 */

public final class PrefDataManager {

    public static final String COMMON_PREF_DATA = "common_pref_data";
    public static final String APP_PREF_SETTINGS = "com.ds05.launcher_preferences";

    public static final String WEATHER_UPDATE_INTERVAL = "weather_update_interval";
    public static final String WEATHER_UPDATE_CITY = "weather_update_city";

    public static final String ALARM_SOUND_VOLUME = "alarm_sound_volume";
    public static final String DOORBELL_VOLUME = "doorbell_volume";
    public static final String SERVER_TYPE = "server_type";

    public enum AutoAlarmTime {
        Time_3sec(3000), Time_8sec(8000), Time_15sec(15000), Time_25sec(25000);

        private int time;
        AutoAlarmTime(int t) {
            time = t;
        }

        public static AutoAlarmTime get(int t) {
            switch(t) {
                case 3000:
                    return Time_3sec;
                case 8000:
                    return Time_8sec;
                case 15000:
                    return Time_15sec;
                case 25000:
                    return Time_25sec;
                default:
                    return null;
            }
        }
        public int time() {
            return time;
        }
    }//AutoAlarmTime
    public enum AlarmIntervalTime{
        Time_30sec(30000),Time_90sec(90000),Time_180sec(180000);

        private int time;
        AlarmIntervalTime(int t){ time = t;}

        public static AlarmIntervalTime get(int t){
            switch(t){
                case 30000:
                    return Time_30sec;
                case 90000:
                    return Time_90sec;
                case 180000:
                    return Time_180sec;
                default:
                    return null;
            }
        }
        public int time(){return time;}
    }//AlarmIntervalTime
    public enum MonitorSensitivity {
        High(1), Low(2);

        int sensi;
        MonitorSensitivity(int sensi) {
            this.sensi = sensi;
        }
        public int sensitivity() {
            return sensi;
        }
        public static MonitorSensitivity get(int sensi) {
            switch(sensi) {
                case 1:
                    return High;
                case 2:
                    return Low;
                default:
                    return null;
            }
        }
    }//MonitorSensitivity

    public enum AlarmMode {
        Recorder(1), Capture(0);

        private int mode;
        AlarmMode(int mode) {
            this.mode = mode;
        }
        public int mode() {
            return mode;
        }
        public static AlarmMode get(int i) {
            switch(i) {
                case 0:
                    return Capture;
                case 1:
                    return Recorder;
                default:
                    return null;
            }
        }
    }//AlarmMode
    public enum AutoAlarmSound {
        Silence(0), Alarm(1), Scream(2);
        private int sound;
        AutoAlarmSound(int i) {
            sound = i;
        }
        public static AutoAlarmSound get(int i) {
            switch(i) {
                case 0:
                    return Silence;
                case 1:
                    return Alarm;
                case 2:
                    return Scream;
                default:
                    return null;
            }
        }
        public int sound() {
            return sound;
        }
    }//AutoAlarmSound
    public enum DoorbellSound{
        ding_dang(0),ding_dong(1),dong_dong(2),ji_cu_qiao_men(3);
        private int sound_door;
        DoorbellSound(int i){
            sound_door = i;
        }
        public static DoorbellSound get(int i){
            switch (i){
                case 0:
                    return ding_dang;
                case 1:
                    return ding_dong;
                case 2:
                    return dong_dong;
                case 3:
                    return ji_cu_qiao_men;
                default:
                    return null;
            }
        }
        public int sound_door(){return sound_door;}
    }


    public static long getWeatherUpdateInterval() {
        long interval = getLong(COMMON_PREF_DATA, WEATHER_UPDATE_INTERVAL, -1);
        if(interval == -1) {
            interval = LauncherApplication.getContext()
                    .getResources().getInteger(R.integer.def_weather_update_interval_ms);
            setWeatherUpdateInterval(interval);
        }

        return interval;
    }
    public static void setWeatherUpdateInterval(long ms) {
        setLong(COMMON_PREF_DATA, WEATHER_UPDATE_INTERVAL, ms);
    }

    public static String getWeatherUpdateCity() {
        String ret = getString(COMMON_PREF_DATA, WEATHER_UPDATE_CITY, "");
        if(ret.equals("")) {
            ret = LauncherApplication.getContext()
                    .getString(R.string.def_weather_update_city);
            setWeatherUpdateCity(ret);
        }

        return ret;
    }
    public static void setWeatherUpdateCity(String city) {
        setString(COMMON_PREF_DATA, WEATHER_UPDATE_CITY, city);
    }

    public static boolean isShowQRCode() {
        Context ctx = LauncherApplication.getContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return pref.getBoolean(HomeInfoSettings.KEY_SHOW_QRCODE,
                ctx.getResources().getBoolean(R.bool.def_home_show_qrcode));
    }
    public static boolean isShowWeather() {
        Context ctx = LauncherApplication.getContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return pref.getBoolean(HomeInfoSettings.KEY_SHOW_WEATHER,
                ctx.getResources().getBoolean(R.bool.def_home_show_weather));
    }

    public static int getAlarmSoundIndex() {
        Context ctx = LauncherApplication.getContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String def = ctx.getString(R.string.def_auto_alarm_sound);
        String index = pref.getString(MonitorFragment.KEY_ALARM_SOUND, def);
        return Integer.parseInt(index);
    }
    public static int getDoorbellSoundIndex() {
        Context ctx = LauncherApplication.getContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String def = ctx.getString(R.string.def_doorbell_sound);
        String index = pref.getString(DoorbellSettings.KEY_DOORBELL_SOUND, def);
        return Integer.parseInt(index);
    }
    public static void setDoorbellSoundIndex(int i) {
        int index = -1;
        if(i == DoorbellSound.ding_dang.sound_door()) {
            index = 0;
        } else if(i == DoorbellSound.ding_dong.sound_door()) {
            index = 1;
        } else if(i == DoorbellSound.dong_dong.sound_door()) {
            index = 2;
        } else if(i == DoorbellSound.ji_cu_qiao_men.sound_door()){
            index = 3;
        }

        if(index >= 0) {
            setString(APP_PREF_SETTINGS, DoorbellSettings.KEY_DOORBELL_SOUND, index + "");
        }
    }

    public static boolean getDoorbellLight(){
        boolean def = LauncherApplication.getContext()
                .getResources().getBoolean(R.bool.def_doorbell_light);
        return getBoolean(APP_PREF_SETTINGS, DoorbellSettings.KEY_DOORBELL_LIGHT, def);
    }
    public static void setDoorbellLight(boolean state){
        setBoolean(APP_PREF_SETTINGS, DoorbellSettings.KEY_DOORBELL_LIGHT, state);
    }
    public static float getAlarmSoundVolume() {
        return getFloat(COMMON_PREF_DATA, ALARM_SOUND_VOLUME, 1.0f);
    }
    public static void setAlarmSoundVolume(float val) {
        if(val < 0) {
            throw new IllegalArgumentException("Invalide volume: " + val);
        }
        setFloat(COMMON_PREF_DATA, ALARM_SOUND_VOLUME, val);
    }
    public static float getDoorbellVolume() {
        return getFloat(COMMON_PREF_DATA, DOORBELL_VOLUME, 1.0f);
    }
    public static void setDoorbellVolume(float val) {
        if(val < 0) {
            throw new IllegalArgumentException("Invalide volume: " + val);
        }
        setFloat(COMMON_PREF_DATA, DOORBELL_VOLUME, val);
    }
    public static void setHumanMonitorState(boolean state) {
        setBoolean(APP_PREF_SETTINGS, MonitorFragment.KEY_HUMAN_MONIOTOR, state);
    }
    public static boolean getHumanMonitorState() {
        boolean def = LauncherApplication.getContext()
                .getResources().getBoolean(R.bool.def_human_monitor_value);
        return getBoolean(APP_PREF_SETTINGS, MonitorFragment.KEY_HUMAN_MONIOTOR, def);
    }
    public static long getAutoAlarmTime() {
        String defIndex = LauncherApplication.getContext()
                .getResources().getString(R.string.def_intelligent_alarm_time);
        String ret = getString(APP_PREF_SETTINGS, MonitorFragment.KEY_INTELL_ALARM_TIME, defIndex);
        int index = Integer.parseInt(ret);
        if(index < 0) {
            return LauncherApplication.getContext()
                    .getResources().getIntArray(R.array.array_auto_alarm_time)[Integer.parseInt(defIndex)];
        } else {
            return LauncherApplication.getContext()
                    .getResources().getIntArray(R.array.array_auto_alarm_time)[index];
        }
    }
    public static int getAutoAlarmTimeIndex() {
        String def = LauncherApplication.getContext()
                .getResources().getString(R.string.def_intelligent_alarm_time);
        return Integer.parseInt(getString(APP_PREF_SETTINGS, MonitorFragment.KEY_INTELL_ALARM_TIME, def));

    }
    public static void setAutoAlarmTime(long time) {
        int[] defs = LauncherApplication.getContext()
                .getResources().getIntArray(R.array.array_auto_alarm_time);
        int i = 0;
        for(; i < defs.length; i++) {
            if(defs[i] == time) break;
        }

        Log.d("pp","########### time = " + time);
        Log.d("pp","########### i = " + i);

        if(i >= 0 && i < defs.length && defs[i] == time) {
            Log.d("pp","###########setString");
            setString(APP_PREF_SETTINGS, MonitorFragment.KEY_INTELL_ALARM_TIME, i + "");
        }
    }
    public static long getAlarmIntervalTime() {
        String defIndex = LauncherApplication.getContext()
                .getResources().getString(R.string.def_alarm_interval_time);
        String ret = getString(APP_PREF_SETTINGS, MonitorFragment.KEY_ALARM_INTERVAL_TIME, defIndex);
        int index = Integer.parseInt(ret);
        if(index < 0) {
            return LauncherApplication.getContext()
                    .getResources().getIntArray(R.array.array_alarm_interval_time)[Integer.parseInt(defIndex)];
        } else {
            return LauncherApplication.getContext()
                    .getResources().getIntArray(R.array.array_alarm_interval_time)[index];
        }
    }
    public static void setAlarmIntervalTime(long time) {
        int[] defs = LauncherApplication.getContext()
                .getResources().getIntArray(R.array.array_alarm_interval_time);
        int i = 0;
        for(; i < defs.length; i++) {
            if(defs[i] == time) break;
        }

        if(i >= 0 && i < defs.length && defs[i] == time) {
            setString(APP_PREF_SETTINGS, MonitorFragment.KEY_ALARM_INTERVAL_TIME, i + "");
        }
    }
    public static MonitorSensitivity getHumanMonitorSensi() {
        String index = LauncherApplication.getContext()
                .getResources().getString(R.string.def_mimonitoring_sensitivity);

        int sensi = Integer.parseInt(getString(APP_PREF_SETTINGS,
                MonitorFragment.KEY_MONITORING_SENS, index));

        if(sensi == 0)
            return MonitorSensitivity.High;
        else if(sensi == 1) {
            return MonitorSensitivity.Low;
        } else {
            return null;
        }
    }
    public static void setHumanMonitorSensi(int sensi) {
        int index = -1;
        if(sensi == MonitorSensitivity.High.sensitivity())
            index = 0;
        else if(sensi == MonitorSensitivity.Low.sensitivity())
            index = 1;

        if(index >= 0) {
            setString(APP_PREF_SETTINGS, MonitorFragment.KEY_MONITORING_SENS, index + "");
        }
    }
    public static AlarmMode getAlarmMode() {
        String index = LauncherApplication.getContext()
                .getResources().getString(R.string.def_shooting_mode);
        int tmp = Integer.parseInt(getString(APP_PREF_SETTINGS, MonitorFragment.KEY_SHOOTING_MODE, index));
        if(tmp == 0)
            return AlarmMode.Capture;
        else
            return AlarmMode.Recorder;
    }
    public static void setAlarmMode(int mode) {
        int index = -1;
        if(mode == AlarmMode.Capture.mode())
            //index = 0;
            index = 0;
        else if(mode == AlarmMode.Recorder.mode())
           // index = 2;
            index = 1;
        Log.d("pp"," index = " + index);
        if(index >= 0) {
            setString(APP_PREF_SETTINGS, MonitorFragment.KEY_SHOOTING_MODE, index + "");
        }
    }
    public static int getShootNumber() {
        return getInt(COMMON_PREF_DATA, "common_shoot_number", 3);
    }
    public static void setShootNumber(int number) {
        setInt(COMMON_PREF_DATA, "common_shoot_number", number);
    }
    public static AutoAlarmSound getAlarmSound() {
        String index = LauncherApplication.getContext()
                .getResources().getString(R.string.def_auto_alarm_sound);
        int tmp = Integer.parseInt(getString(APP_PREF_SETTINGS, MonitorFragment.KEY_ALARM_SOUND, index));
        if(tmp == 0)
            return AutoAlarmSound.Silence;
        else if(tmp == 1)
            return AutoAlarmSound.Alarm;
        else if(tmp == 2)
            return AutoAlarmSound.Scream;
        else
            return null;
    }
    public static void setAlarmSound(int i) {
        int index = -1;
        if(i == AutoAlarmSound.Silence.sound()) {
            index = 0;
        } else if(i == AutoAlarmSound.Alarm.sound()) {
            index = 1;
        } else if(i == AutoAlarmSound.Scream.sound()) {
            index = 2;
        }

        if(index >= 0) {
            setString(APP_PREF_SETTINGS, MonitorFragment.KEY_ALARM_SOUND, index + "");
        }
    }
    public static boolean getBackLightState() {
        boolean def = LauncherApplication.getContext()
                .getResources().getBoolean(R.bool.def_backlight_light);
        return getBoolean(APP_PREF_SETTINGS, CameraSettings.KEY_BACKLIGHT_LIGHT, def);
    }
    public static int getNightFillLightSensi() {
        String index = LauncherApplication.getContext()
                .getResources().getString(R.string.def_night_vision_light_sens);
        return Integer.parseInt(getString(APP_PREF_SETTINGS, CameraSettings.KEY_NIGHT_FILL_LIGHT_SENSI, index));
    }
    public static int getLightSourceFreq() {
        String def = LauncherApplication.getContext()
                .getResources().getString(R.string.def_light_source_freq);
        return Integer.parseInt(getString(APP_PREF_SETTINGS, CameraSettings.KEY_LIGHT_SOURCE_FREQ, def));
    }

    /************ Base Method **********/
    public static float getFloat(String file, String key, float def) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getFloat(key, def);
    }
    public static void setFloat(String file, String key, float val) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putFloat(key, val);
        spe.commit();
    }
    public static long getLong(String file, String key, long def) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getLong(key, def);
    }
    public static int getInt(String file, String key, int def) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getInt(key, def);
    }
    public static String getString(String file, String key, String def) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getString(key, def);
    }
    public static void setLong(String file, String key, long val) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putLong(key, val);
        spe.commit();
    }
    public static void setLong(SharedPreferences sp, String key, long val) {
        SharedPreferences.Editor spe = sp.edit();
        spe.putLong(key, val);
        spe.commit();
    }
    public static void setInt(String file, String key, int val) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(key, val);
        spe.commit();
    }
    public static void setInt(SharedPreferences sp, String key, int val) {
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(key, val);
        spe.commit();
    }
    public static void setString(String file, String key, String val) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(key, val);
        spe.commit();
    }
    public static void setString(SharedPreferences sp, String key, String val) {
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(key, val);
        spe.commit();
    }
    public static void setBoolean(String file, String key, boolean val) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putBoolean(key, val);
        spe.commit();
    }
    public static boolean getBoolean(String file, String key, boolean def) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(file, Context.MODE_PRIVATE);
        return sp.getBoolean(key, def);
    }

    public static int getServerType() {
        return getInt(COMMON_PREF_DATA, SERVER_TYPE, 0);
    }
    public static void setServerType(int val) {
        if(val < 0 || val > 1) {
            throw new IllegalArgumentException("Invalide volume: " + val);
        }
        setInt(COMMON_PREF_DATA, SERVER_TYPE, val);
    }
}
