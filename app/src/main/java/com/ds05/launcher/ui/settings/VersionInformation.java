package com.ds05.launcher.ui.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Chongyang.Hu on 2017/1/18 0018.
 */

public class VersionInformation extends ModuleBaseFragment {

    public static final String KEY_MODEL = "key_model";
    public static final String KEY_SERIAL_NUMBER = "key_serial_number";
    public static final String KEY_ANDROID_VERSION = "key_android_version";
    public static final String KEY_BASEBAND_VERSION = "key_baseband_verdion";
    public static final String KEY_KERNEL_VERSION = "key_kernel_version";
    public static final String KEY_SYSTEM_VERSION = "key_system_version";
    public static final String KEY_SOFTWARE_VERSION = "key_software_version";


    private static final String FILENAME_PROC_VERSION = "/proc/version";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.version_info_settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_str_version);


        String model = Build.MODEL;
        String serial = Build.SERIAL;
        String androidVer = Build.VERSION.RELEASE;
        String baseband = getBasebandVer();
        String kernel = getFormattedKernelVersion();
        String systemVer = Build.DISPLAY;
        String softwareVersion = getVersionName();

        findPreference(KEY_MODEL).setSummary(model);
        findPreference(KEY_SERIAL_NUMBER).setSummary(serial);
        findPreference(KEY_ANDROID_VERSION).setSummary(androidVer);
        findPreference(KEY_BASEBAND_VERSION).setSummary(baseband);
        findPreference(KEY_KERNEL_VERSION).setSummary(kernel);
        findPreference(KEY_SYSTEM_VERSION).setSummary(systemVer);
        findPreference(KEY_SOFTWARE_VERSION).setSummary(softwareVersion);
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
}
