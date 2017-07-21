package com.ds05.launcher.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;
import com.ds05.launcher.ModuleBaseFragment;
import com.ds05.launcher.R;
import com.ds05.launcher.common.ConnectUtils;
import com.ds05.launcher.common.utils.ToastUtil;
import com.ds05.launcher.common.utils.WifiUtils;
import com.ds05.launcher.qrcode.QRCodeScanActivity;

import java.util.Locale;

import ext.system.feature.CustomizeFeature;

/**
 * Created by Chongyang.Hu on 2017/1/1 0001.
 */

public class SettingsFragment extends ModuleBaseFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_DISPLAY = "key_settings_display";
    public static final String KEY_CAMERA_SETTINGS = "key_settings_camera_settings";
    public static final String KEY_QR_CODE = "key_settings_scan_qr_code";
    public static final String KEY_DOORBELL_SETTINGS = "key_settings_doorbell_settings";
    public static final String KEY_ENERGY_SAVING_MODE = "key_Settings_energy_saving_mode";
    public static final String KEY_TIME_AND_DATE = "key_Settings_time_date";
    public static final String KEY_LANGUAGE = "key_Settings_language";
    public static final String KEY_WIFI = "key_Settings_wifi";
    public static final String KEY_BT = "key_Settings_bt";
    public static final String KEY_INTELLIGNET_LOCK = "key_Settings_intelligent_lock";
    public static final String KEY_STORE = "key_Settings_store";
    public static final String KEY_HOME_SETTINGS = "key_Settings_home_settings";
    public static final String KEY_RESET_FACTORY = "key_Settings_reset_factory";
    public static final String KEY_VERSION = "key_Settings_version";
    private static final int TIMEOUT = 18;

    private static final int NETWORKCONNECTED = 0;
    private static final int NETWORKDISCONNECTED = 1;
    private static final int NETWORKISOK = 2;

    private boolean wifiConnected = false;

    private WifiManager mWifiManager;
    private BluetoothAdapter mBluetoothAdapter;
    WifiAutoConnectManager wac;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_fragment);

        mWifiManager = (WifiManager) LauncherApplication.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        SwitchPreference energySaveingMode = (SwitchPreference) findPreference(KEY_ENERGY_SAVING_MODE);
        SwitchPreference wifiPref = (SwitchPreference) findPreference(KEY_WIFI);
        wifiPref.setOnPreferenceChangeListener(this);
        energySaveingMode.setOnPreferenceChangeListener(this);

        wac = new WifiAutoConnectManager(mWifiManager);

        boolean isSavingMode = energySaveingMode.isChecked();
        wifiPref.setEnabled(!isSavingMode);
        findPreference(KEY_BT).setEnabled(!isSavingMode);
        findPreference(KEY_INTELLIGNET_LOCK).setEnabled(!isSavingMode);
        if (isSavingMode) {
            wifiPref.setChecked(false);
            if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(false);
            }
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
        } else {
            wifiPref.setChecked(mWifiManager.isWifiEnabled());
        }

        if(mWifiManager == null) {
            getPreferenceScreen().removePreference(wifiPref);
        }
        if(mBluetoothAdapter == null) {
            getPreferenceScreen().removePreference(findPreference(KEY_BT));
        }

        initLanguage();
    }

    private void initLanguage() {
        Configuration config = getResources().getConfiguration();
        String currLang = config.locale.getLanguage() + "_" + config.locale.getCountry();
        Log.d("TEST", "currLang:" + currLang);

        if(!currLang.equals("zh_CN") && !currLang.equals("en_US")) {
            String defLang = getResources().getString(R.string.def_language);
            Locale defLocale = Locale.SIMPLIFIED_CHINESE;
            if(defLang.equals("en_US")) {
                defLocale = Locale.ENGLISH;
            }

            config.locale = defLocale;
            DisplayMetrics dm = getResources().getDisplayMetrics();
            getResources().updateConfiguration(config, dm);
            currLang = defLang;
        }

        ListPreference langPref = (ListPreference)findPreference(KEY_LANGUAGE);
        int index = 0;
        if(currLang.equals("zh_CN")) {
            index = 0;
        } else if(currLang.equals("en_US")) {
            index = 1;
        }

        langPref.setValue("" + index);
        langPref.setSummary(langPref.getEntries()[index]);
        langPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.string_system_settings);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals(KEY_QR_CODE)) {
            //startActivity(new Intent(getActivity(), QRCodeScanActivity.class));
            Intent intent = new Intent(getActivity(), QRCodeScanActivity.class);
            intent.putExtra(QRCodeScanActivity.EXTRA_REQ_REASON, QRCodeScanActivity.REASON_GET_WIFI);
            startActivityForResult(intent, QRCodeScanActivity.ACT_REQUEST_CODE_GET_WIFI);
//            Intent intent = new Intent(getActivity(), MainActivity123.class);
//            startActivity(intent);
            return true;
        } else if (key.equals(KEY_DISPLAY)) {
            jumpToFragment(new DisplaySettings());
        } else if (key.equals(KEY_CAMERA_SETTINGS)) {
            jumpToFragment(new CameraSettings());
        } else if (key.equals(KEY_DOORBELL_SETTINGS)) {
            jumpToFragment(new DoorbellSettings());
        } else if (key.equals(KEY_TIME_AND_DATE)) {
            //jumpToFragment(new TimeDateSettings());
            Intent intent = new Intent();
            intent.setAction("com.ds05.settings.DATE_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (key.equals(KEY_WIFI)) {

        } else if (key.equals(KEY_BT)) {

        } else if (key.equals(KEY_INTELLIGNET_LOCK)) {
            jumpToFragment(new IntellignetLockSetting());
        } else if (key.equals(KEY_STORE)) {
            Intent intent = new Intent();
            intent.setAction("com.ds05.settings.STORAGE_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (key.equals(KEY_HOME_SETTINGS)) {
            jumpToFragment(new HomeInfoSettings());
        } else if (key.equals(KEY_RESET_FACTORY)) {
            createDialog(getActivity(),
                    getString(R.string.settings_str_reset_factory_dialog_title),
                    getString(R.string.settings_str_reset_factory_dialog_msg),
                    getString(R.string.settings_str_reset_factory_reset_btn_txt),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Context ctx = getActivity();
                            ProgressDialog mProgressDialog = new ProgressDialog(ctx);
                            mProgressDialog.setMessage(getString(R.string.settings_str_reset_phone_wait_msg));
                            mProgressDialog.setCancelable(false);
                            mProgressDialog.setCanceledOnTouchOutside(false);
                            mProgressDialog.show();

                            CustomizeFeature.factoryReset(ctx);
                        }
                    });
        } else if (key.equals(KEY_VERSION)) {
            jumpToFragment(new VersionInformation());
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(KEY_ENERGY_SAVING_MODE)) {
            SwitchPreference saveingPref = (SwitchPreference) preference;
            SwitchPreference wifiPref = (SwitchPreference) findPreference(KEY_WIFI);
            boolean isChecked = (boolean) newValue;
            if (isChecked) {
                wifiPref.setChecked(false);
                if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(false);
                }
                if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                }
            }
            wifiPref.setEnabled(!isChecked);
            Preference btPref = findPreference(KEY_BT);
            if(btPref != null) {
                btPref.setEnabled(!isChecked);
            }
            findPreference(KEY_INTELLIGNET_LOCK).setEnabled(!isChecked);
        } else if(key.equals(KEY_WIFI)) {
            if (mWifiManager != null) {
                boolean currWifi = mWifiManager.isWifiEnabled();
                boolean newState = (boolean)newValue;
                if(currWifi != newState) {
                    mWifiManager.setWifiEnabled(newState);
                }
            }
        } else if (key.equals(KEY_LANGUAGE)) {
            Log.d("TEST", "onPreferenceChange  KEY_LANGUAGE");
//            Configuration config = getResources().getConfiguration();
//            String currLang = config.locale.getLanguage() + "_" + config.locale.getCountry();

            int index = Integer.parseInt((String)newValue);
            CharSequence[] langs = getResources().getStringArray(R.array.array_langs);
//            if(currLang.equals(langs[index])) {
//                return true;
//            }

            Locale locale = Locale.SIMPLIFIED_CHINESE;
            if(index != 0) {
                locale = Locale.ENGLISH;
            }
//            DisplayMetrics dm = getResources().getDisplayMetrics();
//            getResources().updateConfiguration(config, dm);

            CustomizeFeature.updateLocale(getActivity(), locale);

            ListPreference langPref = (ListPreference)preference;
            langPref.setValue("" + index);
            langPref.setSummary(langPref.getEntries()[index]);
        }
        return true;
    }

    private ProgressDialog mWaitingDialog;

    private void showWaitDialog() {
        if (mWaitingDialog != null && mWaitingDialog.isShowing()) return;
        if (mWaitingDialog == null) {
            mWaitingDialog = new ProgressDialog(getActivity());
            mWaitingDialog.setMessage(getString(R.string.waiting));
            mWaitingDialog.setIndeterminate(true);
            mWaitingDialog.setCancelable(false);
            mWaitingDialog.setCanceledOnTouchOutside(false);
        }
        mWaitingDialog.show();
    }

    private void dismissDlg() {
        if (mWaitingDialog != null && mWaitingDialog.isShowing()) {
            mWaitingDialog.dismiss();
        }
    }

    String userId = null, ssid = null, pwd = null;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case QRCodeScanActivity.ACT_REQUEST_CODE_GET_WIFI: {
                if(resultCode != Activity.RESULT_OK) return;

                String result = data.getStringExtra(QRCodeScanActivity.EXTRA_QRCODE_RESULT);
                if(TextUtils.isEmpty(result)) return;

                final String[] wifiInfo = result.split(",");
                if(wifiInfo == null || (wifiInfo.length != 2 && wifiInfo.length != 3)) return;

                if(wifiInfo.length == 2) {
                    ssid = wifiInfo[0];
                    pwd = wifiInfo[1];
                } else if(wifiInfo.length == 3) {
                    userId = wifiInfo[0];
                    ssid = wifiInfo[1];
                    pwd = wifiInfo[2];
                    if(ConnectUtils.NETWORK_IS_OK  && ConnectUtils.CONNECT_SERVER_STATUS){
                        Message message = new Message();
                        message.what = NETWORKISOK;
                        mHandler.sendMessage(message);

                        Intent intent = new Intent();
                        intent.setAction("com.ds05.Broadcast.ToServer.NOTIFY_QRCODE_RESULT");
                        intent.putExtra("QRCodeResult_UserId", userId);
                        intent.putExtra("QRCodeResult_WifiSSID", ssid);
                        intent.putExtra("QRCodeResult_WifiPassword", pwd);
                        getActivity().sendBroadcast(intent);
                    }else{
                        wac.connect(ssid, pwd, pwd.equals("")? WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS: WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
                        showWaitDialog();
                        Thread mThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                wifiConnected = true;
                                int timeout = 0;

                                while (!ConnectUtils.NETWORK_IS_OK || !ConnectUtils.CONNECT_SERVER_STATUS) {
                                    try {
                                        // 为了避免程序一直while循环，让它睡个100毫秒检测……
                                        timeout++;
                                        if(timeout > TIMEOUT){
                                            wifiConnected = false;
                                            break;
                                        }
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ie) {
                                    }
                                }
                                Log.d("ZXH","###  wifiConnected = " + wifiConnected);

                                dismissDlg();
                                if(wifiConnected){
                                    Message message = new Message();
                                    message.what = NETWORKCONNECTED;
                                    mHandler.sendMessage(message);
                                    Intent intent = new Intent();
                                    intent.setAction("com.ds05.Broadcast.ToServer.NOTIFY_QRCODE_RESULT");
                                    intent.putExtra("QRCodeResult_UserId", userId);
                                    intent.putExtra("QRCodeResult_WifiSSID", ssid);
                                    intent.putExtra("QRCodeResult_WifiPassword", pwd);
                                    getActivity().sendBroadcast(intent);
                                }else{
                                    Message message = new Message();
                                    message.what = NETWORKDISCONNECTED;
                                    mHandler.sendMessage(message);
                                }
                            }
                        });
                        mThread.start();
                    }

