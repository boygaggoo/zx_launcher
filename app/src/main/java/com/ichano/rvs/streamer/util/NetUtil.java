package com.ichano.rvs.streamer.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.ichano.rvs.internal.RvsLog;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetUtil {
	private static final String TAG = "NetUtil";

	public static String getWifiIp(Context context) {
		WifiManager wm = (WifiManager) context.getSystemService("wifi");
		if (!wm.isWifiEnabled()) {
			return "0.0.0.0";
		}
		WifiInfo wi = wm.getConnectionInfo();
		int ipAdd = wi.getIpAddress();
		if (ipAdd == 0) {
			DhcpInfo dhcp = wm.getDhcpInfo();
			ipAdd = dhcp.ipAddress;
		}
		String ip = intToIp(ipAdd);

		return ip;
	}

	public static String getLocalIp() {

		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			RvsLog.e(NetUtil.class, "get ip fail:" + e.getMessage());
		}

		return "0.0.0.0";
	}

	private static String intToIp(int i) {
		return (i & 0xFF) + "." + (i >> 8 & 0xFF) + "." + (i >> 16 & 0xFF) + "." + (i >> 24 & 0xFF);
	}
}
