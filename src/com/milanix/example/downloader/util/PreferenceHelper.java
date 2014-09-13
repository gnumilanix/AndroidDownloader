package com.milanix.example.downloader.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class contains helper method to work with shared preferences
 * 
 * @author Milan
 * 
 */
public class PreferenceHelper {
	public static final String KEY_PREFERENCES = "com.milanix.example.downloader.preferences";

	public static final String KEY_DOWNLOADPATH = "key_downloadpath";

	public static final String PATH_DOWNLOAD = "/Download";

	/**
	 * This method will set the download path
	 * 
	 * @param is
	 *            the base application context
	 * @param url
	 *            is the url path
	 */
	public static void setDownloadPath(Context context, String url) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(KEY_DOWNLOADPATH, url);
		editor.commit();
	}

	/**
	 * This method will get the download path
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getDownloadPath(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		return sharedPref.getString(KEY_DOWNLOADPATH, "");

	}
}
