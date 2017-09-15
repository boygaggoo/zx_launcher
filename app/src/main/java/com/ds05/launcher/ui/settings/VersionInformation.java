package com.ds05.launcher.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.common.config.MyAvsHelper;
import com.ds05.launcher.common.manager.PrefDataManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.weixvn.wae.webpage.net.proxy.UpdataUntil.TAG;

/**
 * Created by Chongyang.Hu on 2017/1/18 0018.
 */

public class VersionInformation extends ModuleBaseFragment{

    public static final String KEY_MODEL = "key_model";
    public static final String KEY_SERIAL_NUMBER = "key_serial_number";
    public static final String KEY_ANDROID_VERSION = "key_android_version";
    public static final String KEY_BASEBAND_VERSION = "key_baseband_verdion";
    public static final String KEY_KERNEL_VERSION = "key_kernel_version";
    public static final String KEY_SYSTEM_VERSION = "key_system_version";
    public static final String KEY_SOFTWARE_VERSION = "key_software_version";
    public static final String key_MACADDRESS = "key_MacAddress";
    public static final String KEY_VIDEO_SERIAL_NUMBER ="key_video_serial_number";
    public static final String KEY_VIDEO_STATE = "key_video_state";


    private static final String FILENAME_PROC_VERSION = "/proc/version";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.version_info_settings);
        Preference serialNumber =  findPreference(KEY_SERIAL_NUMBER);
        getPreferenceScreen().removePreference(serialNumber);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_str_version);


        String model = Build.MODEL;
        //String serial = Build.SERIAL;
        String videoserialnumber = MyAvsHelper.zy_cid;
        boolean videostate = MyAvsHelper.haveLogin;
        String androidVer = Build.VERSION.RELEASE;
        String baseband = getBasebandVer();
        String kernel = getFormattedKernelVersion();
        String systemVer = Build.DISPLAY;
        String softwareVersion = getVersionName();
        String macaddress = getMacAddress();

        findPreference(KEY_MODEL).setSummary(model);
       // findPreference(KEY_SERIAL_NUMBER).setSummary(serial);
        findPreference(KEY_VIDEO_SERIAL_NUMBER).setSummary(videoserialnumber);
       // findPreference(KEY_VIDEO_STATE).setSummary(videostate);
        findPreference(KEY_ANDROID_VERSION).setSummary(androidVer);
        findPreference(KEY_BASEBAND_VERSION).setSummary(baseband);
        findPreference(KEY_KERNEL_VERSION).setSummary(kernel);
        findPreference(KEY_SYSTEM_VERSION).setSummary(systemVer);
        findPreference(KEY_SOFTWARE_VERSION).setSummary(softwareVersion);
        findPreference(key_MACADDRESS).setSummary(macaddress);

        if(videostate == true){
            //findPreference(KEY_VIDEO_STATE).setSummary(this.getResources().getString(R.string.string_video_state_online));
            findPreference(KEY_VIDEO_STATE).setSummary(R.string.string_video_state_online);
        }else if(videostate == false){
            findPreference(KEY_VIDEO_STATE).setSummary(R.string.string_video_state_offline);
        }
    }



     private String getMacAddress(){
     String result = "";
     String str = "";
     WifiManager wifiManager = (WifiManager) LauncherApplication.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
     WifiInfo wifiInfo = wifiManager.getConnectionInfo();
     str = wifiInfo.getMacAddress();
     result = str.replaceAll(":","");
     Log.i(TAG, "macAdd:" + result);
     return result;
     }



    private String getVersionName()
    {
        PackageManager packageManager = getActivity().getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getActivity().getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) { }
        String version = packInfo.versionName;
        return version;
    }

    public static String getBasebandVer() {
        String Version = "";
        try {
            Class cl = Class.forName("android.os.SystemProperties");
            Object invoker = cl.newInstance();
            Method m = cl.getMethod("get", new Class[]{String.class, String.class});
            Object result = m.invoke(invoker, new Object[]{"gsm.version.baseband", "unknow"});
            Version = (String) result;
        } catch (Exception e) {
        }
        return Version;
    }

    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            Log.e("DS05",
                    "IO Exception when getting kernel version for Device Info screen",
                    e);

            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
                "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
                        "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
                        "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
                        "(#\\d+) " +              /* group 3: "#1" */
                        "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
                        "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e("DS05", "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.e("DS05", "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        }
        return m.group(1) + "\n" +                 // 3.0.31-g6fb96c9
                m.group(2) + " " + m.group(3) + "\n" + // x@y.com #1
                m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
    }

    AlertDialog mDialog;
    long[] mHints = new long[5];
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals(KEY_SOFTWARE_VERSION)) {
            System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
            mHints[mHints.length - 1] = SystemClock.uptimeMillis();
            if (SystemClock.uptimeMillis() - mHints[0] <= 1000) {
                if(mDialog != null && mDialog.isShowing()){
                    return true;
                }

                int val = PrefDataManager.getServerType();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.choose_server);

                DialogInterface.OnClickListener btnListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                PrefDataManager.setServerType(which);
                            }
                        };
                String[] category_names;
                category_names= getActivity().getResources().getStringArray(R.array.server_type);
                builder.setSingleChoiceItems(category_names, val, btnListener);
                builder.setPositiveButton("确定", null);
                mDialog = builder.create();
                mDialog.show();
            }
        }
        return true;
    }
}
