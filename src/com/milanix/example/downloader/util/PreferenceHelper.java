package com.milanix.example.downloader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This class contains helper method to work with shared preferences
 * 
 * @author Milan
 * 
 */
public class PreferenceHelper {
	public static final String KEY_PREFERENCES = "com.milanix.example.downloader.preferences";

	public static final String KEY_DOWNLOADPATH = "key_downloadpath";
	public static final String KEY_DOWNLOADPOOLSIZE = "key_downloadpoolsize";

	public static final String KEY_USERLEARNEDDRAWER = "navigation_drawer_learned";

	public static final String PATH_DOWNLOAD = "/Download";

	public static final int DEFAULT_POOLSIZE = 5;

	/**
	 * This method will set if user has learned navigation drawer. This will
	 * read from Android default shared preferences.
	 * 
	 * @param is
	 *            the base application context
	 * @param hasLearned
	 *            true if user has learned about navigation drawer otherwise
	 *            false
	 */
	public static void setHasLearnedDrawer(Context context, boolean hasLearned) {
		SharedPreferences defaultSharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);

		SharedPreferences.Editor editor = defaultSharedPref.edit();
		editor.putBoolean(KEY_USERLEARNEDDRAWER, hasLearned);
		editor.apply();
	}

	/**
	 * This method will get if user has learned navigation drawer. This will
	 * read from Android default shared preferences.
	 * 
	 * @param is
	 *            the base application context
	 */
	public static boolean getHasLearnedDrawer(Context context) {
		SharedPreferences defaultSharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);

		return defaultSharedPref.getBoolean(KEY_USERLEARNEDDRAWER, false);
	}

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
		editor.apply();
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

	/**
	 * This method will set the download pool size
	 * 
	 * @param is
	 *            the base application context
	 * @param poolSize
	 *            is the max download pool size
	 */
	public static void setDownloadPoolSize(Context context, int poolSize) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(KEY_DOWNLOADPOOLSIZE, poolSize);
		editor.apply();
	}

	/**
	 * This method will get the download pool size
	 * 
	 * @param is
	 *            the base application context
	 */
	public static int getDownloadPoolSize(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		return sharedPref.getInt(KEY_DOWNLOADPOOLSIZE, DEFAULT_POOLSIZE);

	}

	/**
	 * This method will get shared preference instance for this application
	 * 
	 * @param context
	 *            the base application context
	 * @return shared preference instance
	 */
	public static SharedPreferences getPreferenceInstance(Context context) {
		return context.getSharedPreferences(KEY_PREFERENCES,
				Context.MODE_PRIVATE);
	}
}
