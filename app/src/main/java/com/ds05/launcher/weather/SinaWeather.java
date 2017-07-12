package com.ds05.launcher.weather;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ds05.launcher.LauncherApplication;

import org.jsoup.nodes.Document;
import org.weixvn.wae.webpage.WebPage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SinaWeather extends WebPage {

	private static final String IMAGE_DOWNLOAD_URL = "http://php.weather.sina.com.cn/images/yb3/78_78/";
	public static final String IMAGE_PATH = "/sdcard/ds05/78_78/";
	private static final String IMAGE_DOWNLOAD_URL_180_180 = "http://php.weather.sina.com.cn/images/yb3/180_180/";
	public static final String IMAGE_PATH_180_180 = "/sdcard/ds05/180_180/";

	@Override
	public void onStart() {
		String city = getHtmlValue("city");
		try {
			city = URLEncoder.encode(city, "gb2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.uri = "http://php.weather.sina.com.cn/xml.php?city=" + city
				+ "&password=DJOYnieT8234jlsK&day=0";
		this.type = RequestType.GET;
	}

	@Override
	public void onSuccess(Document doc) {
		Log.d("DS05", "[SinaWeather][onSuccess]doc:" + doc);
		parserWeatherData(doc);
	}

	@Override
	public void onFailure(Throwable arg0) {
		//super.onFailure(arg0);
		Log.d("DS05", "[SinaWeather][onFailure]Get weather fail. ERROR:" + arg0.getMessage());
		Intent intent = new Intent(Weather.ACTION_GET_WEATHER_FAIL);
		intent.putExtra(Weather.EXTRA_REASON, Weather.REASON_SERVER_EXCEPTION);
		LauncherApplication.getContext().sendBroadcast(intent);
	}

	private void parserWeatherData(Document doc) {
		if (doc != null && !TextUtils.isEmpty(doc.toString())
				&& !doc.getElementsByTag("city").isEmpty()
				&& !doc.getElementsByTag("status1").isEmpty()
				&& !doc.getElementsByTag("status2").isEmpty()
				&& !doc.getElementsByTag("temperature1").isEmpty()
				&& !doc.getElementsByTag("temperature2").isEmpty()
				&& !doc.getElementsByTag("direction1").isEmpty()
				&& !doc.getElementsByTag("direction2").isEmpty()
				&& !doc.getElementsByTag("power1").isEmpty()
				&& !doc.getElementsByTag("power2").isEmpty()
				&& !doc.getElementsByTag("figure1").isEmpty()
				&& !doc.getElementsByTag("figure2").isEmpty()) {

			Bundle bundle = new Bundle();
			bundle.putString(Weather.BUNDLE_CITY, doc.getElementsByTag("city").get(0).text());
			bundle.putString(Weather.BUNDLE_DAY_WEATHER, doc.getElementsByTag("status1").get(0).text());
			bundle.putString(Weather.BUNDLE_NIGHT_WEATHER, doc.getElementsByTag("status2").get(0).text());
			bundle.putString(Weather.BUNDLE_DAY_TEMPER, doc.getElementsByTag("temperature1").get(0).text());
			bundle.putString(Weather.BUNDLE_NIGHT_TEMPER, doc.getElementsByTag("temperature2").get(0).text());
			bundle.putString(Weather.BUNDLE_DAY_WIND_DIRECTION, doc.getElementsByTag("direction1").get(0).text());
			bundle.putString(Weather.BUNDLE_NIGHT_WIND_DIRECTION, doc.getElementsByTag("direction2").get(0).text());
			bundle.putString(Weather.BUNDLE_DAY_WIND_POWER, doc.getElementsByTag("power1").get(0).text());
			bundle.putString(Weather.BUNDLE_NIGHT_WIND_POWER, doc.getElementsByTag("power2").get(0).text());

			String figure1 = doc.getElementsByTag("figure1").get(0).text() + "_0.png";
			String figure2 = doc.getElementsByTag("figure2").get(0).text() + "_1.png";

			if(!fileExist(figure1)) {
				downloadImage(figure1);
			}
			if(!fileExist(figure2)) {
				downloadImage(figure2);
			}
			bundle.putString(Weather.BUNDLE_DAY_WEATHER_IMG, figure1);
			bundle.putString(Weather.BUNDLE_NIGHT_WEATHER_IMG, figure2);

			Log.d("DS05", "[SinaWeather][parserWeatherData]" + bundle.toString());

			Intent successIntent = new Intent(Weather.ACTION_GET_WEATHER_SUCCESS);
			LauncherApplication.getContext().sendBroadcast(successIntent);

			Intent intent = new Intent(Weather.ACTION_WEATHER_UPDATE);
			intent.putExtra(Weather.EXTRA_RESULT_STATE, true);
			intent.putExtra(Weather.EXTRA_WEATHER_DATA, bundle);
			LauncherApplication.getContext().sendStickyBroadcast(intent);
		} else {
			Log.d("DS05", "[SinaWeather][parserWeatherData]Get weather fail. XML data struct not match");
			Intent intent = new Intent(Weather.ACTION_GET_WEATHER_FAIL);
			intent.putExtra(Weather.EXTRA_REASON, Weather.REASON_CITY_ERROR);
			LauncherApplication.getContext().sendBroadcast(intent);
		}
	}

	private void downloadImage(String imageName) {
		Log.d("DS05", "downloadImage: " + IMAGE_DOWNLOAD_URL + imageName);
		try {
			byte[] data = getImage(IMAGE_DOWNLOAD_URL + imageName);
			if (data != null) {
				Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
				saveFile(bmp, IMAGE_PATH, imageName);
			} else {
				Log.d("DS05", "downloadImage FAIL");
			}

			data = getImage(IMAGE_DOWNLOAD_URL_180_180 + imageName);
			if (data != null) {
				Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
				saveFile(bmp, IMAGE_PATH_180_180, imageName);
			} else {
				Log.d("DS05", "downloadImage FAIL");
			}
		}catch (Exception e) {
			Log.d("DS05", "downloadImage ERROR:" + e.getMessage());
		}
	}

	public void saveFile(Bitmap bm, String path, String fileName) throws IOException {
		File dirFile = new File(path);
		if(!dirFile.exists()){
			dirFile.mkdir();
		}
		File myCaptureFile = new File(path + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
		bm.compress(Bitmap.CompressFormat.PNG, 80, bos);
		bos.flush();
		bos.close();
	}

	public byte[] getImage(String path) throws Exception{
		Log.d("DS05", "getImage ");
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		InputStream inStream = conn.getInputStream();
		if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
			Log.d("DS05", "getImage OK:" + path);
			return readStream(inStream);
		}
		Log.d("DS05", "getImage FAIL:" + path);
		return null;
	}

	public static byte[] readStream(InputStream inStream) throws Exception{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while( (len=inStream.read(buffer)) != -1){
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		inStream.close();
		return outStream.toByteArray();
	}

	private boolean fileExist(String fileName) {
		File dirPath = new File(IMAGE_PATH);
		if(!dirPath.exists()) {
			dirPath.mkdirs();
			return false;
		}

		File image = new File(IMAGE_PATH + fileName);
		return image.exists();
	}
}
