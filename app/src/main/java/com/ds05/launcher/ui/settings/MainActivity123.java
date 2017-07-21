package com.ds05.launcher.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ds05.launcher.R;

public class MainActivity123 extends Activity {

	Button btnConnect;
	WifiManager wifiManager;
	WifiAutoConnectManager wac;
	TextView textView1;
	EditText editPwd;
	EditText editSSID;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main123);

		btnConnect = (Button) findViewById(R.id.btnConnect);
		textView1 = (TextView) findViewById(R.id.txtMessage);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wac = new WifiAutoConnectManager(wifiManager);
		
		 editPwd=(EditText) findViewById(R.id.editPwd);
		 editSSID=(EditText) findViewById(R.id.editSSID);
		
		/**wac.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 操作界面
				textView1.setText(textView1.getText()+"\n"+msg.obj+"");
				super.handleMessage(msg);
			}
		};
		 */
		btnConnect.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					wac.connect(editSSID.getText().toString(), editPwd.getText().toString(),
							editPwd.getText().toString().equals("")? WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS: WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);


				} catch (Exception e) {
					textView1.setText(e.getMessage());
				}


				while (!isWifiConnected(MainActivity123.this)) {
					try {
						// 为了避免程序一直while循环，让它睡个100毫秒检测……
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
					}
				}

			}
		});
	}

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

}