//                    Intent intent = new Intent();
//                    intent.setAction("com.ds05.Broadcast.ToServer.NOTIFY_QRCODE_RESULT");
//                    intent.putExtra("QRCodeResult_UserId", userId);
//                    intent.putExtra("QRCodeResult_WifiSSID", ssid);
//                    intent.putExtra("QRCodeResult_WifiPassword", pwd);
//                    getActivity().sendBroadcast(intent);
                }
//                if(ssid == null) return;
//
//                final String ssidF = ssid, pwdF = pwd;
//                createDialog(getActivity(),
//                        getString(R.string.string_connect_wifi),
//                        getString(R.string.string_ap_name) + " " + ssid + "\n"
//                            + getString(R.string.string_pwd) + " " + pwd,
//                        getString(R.string.string_connect),
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                WifiUtils wifiUtils = new WifiUtils(getActivity());
//                                wifiUtils.connectWifi(ssidF, pwdF);
//                            }
//                        });
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NETWORKCONNECTED:
                    ToastUtil.showToast(getActivity(), getActivity().getResources().getString(R.string.wifi_connected));
                    break;
                case NETWORKDISCONNECTED:
                    ToastUtil.showToast(getActivity(), getActivity().getResources().getString(R.string.wifi_disconnected));
                    break;
                case NETWORKISOK:
                    ToastUtil.showToast(getActivity(), getActivity().getResources().getString(R.string.not_configure_wifi));
                    break;
                default:
                    break;
            }
        }
    };


    public static boolean isWifiConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true ;
        }
        return false ;
    }

    private void createDialog(final Context ctx, String title, String msg,
                              String btnTxt, DialogInterface.OnClickListener l) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
        dialog.setTitle(title)
            .setMessage(msg)
            .setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
            .setPositiveButton(btnTxt, l)
            .show();
    }
}
