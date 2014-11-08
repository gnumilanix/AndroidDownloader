package com.milanix.example.downloader.pref;

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
	public static final String KEY_FILTERING_FIELD = "key_filter_field";
	public static final String KEY_IS_AUTOSTART = "key_autostart";
	public static final String KEY_IS_AGGREGATEDOWNLOAD = "key_aggregatedownload";
	public static final String KEY_SCHEDULE = "key_schedule";
	public static final String KEY_SCHEDULE_START = "key_schedule_start";
	public static final String KEY_SCHEDULE_UNTIL = "key_schedule_until";

	public static final String KEY_USERLEARNEDDRAWER = "navigation_drawer_learned";

	public static final String DEFAULT_DOWNLOAD_FOLDER = "/Download";
	public static final int DEFAULT_POOL_SIZE = Runtime.getRuntime()
			.availableProcessors();
	public static final int DEFAULT_WARNINGSIZE = 25;
	public static final ByteType DEFAULT_WARNINGTYPE = ByteType.MB;
	public static final String DEFAULT_ORDERING_FIELD = DownloadsDatabase.COLUMN_DATE_ADDED;
	public static final String DEFAULT_ORDERING_TYPE = QueryHelper.ORDERING_DESC;
	public static final String DEFAULT_SCHEDULE_START = "22:00";
	public static final String DEFAULT_SCHEDULE_UNTIL = "06:00";

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
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.putBoolean(KEY_USERLEARNEDDRAWER, hasLearned).apply();
	}

	/**
	 * This method will get if user has learned navigation drawer. This will
	 * read from Android default shared preferences.
	 * 
	 * @param is
	 *            the base application context
	 */
	public static boolean getHasLearnedDrawer(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(KEY_USERLEARNEDDRAWER, false);
	}

	/**
	 * This method will get if auto start is enabled/disable by user
	 * 
	 * @param is
	 *            the base application context
	 */
	public static boolean getIsAutoStart(Context context) {
		return getPreferenceInstance(context)
				.getBoolean(KEY_IS_AUTOSTART, true);
	}

	/**
	 * This method will set auto start if enabled/disable by user
	 * 
	 * @param is
	 *            the base application context
	 * @param isAutoStart
	 *            set true if service should auto start, otherwise false
	 */
	public static void setIsAutoStart(Context context, boolean isAutoStart) {
		getPreferenceInstance(context).edit()
				.putBoolean(KEY_IS_AUTOSTART, isAutoStart).apply();
	}

	/**
	 * This method will get if aggregate download notification is
	 * enabled/disable by user
	 * 
	 * @param is
	 *            the base application context
	 */
	public static boolean getIsAggregateDownload(Context context) {
		return getPreferenceInstance(context).getBoolean(
				KEY_IS_AGGREGATEDOWNLOAD, false);
	}

	/**
	 * This method will set aggregate download notification if enabled/disable
	 * by user
	 * 
	 * @param is
	 *            the base application context
	 * @param isAutoStart
	 *            set true if service should aggregate download notification,
	 *            otherwise false
	 */
	public static void setIsAggregateDownload(Context context,
			boolean isAutoStart) {
		getPreferenceInstance(context).edit()
				.putBoolean(KEY_IS_AGGREGATEDOWNLOAD, isAutoStart).apply();
	}

	/**
	 * This method will get if schedule is enabled
	 * 
	 * @param is
	 *            the base application context
	 */
	public static boolean getIsOnSchedule(Context context) {
		return getPreferenceInstance(context).getBoolean(KEY_SCHEDULE, false);
	}

	/**
	 * This method will set if schedule should be enabled
	 * 
	 * @param is
	 *            the base application context
	 * @param isAutoStart
	 *            set true if service should auto start, otherwise false
	 */
	public static void setIsOnSchedule(Context context, boolean isOnSchedule) {
		getPreferenceInstance(context).edit()
				.putBoolean(KEY_SCHEDULE, isOnSchedule).apply();
	}

	/**
	 * This method will set basic start-until schedule
	 * 
	 * @param is
	 *            the base application context
	 * @param start
	 *            start of the schedule
	 * @param until
	 *            until the schedule should run
	 */
	public static void setBasicSchedule(Context context, String start,
			String until) {
		getPreferenceInstance(context).edit()
				.putString(KEY_SCHEDULE_START, start)
				.putString(KEY_SCHEDULE_UNTIL, until).apply();
	}

	/**
	 * This method will get the schedule start time
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getBasicScheduleStart(Context context) {
		return getPreferenceInstance(context).getString(KEY_SCHEDULE_START,
				DEFAULT_SCHEDULE_START);
	}

	/**
	 * This method will get the schedule end time
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getBasicScheduleUntil(Context context) {
		return getPreferenceInstance(context).getString(KEY_SCHEDULE_UNTIL,
				DEFAULT_SCHEDULE_UNTIL);
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
		getPreferenceInstance(context).edit().putString(KEY_DOWNLOADPATH, url)
				.apply();
	}

	/**
	 * This method will get the download path
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getDownloadPath(Context context) {
		return getPreferenceInstance(context).getString(KEY_DOWNLOADPATH, "");
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
		getPreferenceInstance(context).edit()
				.putInt(KEY_DOWNLOADLIMIT_SIZE, size)
				.putString(KEY_DOWNLOADLIMIT_TYPE, byteType.toString()).apply();
	}

	/**
	 * This method will get the download limit size
	 * 
	 * @param is
	 *            the base application context
	 */
	public static int getDownloadLimitSize(Context context) {
		return getPreferenceInstance(context).getInt(KEY_DOWNLOADLIMIT_SIZE,
				DEFAULT_WARNINGSIZE);
	}

	/**
	 * This method will get the download limit type
	 * 
	 * @param is
	 *            the base application context
	 */
	public static ByteType getDownloadLimitType(Context context) {
		return ByteType.valueOf(getPreferenceInstance(context).getString(
				KEY_DOWNLOADLIMIT_TYPE, ByteType.MB.toString()));
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
		getPreferenceInstance(context).edit()
				.putString(KEY_DOWNLOADNETWORK, network.toString()).apply();
	}

	/**
	 * This method will get the download network
	 * 
	 * @param is
	 *            the base application context
	 */
	public static NetworkType getDownloadNetwork(Context context) {
		return NetworkType.valueOf(getPreferenceInstance(context).getString(
				KEY_DOWNLOADNETWORK, NetworkType.WIFI.toString()));
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
		getPreferenceInstance(context).edit()
				.putInt(KEY_DOWNLOADPOOLSIZE, poolSize).apply();
	}

	/**
	 * This method will get the download pool size
	 * 
	 * @param is
	 *            the base application context
	 */
	public static int getDownloadPoolSize(Context context) {
		return getPreferenceInstance(context).getInt(KEY_DOWNLOADPOOLSIZE,
				DEFAULT_POOL_SIZE);
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
		getPreferenceInstance(context).edit()
				.putString(KEY_ORDERING_FIELD, orderingField)
				.putString(KEY_ORDERING_TYPE, orderingType).apply();
	}

	/**
	 * This method will get the ordering field
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getSortOrderingField(Context context) {
		return getPreferenceInstance(context).getString(KEY_ORDERING_FIELD,
				DEFAULT_ORDERING_FIELD);
	}

	/**
	 * This method will get the ordering type
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getSortOrderingType(Context context) {
		return getPreferenceInstance(context).getString(KEY_ORDERING_TYPE,
				DEFAULT_ORDERING_TYPE);
	}

	/**
	 * This method will set the filter type
	 * 
	 * @param is
	 *            the base application context
	 * @param filterBy
	 *            is the filter type
	 */
	public static void setFilterType(Context context, String filterBy) {
		getPreferenceInstance(context).edit()
				.putString(KEY_FILTERING_FIELD, filterBy).apply();
	}

	/**
	 * This method will get the filter type. This method will return empty to
	 * define all
	 * 
	 * @param is
	 *            the base application context
	 */
	public static String getFilterType(Context context) {
		return getPreferenceInstance(context)
				.getString(KEY_FILTERING_FIELD, "");
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
