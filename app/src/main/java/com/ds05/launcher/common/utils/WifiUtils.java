package com.ds05.launcher.common.utils;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ds05.launcher.R;
import com.ds05.launcher.common.WifiAdmin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WifiUtils {
    private static final String TAG = "DS05/WifiUtils";

    private Context mContext;
    private WifiManager mWifiManager;

    public WifiUtils(Context ctx) {
        mContext = ctx;
        mWifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isWifiEnable() {
        return mWifiManager.isWifiEnabled();
    }

    public boolean enableWifi(boolean isEnable) {
        return mWifiManager.setWifiEnabled(isEnable);
    }

    public void connectWifi(String ssid, String pwd) {
//        if(!isWifiEnable()) return;

        Log.d(TAG, "connectWifi");
        new ConnectWifiTask().execute(new String[]{ssid, pwd});
    }


    /** These values are matched in string arrays -- changes must be kept in sync */
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;
    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    public static String addQuote(String s) {
        return "\"" + s + "\"";
    }

    private WifiConfiguration getConfig(int security, String SSID, String PWD, String bssid) {
        WifiConfiguration config = new WifiConfiguration();

        config.SSID = addQuote(SSID);
        config.hiddenSSID = true;
        if(!TextUtils.isEmpty(bssid)) {
            config.BSSID = bssid;
        }

        switch (security) {
            case SECURITY_NONE:
                Log.d(TAG, "SECURITY_NONE");
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case SECURITY_WEP:
                Log.d(TAG, "SECURITY_WEP");
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (PWD != null && PWD.length() != 0) {
                    int length = PWD.length();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            PWD.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = PWD;
                    } else {
                        config.wepKeys[0] = '"' + PWD + '"';
                    }
                }
                break;

            case SECURITY_PSK:
                Log.d(TAG, "SECURITY_PSK");
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

                //config.status = WifiConfiguration.Status.ENABLED;

                if (PWD != null && PWD.length() != 0) {
                    if (PWD.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = PWD;
                    } else {
                        config.preSharedKey = '"' + PWD + '"';
                    }
                }
                break;

            case SECURITY_EAP:
                /* Not support */
                Log.d(TAG, "{getConfig}SECURITY_EAP. This security not support");
                break;
            default:
                return null;
        }
        return config;
    }

    private static int getSecurity(ScanResult result) {

        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }
    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Log.w(TAG, "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

    private class ConnectWifiTask extends AsyncTask<String, String, Boolean> {
        private ProgressDialog mmProgressDialog;
        private ReentrantLock mLock = new ReentrantLock();
        private Condition mScanResultAvailable = mLock.newCondition();
        private Condition mWaitStatus = mLock.newCondition();
        private Condition mWaitWifiEnable = mLock.newCondition();

        private NetworkInfo.DetailedState mState = NetworkInfo.DetailedState.IDLE;
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            mmProgressDialog = new ProgressDialog(mContext);
            mmProgressDialog.setMessage("");
            mmProgressDialog.show();
            registerReceiver();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground params" + params);
            if(params == null || params.length < 2)
                throw new IllegalArgumentException("Invalide params");

            String ssid = params[0];
            String pwd = params[1];
            if (TextUtils.isEmpty(ssid)) return false;

            ScanResult targetAP = null;
            boolean ret = false;

            try {
                mLock.lock();

                if(!isWifiEnable()) {
                    publishProgress(mContext.getString(R.string.string_wait_open_wifi));
                    enableWifi(true);
                    try {
                        if (!mWaitWifiEnable.await(10000, TimeUnit.MILLISECONDS)) {
                            Log.d(TAG, "{ConnectWifiTask}{doInBackground}Enable WIFI timeout");
                            return false;
                        }
                    } catch (InterruptedException e) {
                    }
                }

                publishProgress(String.format(mContext.getString(R.string.string_searching_wifi), ssid));
                mWifiManager.startScan();

                int foundAPCount = 0;
                while (true) {
                    try {
                        Log.d(TAG, "{ConnectWifiTask}{doInBackground}await 30 * 1000");
                        if (!mScanResultAvailable.await(30 * 1000, TimeUnit.MILLISECONDS)) {
                            Log.d(TAG, "{ConnectWifiTask}{doInBackground}wait timeout");
                            return false;
                        }
                    } catch (InterruptedException e) {
                        Log.d(TAG, "{ConnectWifiTask}{doInBackground}InterruptedException:" + e.getMessage());
                    }

                    Log.d(TAG, "{ConnectWifiTask}{doInBackground}Find scan result...");
                    List<ScanResult> scanResults = mWifiManager.getScanResults();
                    if (scanResults == null || scanResults.size() == 0) continue;

                    int count = scanResults.size();
                    for (int i = 0; i < count; i++) {
                        ScanResult result = scanResults.get(i);
                        Log.d(TAG, "{ConnectWifiTask}{doInBackground}SSID:{"
                                + result.SSID + "} ssid:{" + ssid + "}"
                                + (result.SSID != null) + ","
                                + (result.SSID.equals(ssid)));
                        if (result.SSID != null && result.SSID.equals(ssid)) {
                            ret = true;
                            targetAP = result;
                            Log.d(TAG, "{ConnectWifiTask}{doInBackground}Found result");
                            break;
                        }
                    }

                    if (ret) {
                        Log.d(TAG, "{ConnectWifiTask}{doInBackground}Found result:{" + targetAP + "}. break while true");
                        break;
                    } else {
                        foundAPCount ++;
                        if(foundAPCount >= 20) {
                            Log.d(TAG, "{ConnectWifiTask}{doInBackground}Not found wifi AP{" + ssid + "}");
                            ret = false;
                            targetAP = null;
                        }
                    }
                }//while(true)

                if (!ret || targetAP == null) return false;

                Log.d(TAG, "{ConnectWifiTask}{doInBackground}Connect wifi...");
                publishProgress(mContext.getString(R.string.string_wifi_connecting));
                int securityType = getSecurity(targetAP);
                Log.d(TAG, "{ConnectWifiTask}{doInBackground}Connect wifi. securityType:" + securityType);
                //WifiConfiguration config = getConfig(securityType, ssid, pwd, targetAP.BSSID);
                WifiAdmin wifiAdmin = new WifiAdmin(mContext);
                WifiConfiguration config = wifiAdmin.createWifiInfo(ssid, pwd, securityType);
                int netID = mWifiManager.addNetwork(config);
                //mWifiManager.removeNetwork(netID);
                if(!mWifiManager.enableNetwork(netID, true)) {
                    return false;
                }

                try {
                    mWaitStatus.await();
                } catch (InterruptedException e) {
                }
                if (mState != NetworkInfo.DetailedState.CONNECTED) {
                    Log.d(TAG, "{ConnectWifiTask}{doInBackground}mState != CONNECTED");
                    ret = false;
                }
            } finally {
                mLock.unlock();
            }
            return ret;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values != null && values.length > 0) {
                mmProgressDialog.setMessage(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            unregisterReceiver();
            if(mmProgressDialog != null && mmProgressDialog.isShowing()) {
                mmProgressDialog.dismiss();
            }
            mmProgressDialog = null;
            if(!aBoolean) {
                Toast.makeText(mContext, R.string.string_connect_wifi_fail, Toast.LENGTH_SHORT).show();
            }
        }

        private void registerReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

            mContext.registerReceiver(mReceiver, filter);
        }
        private void unregisterReceiver() {
            mContext.unregisterReceiver(mReceiver);
        }
        private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "{onReceive}action: " + action);
                if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    try {
                        Log.d(TAG, "{onReceive}{SCAN_RESULTS_AVAILABLE_ACTION}");
                        mLock.lock();
                        Log.d(TAG, "{onReceive}{SCAN_RESULTS_AVAILABLE_ACTION}signal");
                        mScanResultAvailable.signal();
                        Log.d(TAG, "{onReceive}{SCAN_RESULTS_AVAILABLE_ACTION}signal over");
                    } finally {
                        mLock.unlock();
                    }
                } else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    if(state == WifiManager.WIFI_STATE_ENABLED) {
                        try {
                            mLock.lock();
                            mWaitWifiEnable.signal();
                        } finally {
                            mLock.unlock();
                        }
                    }
                } else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo info = (NetworkInfo)intent
                            .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                    switch(info.getDetailedState()) {
                        case CONNECTING:
                            Log.d(TAG, "{onReceive}{NETWORK_STATE_CHANGED_ACTION}CONNECTING");
                            publishProgress(mContext.getString(R.string.string_wifi_connecting));
                            break;
                        case AUTHENTICATING:
                            Log.d(TAG, "{onReceive}{NETWORK_STATE_CHANGED_ACTION}AUTHENTICATING");
                            publishProgress(mContext.getString(R.string.string_wifi_verify));
                            break;
                        case OBTAINING_IPADDR:
                            Log.d(TAG, "{onReceive}{NETWORK_STATE_CHANGED_ACTION}OBTAINING_IPADDR");
                            publishProgress(mContext.getString(R.string.string_wifi_obtain_ip));
                            break;
                        case CONNECTED:
                            Log.d(TAG, "{onReceive}{NETWORK_STATE_CHANGED_ACTION}CONNECTED");
                            mState = NetworkInfo.DetailedState.CONNECTED;
                            try {
                                mLock.lock();
                                mWaitStatus.signal();
                            } finally {
                                mLock.unlock();
                            }
                            break;
                        case FAILED:
                            Log.d(TAG, "{onReceive}{NETWORK_STATE_CHANGED_ACTION}FAILED");
                            mState = NetworkInfo.DetailedState.FAILED;
                            try {
                                mLock.lock();
                                mWaitStatus.signal();
                            } finally {
                                mLock.unlock();
                            }
                            break;
                    }
                }
            }
        };
    }
}





































