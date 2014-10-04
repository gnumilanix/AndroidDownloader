package com.milanix.example.downloader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog.NetworkType;
import com.milanix.example.downloader.util.FileUtils.ByteType;

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
	public static final String KEY_DOWNLOADNETWORK = "key_network";
	public static final String KEY_DOWNLOADLIMIT_SIZE = "key_downloadwarning_size";
	public static final String KEY_DOWNLOADLIMIT_TYPE = "key_downloadwarning_type";
	public static final String KEY_ORDERING_FIELD = "key_ordering_field";
	public static final String KEY_ORDERING_TYPE = "key_ordering_type";

	public static final String KEY_USERLEARNEDDRAWER = "navigation_drawer_learned";

	public static final String PATH_DOWNLOAD = "/Download";

	public static final int DEFAULT_POOLSIZE = Runtime.getRuntime()
			.availableProcessors();
	public static final int DEFAULT_WARNINGSIZE = 25;
	public static final ByteType DEFAULT_WARNINGTYPE = ByteType.MB;

	public static final String DEFAULT_ORDERING_FIELD = DownloadsDatabase.COLUMN_DATE_ADDED;
	public static final String DEFAULT_ORDERING_TYPE = QueryHelper.ORDERING_DESC;

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
	 * This method will set the download warning size and type
	 * 
	 * @param is
	 *            the base application context
	 * @param storageType
	 *            is the warning size
	 * @param byteType
	 *            is a byte type
	 */
	public static void setDownloadLimit(Context context, int size,
			ByteType byteType) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(KEY_DOWNLOADLIMIT_SIZE, size);
		editor.putString(KEY_DOWNLOADLIMIT_TYPE, byteType.toString());
		editor.apply();
	}

	/**
	 * This method will get the download limit size
	 * 
	 * @param is
	 *            the base application context
	 */
	public static int getDownloadLimitSize(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		return sharedPref.getInt(KEY_DOWNLOADLIMIT_SIZE, DEFAULT_WARNINGSIZE);
	}

	/**
	 * This method will get the download limit type
	 * 
	 * @param is
	 *            the base application context
	 */
	public static ByteType getDownloadLimitType(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		return ByteType.valueOf(sharedPref.getString(KEY_DOWNLOADLIMIT_TYPE,
				ByteType.MB.toString()));
	}

	/**
	 * This method will set the download network
	 * 
	 * @param is
	 *            the base application context
	 * @param network
	 *            is the NetworkType
	 */
	public static void setDownloadNetwork(Context context, NetworkType network) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(KEY_DOWNLOADNETWORK, network.toString());
		editor.apply();
	}

	/**
	 * This method will get the download network
	 * 
	 * @param is
	 *            the base application context
	 */
	public static NetworkType getDownloadNetwork(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		return NetworkType.valueOf(sharedPref.getString(KEY_DOWNLOADNETWORK,
				NetworkType.WIFI.toString()));
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
	 * This method will set the ordering type
	 * 
	 * @param is
	 *            the base application context
	 * @param orderingField
	 *            is the ordering field
	 * @param orderingType
	 *            ordering method
	 */
	public static void setSortOrdering(Context context, String orderingField,
			String orderingType) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(KEY_ORDERING_FIELD, orderingField);
		editor.putString(KEY_ORDERING_TYPE, orderingType);
		editor.apply();
	}

	/**
	 * This method will get the ordering field
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getSortOrderingField(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		return sharedPref.getString(KEY_ORDERING_FIELD, DEFAULT_ORDERING_FIELD);
	}

	/**
	 * This method will get the ordering type
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getSortOrderingType(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				KEY_PREFERENCES, Context.MODE_PRIVATE);

		return sharedPref.getString(KEY_ORDERING_TYPE, DEFAULT_ORDERING_TYPE);
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
