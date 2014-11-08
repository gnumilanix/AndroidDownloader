package com.milanix.example.downloader.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.milanix.example.downloader.Downloader;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.activity.HomeActivity;
import com.milanix.example.downloader.data.dao.Credential;
import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadListener;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.dao.Download.FailedReason;
import com.milanix.example.downloader.data.database.CredentialsDatabase;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.CredentialContentProvider;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog.NetworkType;
import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.util.FileUtils;
import com.milanix.example.downloader.util.FileUtils.ByteType;
import com.milanix.example.downloader.util.IOUtils;
import com.milanix.example.downloader.util.NetworkUtils;
import com.milanix.example.downloader.util.TextHelper;

/**
 * This is a download service
 * 
 * @author Milan
 * 
 */
public class DownloadService extends Service {
	// Schedule format
	public static final String SCHEDULE_DATE_FORMAT = "HH:mm";

	// Max progress for the notification
	private static final int NOTIFICATION_MAX_PROGRESS = 100;
	private static final int NOTIFICATION_PROGRESS_TIMER = 2 * 1000;

	// Notification ids
	private static final int NOTIFICATION_ID_WARNING = 1000;
	private static final int NOTIFICATION_ID_COMPLETED = 1001;
	private static final int NOTIFICATION_ID_FAILED = 1002;

	// Notification request codes
	private static final int NOTIFICATION_REQCODE_CONTINUE = 1000;
	private static final int NOTIFICATION_REQCODE_RESUME = 1001;
	private static final int NOTIFICATION_REQCODE_PAUSE = 1002;
	private static final int NOTIFICATION_REQCODE_CLEAR = 1003;

	// Alarm request codes
	private static final int ALARM_REQCODE_SCHEDULE_START = 2000;
	private static final int ALARM_REQCODE_SCHEDULE_UNTIL = 2001;

	// Handler's whatss
	private static final int HANDLE_NETWORK_CONNECTED = 0;
	private static final int HANDLE_NETWORK_DISCONNECTED = 1;

	private static final int HANDLE_CONTINUE_DOWNLOAD = 100;
	private static final int HANDLE_PAUSE_DOWNLOAD = 101;
	private static final int HANDLE_RESUME_DOWNLOAD = 102;

	// Notification tags
	private static final String NOTIFICATION_TAG_GENERAL = "notification_tag_general";
	private static final String NOTIFICATION_TAG_WARNING = "notification_tag_warning";
	private static final String NOTIFICATION_TAG_PROGRESS = "notification_tag_progress";
	private static final String NOTIFICATION_TAG_COMPLETED = "notification_tag_completed";
	private static final String NOTIFICATION_TAG_FAILED = "notification_tag_failed";
	private static final String NOTIFICATION_TAG_CLEAR = "notification_tag_clear";

	// Notification actions

	private static final String ACTION_CONTINUE = "action_continue";
	private static final String ACTION_RESUME = "action_resume";
	private static final String ACTION_PAUSE = "action_pause";
	private static final String ACTION_COMPLETE = "action_complete";
	private static final String ACTION_OPEN = "action_open";

	private static final String ACTION_SCHEDULE_START = "action_schedule_start";
	private static final String ACTION_SCHEDULE_UNTIL = "action_schedule_until";

	// Notification keys
	private static final String KEY_DOWNLOADID = "key_downloadid";
	private static final String KEY_DOWNLOAD_ACTIVEIDS = "key_download_activeids";

	private static final String KEY_NOTIFICATION_TAG = "key_notification_tag";
	private static final String KEY_NOTIFICATION_ID = "key_notification_id";

	public static final String KEY_OPEN_DOWNLOADED = "key_open_downloaded";

	public static final String KEY_FILE_PATH = "key_file_path";

	// Value constants
	public static final int INTERVAL_DAY = 24 * 60 * 60 * 1000;

	private ServiceActionReceiver serviceActionReceiver;
	private static NotificationManager notificationManager;
	private static AlarmManager alarmManager;

	// Shared preference instance
	private static SharedPreferences sharedPreferenced;
	private static OnSharedPreferenceChangeListener sharedPrefChangeListener;

	// Download user configured params
	private static Integer downloadPoolSize;
	private static NetworkType downloadNetworkType;
	private static Integer downloadLimitSize;
	private static ByteType downloadLimitType;

	// Ids for general notifications
	private static TreeSet<Integer> generalNotificationIds = new TreeSet<Integer>();

	// Completed and failed download name for notifications
	private static HashSet<Download> completedDownloads = new HashSet<Download>();

	// Map to keep track of async task
	private static HashMap<Integer, DownloadTask> downloadTasks = new HashMap<Integer, DownloadTask>();

	// Map to support multiple callback with same ids but different caller
	private static HashMap<Integer, HashSet<DownloadListener>> attachedCallbacks = new HashMap<Integer, HashSet<DownloadListener>>();

	// Executor for parallel downloads
	private static ThreadPoolExecutor executor;

	// Executor constants
	private static final int POOL_MAX_MULTIPLIER = 5;
	private static final int POOL_KEEP_ALIVE = 1;
	private static final BlockingQueue<Runnable> POOL_WORKQUEUE = new LinkedBlockingQueue<Runnable>(
			10);
	private static final ThreadFactory POOL_THREAD_FACTORY = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			StringBuilder threadNameBuilder = new StringBuilder(
					"Download AsyncTask #");
			threadNameBuilder.append(mCount.getAndIncrement());

			return new Thread(r, threadNameBuilder.toString());
		}
	};

	private static Handler serviceActionHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (null != msg && null != msg.getData()) {
				switch (msg.what) {
				case HANDLE_CONTINUE_DOWNLOAD:
					if (msg.getData().containsKey(KEY_DOWNLOADID))
						authorizeAndBeginDownload(new long[] { msg.getData()
								.getInt(KEY_DOWNLOADID) });

					break;
				case HANDLE_RESUME_DOWNLOAD:
					if (msg.getData().containsKey(KEY_DOWNLOADID))
						resumeDownload(new long[] { msg.getData().getInt(
								KEY_DOWNLOADID) });

					break;
				case HANDLE_PAUSE_DOWNLOAD:
					if (msg.getData().containsKey(KEY_DOWNLOADID))
						pauseDownload(new long[] { msg.getData().getInt(
								KEY_DOWNLOADID) });

					break;
				case HANDLE_NETWORK_CONNECTED:
					if (msg.getData().containsKey(KEY_DOWNLOAD_ACTIVEIDS))
						resumeDownload(msg.getData().getLongArray(
								KEY_DOWNLOAD_ACTIVEIDS));

					break;
				case HANDLE_NETWORK_DISCONNECTED:
					if (msg.getData().containsKey(KEY_DOWNLOAD_ACTIVEIDS))
						pauseDownload(msg.getData().getLongArray(
								KEY_DOWNLOAD_ACTIVEIDS));

					break;
				default:
					super.handleMessage(msg);
				}
			}
		}
	};

	private final IBinder mBinder = new DownloadBinder();

	public class DownloadBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		init();
		autoAddToQueue();

		// Don't move to init as init might be called after the service has
		// started
		registerServiceActionReceiver();
		registerConfigurationListener();

		return START_STICKY;
	}

	/**
	 * This method will init params that is required for this service to work
	 * optimally
	 */
	private void init() {
		if (null == serviceActionReceiver)
			serviceActionReceiver = new ServiceActionReceiver();

		if (null == notificationManager)
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		if (null == alarmManager)
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		if (null == sharedPreferenced)
			sharedPreferenced = PreferenceHelper
					.getPreferenceInstance(getApplicationContext());

		if (null == downloadPoolSize)
			downloadPoolSize = PreferenceHelper
					.getDownloadPoolSize(getApplicationContext());

		if (null == downloadNetworkType)
			downloadNetworkType = PreferenceHelper
					.getDownloadNetwork(getApplicationContext());

		if (null == downloadLimitSize)
			downloadLimitSize = PreferenceHelper
					.getDownloadLimitSize(getApplicationContext());

		if (null == downloadLimitType)
			downloadLimitType = PreferenceHelper
					.getDownloadLimitType(getApplicationContext());

		if (null == executor)
			executor = new ThreadPoolExecutor(getCorePoolSizeFromPref(),
					getMaxPoolSizeFromPref(), POOL_KEEP_ALIVE,
					TimeUnit.SECONDS, POOL_WORKQUEUE, POOL_THREAD_FACTORY);

		scheduleAlarm();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		clean();

		super.onDestroy();
	}

	/**
	 * This method will cleanup params that were initialized
	 */
	private void clean() {
		unregisterServiceActionReceiver();
		unregisterPoolConfigureListener();

		if (null != notificationManager) {
			notificationManager.cancel(NOTIFICATION_TAG_WARNING,
					NOTIFICATION_ID_WARNING);
			notificationManager.cancel(NOTIFICATION_TAG_COMPLETED,
					NOTIFICATION_ID_COMPLETED);
			notificationManager.cancel(NOTIFICATION_TAG_FAILED,
					NOTIFICATION_ID_FAILED);

			for (Integer generalNotificationId : generalNotificationIds) {
				notificationManager.cancel(NOTIFICATION_TAG_GENERAL,
						generalNotificationId);
			}
		}

		if (null != downloadTasks) {
			Iterator<Entry<Integer, DownloadTask>> currentTaskItreator = downloadTasks
					.entrySet().iterator();

			while (currentTaskItreator.hasNext()) {
				Map.Entry<Integer, DownloadTask> taskPair = currentTaskItreator
						.next();

				DownloadTask task = taskPair.getValue();

				if (null != task)
					task.cancel(true);
			}
		}
	}

	/**
	 * Adds downloads to the queue automatically
	 * 
	 */
	private void autoAddToQueue() {
		final Cursor cursor = Downloader
				.getDownloaderContext()
				.getContentResolver()
				.query(DownloadContentProvider.CONTENT_URI_DOWNLOADS,
						null,
						QueryHelper.getWhere(DownloadsDatabase.COLUMN_STATE,
								DownloadState.COMPLETED.toString(), false),
						null,
						QueryHelper.getOrdering(
								DownloadsDatabase.COLUMN_DATE_ADDED,
								QueryHelper.ORDERING_DESC));

		if (null != cursor) {
			HashSet<Integer> downloadIds = new HashSet<Integer>();

			while (cursor.moveToNext()) {
				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_ID))
					downloadIds.add(cursor.getInt(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_ID)));
			}

			if (!downloadIds.isEmpty()) {
				for (Integer downloadId : downloadIds)
					DownloadService.downloadFile(downloadId);
			}
		}
	}

	/**
	 * This method will retrieve logtag
	 * 
	 * @return
	 */
	private static String getLogTag() {
		return DownloadService.class.getName();
	}

	/**
	 * This method registers ServiceActionReceiver
	 */
	private void registerServiceActionReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(ACTION_CONTINUE);
		filter.addAction(ACTION_RESUME);
		filter.addAction(ACTION_PAUSE);

		getApplicationContext().registerReceiver(serviceActionReceiver, filter);
	}

	/**
	 * This method unregisters ServiceActionReceiver
	 */
	private void unregisterServiceActionReceiver() {
		if (null != serviceActionReceiver)
			getApplicationContext().unregisterReceiver(serviceActionReceiver);
	}

	/**
	 * This method will register shared preference change listener for pool
	 * change event
	 */
	private void registerConfigurationListener() {
		if (null == sharedPrefChangeListener)
			sharedPrefChangeListener = new OnSharedPreferenceChangeListener() {

				@Override
				public void onSharedPreferenceChanged(
						SharedPreferences sharedPreference, String key) {
					if (PreferenceHelper.KEY_DOWNLOADPOOLSIZE.equals(key)) {
						downloadPoolSize = PreferenceHelper
								.getDownloadPoolSize(getApplicationContext());

						resetPoolSize();
					} else if (PreferenceHelper.KEY_DOWNLOADNETWORK.equals(key)) {
						downloadNetworkType = PreferenceHelper
								.getDownloadNetwork(getApplicationContext());
					} else if (PreferenceHelper.KEY_DOWNLOADLIMIT_SIZE
							.equals(key)) {
						downloadLimitSize = PreferenceHelper
								.getDownloadLimitSize(getApplicationContext());
					} else if (PreferenceHelper.KEY_DOWNLOADLIMIT_TYPE
							.equals(key)) {
						downloadLimitType = PreferenceHelper
								.getDownloadLimitType(getApplicationContext());
					}
				}
			};

		sharedPreferenced
				.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener);
	}

	/**
	 * This method will unregister pool configuration listener
	 */
	private void unregisterPoolConfigureListener() {
		if (null == sharedPreferenced && null != sharedPrefChangeListener)
			sharedPreferenced
					.unregisterOnSharedPreferenceChangeListener(sharedPrefChangeListener);
	}

	/**
	 * This method will schedule alarm. Internally it will apply two alarms, one
	 * to start an schedule and one until the schedule should run.
	 */
	private void scheduleAlarm() {
		if (PreferenceHelper.getIsOnSchedule(getApplicationContext())) {
			Date scheduleStartDate = TextHelper.getAsDate(
					getApplicationContext(), SCHEDULE_DATE_FORMAT,
					PreferenceHelper
							.getBasicScheduleStart(getApplicationContext()));

			Date scheduleUntilDate = TextHelper.getAsDate(
					getApplicationContext(), SCHEDULE_DATE_FORMAT,
					PreferenceHelper
							.getBasicScheduleUntil(getApplicationContext()));

			if (null != scheduleStartDate && null != scheduleUntilDate) {
				Intent scheduleStartIntent = new Intent(
						getApplicationContext(), ServiceActionReceiver.class)
						.setAction(ACTION_SCHEDULE_START);
				Intent scheduleUntilIntent = new Intent(
						getApplicationContext(), ServiceActionReceiver.class)
						.setAction(ACTION_SCHEDULE_UNTIL);

				PendingIntent scheduleStartPendingIntent = PendingIntent
						.getBroadcast(getApplicationContext(),
								ALARM_REQCODE_SCHEDULE_START,
								scheduleStartIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);
				PendingIntent scheduleUntilPendingIntent = PendingIntent
						.getBroadcast(getApplicationContext(),
								ALARM_REQCODE_SCHEDULE_UNTIL,
								scheduleUntilIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);

				long triggerStartMillis = scheduleStartDate.getTime();
				long triggerUntilMillis = scheduleUntilDate.getTime();

				if (scheduleStartDate.getTime() < Calendar.getInstance()
						.getTimeInMillis()
						|| scheduleUntilDate.getTime() < Calendar.getInstance()
								.getTimeInMillis()) {
					triggerStartMillis = scheduleStartDate.getTime()
							+ INTERVAL_DAY;
					triggerUntilMillis = scheduleUntilDate.getTime()
							+ INTERVAL_DAY;
				}

				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
						triggerStartMillis, AlarmManager.INTERVAL_DAY,
						scheduleStartPendingIntent);
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
						triggerUntilMillis, AlarmManager.INTERVAL_DAY,
						scheduleUntilPendingIntent);
			}
		}
	}

	/**
	 * This method will show general notification message
	 * 
	 * @param icon
	 *            is an icon to be shown in the notification
	 * @param title
	 *            is a title to be shown in the notification
	 * @param message
	 *            is a message text to be shown in the notification
	 * 
	 * @return bundle attached to this notification. Contains notification tag
	 *         and id. Callers can use it to cancel notification using
	 *         cancelNotification();
	 */
	public static Bundle showGeneralNotification(int icon, String title,
			String message) {

		Intent notificationIntent = new Intent(
				Downloader.getDownloaderContext(), ServiceActionReceiver.class)
				.putExtra(KEY_NOTIFICATION_TAG, NOTIFICATION_TAG_CLEAR)
				.putExtra(KEY_NOTIFICATION_ID,
						generalNotificationIds.last() + 1);

		PendingIntent notificationPendingIntent = PendingIntent.getActivity(
				Downloader.getDownloaderContext(), 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder serviceStartBuilder = new NotificationCompat.Builder(
				Downloader.getDownloaderContext())
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Downloader service")
				.setContentText("Downloader service has started")
				.setContentIntent(notificationPendingIntent);

		notify(NOTIFICATION_TAG_GENERAL, generalNotificationIds.last() + 1,
				serviceStartBuilder.build());

		return notificationIntent.getExtras();
	}

	/**
	 * This method will get progress notification for given download
	 * 
	 * @param download
	 *            to be attached to this notification
	 * 
	 * @return progress notification builder
	 */
	private static Builder getProgressNotification(Download download) {
		if (download.isValid() && null != Downloader.getDownloaderContext()) {

			Intent resultIntent = new Intent(Downloader.getDownloaderContext(),
					HomeActivity.class);

			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					Downloader.getDownloaderContext(), 0, resultIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			Intent resumeIntent = new Intent(Downloader.getDownloaderContext(),
					ServiceActionReceiver.class).setAction(ACTION_RESUME)
					.putExtra(KEY_DOWNLOADID, download.getId())
					.putExtra(KEY_NOTIFICATION_TAG, NOTIFICATION_TAG_PROGRESS)
					.putExtra(KEY_NOTIFICATION_ID, download.getId());

			Intent pauseIntent = new Intent(Downloader.getDownloaderContext(),
					ServiceActionReceiver.class).setAction(ACTION_PAUSE)
					.putExtra(KEY_DOWNLOADID, download.getId())
					.putExtra(KEY_NOTIFICATION_TAG, NOTIFICATION_TAG_PROGRESS)
					.putExtra(KEY_NOTIFICATION_ID, download.getId());

			PendingIntent pendingIntentResume = PendingIntent.getBroadcast(
					Downloader.getDownloaderContext(),
					NOTIFICATION_REQCODE_RESUME, resumeIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			PendingIntent pendingIntentPause = PendingIntent.getBroadcast(
					Downloader.getDownloaderContext(),
					NOTIFICATION_REQCODE_PAUSE, pauseIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder progressBuilder = new NotificationCompat.Builder(
					Downloader.getDownloaderContext());
			progressBuilder
					.setContentTitle(download.getName())
					.setContentText(download.getUrl())
					.setSmallIcon(R.drawable.ic_icon_download_dark)
					.setOngoing(true)
					.setContentIntent(resultPendingIntent)
					.setStyle(
							new NotificationCompat.BigTextStyle()
									.bigText(download.getUrl()))
					.addAction(
							R.drawable.ic_action_resume_dark,
							Downloader.getDownloaderContext().getString(
									R.string.btn_resume), pendingIntentResume)
					.addAction(
							R.drawable.ic_action_pause_dark,
							Downloader.getDownloaderContext().getString(
									R.string.btn_pause), pendingIntentPause);

			return progressBuilder;
		}

		return null;

	}

	/**
	 * This method will show warning notification for given download
	 * 
	 * @param download
	 *            to be attached to this notification
	 */
	private static void showWarningNotification(Download download) {
		if (download.isValid() && null != Downloader.getDownloaderContext()) {
			Intent resultIntent = new Intent(Downloader.getDownloaderContext(),
					HomeActivity.class);

			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					Downloader.getDownloaderContext(), 0, resultIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			Intent continueIntent = new Intent(
					Downloader.getDownloaderContext(),
					ServiceActionReceiver.class).setAction(ACTION_CONTINUE)
					.putExtra(KEY_DOWNLOADID, download.getId())
					.putExtra(KEY_NOTIFICATION_TAG, NOTIFICATION_TAG_WARNING)
					.putExtra(KEY_NOTIFICATION_ID, NOTIFICATION_ID_WARNING);

			PendingIntent pendingIntentContinue = PendingIntent.getBroadcast(
					Downloader.getDownloaderContext(),
					NOTIFICATION_REQCODE_CONTINUE, continueIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			String message = "";
			String title = "";

			// Message context based on if size is determined or not
			if (null == download.getSize()) {
				title = Downloader.getDownloaderContext().getString(
						R.string.download_size_unknown_title);

				message = String.format(Downloader.getDownloaderContext()
						.getString(R.string.download_size_unknown_message),
						download.getName());
			} else {
				title = Downloader.getDownloaderContext().getString(
						R.string.download_size_exceeded_title);

				message = String.format(Downloader.getDownloaderContext()
						.getString(R.string.download_size_exceeded_message),
						download.getName(), PreferenceHelper
								.getDownloadLimitSize(Downloader
										.getDownloaderContext()),
						PreferenceHelper.getDownloadLimitType(Downloader
								.getDownloaderContext()));
			}

			NotificationCompat.Builder warningBuilder = new NotificationCompat.Builder(
					Downloader.getDownloaderContext())
					.setSmallIcon(R.drawable.ic_icon_limit_dark)
					.setContentTitle(title)
					.setContentText(message)
					.setContentIntent(resultPendingIntent)
					.setDefaults(Notification.DEFAULT_ALL)
					.setOngoing(true)
					.setStyle(
							new NotificationCompat.BigTextStyle()
									.bigText(message))
					.addAction(
							R.drawable.ic_action_resume_dark,
							Downloader.getDownloaderContext().getString(
									R.string.btn_continue),
							pendingIntentContinue);

			notify(NOTIFICATION_TAG_WARNING, NOTIFICATION_ID_WARNING,
					warningBuilder.build());
		}
	}

	/**
	 * This method will show download completed notification for given download.
	 * Depending on if it failed or succeed it will add to different
	 * notification
	 * 
	 * @param download
	 *            to be attached to this notification
	 */
	private static void showCompletedNotification(Download download) {
		if (download.isValid() && null != Downloader.getDownloaderContext()) {

			String title = null;
			String message = null;

			String notificationTag = null;
			Integer notificationId = null;

			int completedCount = 0;
			int failedCount = 0;

			if (PreferenceHelper.getIsAggregateDownload(Downloader
					.getDownloaderContext())) {

				completedCount++;
				failedCount++;

				notificationId = download.getId();

				if (DownloadState.COMPLETED.equals(download.getState())) {
					title = Downloader.getDownloaderContext().getString(
							R.string.download_completed_title);
					message = Downloader
							.getDownloaderContext()
							.getResources()
							.getQuantityString(
									R.plurals.download_completed_subtitle,
									completedCount, download.getName());

					notificationTag = NOTIFICATION_TAG_COMPLETED;
				} else if (DownloadState.FAILED.equals(download.getState())) {
					title = Downloader.getDownloaderContext().getString(
							R.string.download_failed_title);
					message = Downloader
							.getDownloaderContext()
							.getResources()
							.getQuantityString(
									R.plurals.download_failed_subtitle,
									failedCount, download.getName());

					notificationTag = NOTIFICATION_TAG_FAILED;
				}

				NotificationCompat.Builder warningBuilder = new NotificationCompat.Builder(
						Downloader.getDownloaderContext())
						.setSmallIcon(R.drawable.ic_icon_download_dark)
						.setContentTitle(title)
						.setContentText(message)
						.setDefaults(Notification.DEFAULT_ALL)
						.setOngoing(false)
						.setStyle(
								new NotificationCompat.BigTextStyle()
										.bigText(message));

				if (DownloadState.COMPLETED.equals(download.getState())) {
					Intent clearIntent = new Intent(
							Downloader.getDownloaderContext(),
							ServiceActionReceiver.class)
							.setAction(ACTION_OPEN)
							.putExtra(KEY_NOTIFICATION_TAG, notificationTag)
							.putExtra(KEY_NOTIFICATION_ID, notificationId)
							.putExtra(DownloadService.KEY_FILE_PATH,
									download.getPath());

					PendingIntent pendingIntentClear = PendingIntent
							.getBroadcast(Downloader.getDownloaderContext(),
									NOTIFICATION_REQCODE_CLEAR, clearIntent,
									PendingIntent.FLAG_CANCEL_CURRENT);

					warningBuilder.setContentIntent(pendingIntentClear);
				} else if (DownloadState.FAILED.equals(download.getState())) {
					completedDownloads.add(download);

					Intent clearIntent = new Intent(
							Downloader.getDownloaderContext(),
							ServiceActionReceiver.class)
							.setAction(ACTION_COMPLETE)
							.putExtra(KEY_OPEN_DOWNLOADED, true)
							.putExtra(KEY_NOTIFICATION_TAG, notificationTag)
							.putExtra(KEY_NOTIFICATION_ID, notificationId);

					PendingIntent pendingIntentClear = PendingIntent
							.getBroadcast(Downloader.getDownloaderContext(),
									NOTIFICATION_REQCODE_CLEAR, clearIntent,
									PendingIntent.FLAG_CANCEL_CURRENT);

					warningBuilder.setContentIntent(pendingIntentClear);
				}

				notify(notificationTag, notificationId, warningBuilder.build());
			} else {

				NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
				inboxStyle.setBigContentTitle(message);

				for (Download completedDownload : completedDownloads) {
					if (DownloadState.COMPLETED.equals(download.getState())) {
						inboxStyle.addLine(String.format(
								Downloader.getDownloaderContext().getString(
										R.string.download_completed_message),
								completedDownload.getName(),
								completedDownload.getUrl()));

						completedCount++;
					} else if (DownloadState.FAILED.equals(download.getState())) {
						inboxStyle.addLine(String.format(
								Downloader.getDownloaderContext().getString(
										R.string.download_failed_message),
								completedDownload.getName(),
								completedDownload.getUrl()));

						failedCount++;
					}
				}

				if (DownloadState.COMPLETED.equals(download.getState())) {
					title = Downloader.getDownloaderContext().getString(
							R.string.download_completed_title);

					if (completedCount == 1)
						message = Downloader
								.getDownloaderContext()
								.getResources()
								.getQuantityString(
										R.plurals.download_completed_subtitle,
										completedCount, download.getName());
					else
						message = Downloader
								.getDownloaderContext()
								.getResources()
								.getQuantityString(
										R.plurals.download_completed_subtitle,
										completedCount, completedCount);

					notificationTag = NOTIFICATION_TAG_COMPLETED;
					notificationId = NOTIFICATION_ID_COMPLETED;
				} else if (DownloadState.FAILED.equals(download.getState())) {
					title = Downloader.getDownloaderContext().getString(
							R.string.download_failed_title);

					if (failedCount == 1)
						message = Downloader
								.getDownloaderContext()
								.getResources()
								.getQuantityString(
										R.plurals.download_failed_subtitle,
										failedCount, download.getName());
					else
						message = Downloader
								.getDownloaderContext()
								.getResources()
								.getQuantityString(
										R.plurals.download_failed_subtitle,
										failedCount, failedCount);

					notificationTag = NOTIFICATION_TAG_FAILED;
					notificationId = NOTIFICATION_ID_FAILED;
				}

				Intent clearIntent = new Intent(
						Downloader.getDownloaderContext(),
						ServiceActionReceiver.class).setAction(ACTION_COMPLETE)
						.putExtra(KEY_OPEN_DOWNLOADED, true)
						.putExtra(KEY_NOTIFICATION_TAG, notificationTag)
						.putExtra(KEY_NOTIFICATION_ID, notificationId);

				PendingIntent pendingIntentClear = PendingIntent.getBroadcast(
						Downloader.getDownloaderContext(),
						NOTIFICATION_REQCODE_CLEAR, clearIntent,
						PendingIntent.FLAG_CANCEL_CURRENT);

				NotificationCompat.Builder completedBuilder = new NotificationCompat.Builder(
						Downloader.getDownloaderContext())
						.setSmallIcon(R.drawable.ic_icon_download_dark)
						.setContentTitle(title).setContentText(message)
						.setContentIntent(pendingIntentClear)
						.setDefaults(Notification.DEFAULT_ALL)
						.setStyle(inboxStyle);

				notify(notificationTag, notificationId,
						completedBuilder.build());
			}
		}
	}

	/**
	 * Displays notification
	 * 
	 * @param tag
	 *            A string identifier for this notification. May be null.
	 * @param id
	 *            An identifier for this notification. The pair (tag, id) must
	 *            be unique within your application.
	 * @param notification
	 *            A Notification object describing what to show the user. Must
	 *            not be null.
	 */
	private static void notify(String tag, int id, Notification notification) {
		if (null != notificationManager && null != notification)
			notificationManager.notify(tag, id, notification);
	}

	/**
	 * This method will cancel notification with attached bundle. Caller must
	 * include KEY_NOTIFICATION_TAG and KEY_NOTIFICATION_ID in the bundle
	 * 
	 * @param bundle
	 *            to retrieve id and tag from
	 */
	public static void cancelNotification(Bundle bundle) {
		if (null != notificationManager && null != bundle) {

			if (bundle.containsKey(KEY_NOTIFICATION_ID)
					&& bundle.containsKey(KEY_NOTIFICATION_TAG))
				cancelNotification(bundle.getString(KEY_NOTIFICATION_TAG),
						bundle.getInt(KEY_NOTIFICATION_ID));

			if (null != Downloader.getDownloaderContext()
					&& bundle.containsKey(KEY_OPEN_DOWNLOADED)) {
				if (null != completedDownloads)
					completedDownloads.clear();

				final Intent intent = new Intent(
						Downloader.getDownloaderContext(), HomeActivity.class);
				intent.putExtra(KEY_OPEN_DOWNLOADED,
						bundle.containsKey(KEY_OPEN_DOWNLOADED));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				Downloader.getDownloaderContext().startActivity(intent);
			}
		}

	}

	/**
	 * This method will cancel notification with given id and tag
	 * 
	 * @param notificationTag
	 *            is the notifications tag
	 * 
	 * @param notificationId
	 *            is the notification id
	 */
	public static void cancelNotification(String notificationTag,
			Integer notificationId) {
		if (null != notificationId && null != notificationTag)
			notificationManager.cancel(notificationTag, notificationId);
	}

	/**
	 * This method will start activity with action_view intent
	 */
	public static void viewFile(Bundle bundle) {
		if (null != Downloader.getDownloaderContext() && null != bundle
				&& bundle.containsKey(KEY_FILE_PATH)) {
			final File file = new File(bundle.getString(KEY_FILE_PATH));

			final String ext = MimeTypeMap.getFileExtensionFromUrl(file
					.getName());
			String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
					ext);

			if (type == null)
				type = "*/*";

			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), type);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			Downloader.getDownloaderContext().startActivity(intent);
		}

		cancelNotification(bundle);
	}

	/**
	 * This method will set core pool size and relative max pool size from
	 * preferences. It will also call initDownloadPool() to ensure the pool
	 * exists
	 * 
	 * @param sharedPreference
	 *            is a preference that was changed
	 */
	public void resetPoolSize() {
		init();

		executor.setCorePoolSize(downloadPoolSize);
		executor.setMaximumPoolSize(downloadPoolSize * POOL_MAX_MULTIPLIER);
	}

	/**
	 * This method will return core pool size from preferences
	 * 
	 * @return core pool size
	 */
	private int getCorePoolSizeFromPref() {
		return PreferenceHelper.getDownloadPoolSize(getApplicationContext());
	}

	/**
	 * This method will return max pool size from based on core pool size
	 * preferences
	 * 
	 * @return max pool size
	 */

	private int getMaxPoolSizeFromPref() {
		return PreferenceHelper.getDownloadPoolSize(getApplicationContext())
				* POOL_MAX_MULTIPLIER;
	}

	/**
	 * This method will download the given download file id. THis will not
	 * immediately start the download. Caller must attach callback to listen to
	 * the progress.
	 * 
	 * @param downloadId
	 *            is a id of the download
	 * @return asynctask with given file handler
	 */
	public synchronized static DownloadTask downloadFile(Integer downloadId) {
		if (null == downloadId)
			return null;

		final Download download = new Download().retrieve(
				Downloader.getDownloaderContext(), downloadId);

		if (!download.isValid()) {
			download.setState(DownloadState.FAILED);
			download.setFailReason(FailedReason.UNKNOWN_ERROR);

			updateDownloadState(download);

			notifyCallbacksFailed(download);
		}

		// If contains task with given id return the reference instead
		if (downloadTasks.containsKey(download.getId()))
			return downloadTasks.get(download.getId());

		if (TextUtils.isEmpty(download.getUrl())) {
			download.setState(DownloadState.FAILED);
			download.setFailReason(FailedReason.UNKNOWN_ERROR);

			updateDownloadState(download);

			notifyCallbacksFailed(download);
		}

		DownloadTask taskToStart = new DownloadTask(download);
		taskToStart.executeOnExecutor(executor);

		downloadTasks.put(download.getId(), taskToStart);

		return null;
	}

	/**
	 * This method will delete task with given id if exist otherwise null
	 * 
	 * @param ids
	 *            with attached tasks to be deleted
	 */
	public synchronized static void deleteDownload(long[] ids) {
		if (null != ids) {
			List<Integer> listIds = getAsIntegerList(ids);

			for (Integer id : listIds) {
				if (null != id) {
					/**
					 * If contains task, delete, cancel then remove it
					 */
					if (downloadTasks.containsKey(id)) {
						DownloadTask task = downloadTasks.get(id);

						task.delete();

						downloadTasks.remove(id);
					}
				}
			}
		}
	}

	/**
	 * This method will pause task with given id if exist otherwise null
	 * 
	 * @param ids
	 *            with attached tasks to be paused
	 * @return TaskStateResult of successfully paused task
	 */
	public synchronized static TaskStateResult pauseDownload(long[] ids) {
		if (null == ids)
			return null;

		List<Integer> listIds = getAsIntegerList(ids);

		TaskStateResult result = new TaskStateResult();

		for (Integer id : listIds) {
			if (null != id) {
				/**
				 * If contains task, pause, cancel then remove it
				 */
				if (downloadTasks.containsKey(id)) {
					DownloadTask task = downloadTasks.get(id);
					task.cancel(true, true);

					downloadTasks.remove(id);

					result.getTasks().add(task);
				}
			}
		}

		return result;
	}

	/**
	 * This method will pause task with given id if exist otherwise the task
	 * will be started
	 * 
	 * @param download
	 *            with attached tasks to be paused
	 * @return TaskStateResult including success and newly added/fresh tasks
	 */
	public synchronized static TaskStateResult resumeDownload(long[] ids) {
		if (null == ids)
			return null;

		List<Integer> listIds = getAsIntegerList(ids);

		TaskStateResult result = new TaskStateResult();

		for (Integer id : listIds) {
			if (null != id) {
				/**
				 * If contains task cancel and remove it as it should not have
				 * been added before resume is called
				 */
				if (downloadTasks.containsKey(id)) {
					DownloadTask task = downloadTasks.get(id);
					task.cancel(true);

					downloadTasks.remove(id);

					task = downloadFile(id);

					result.getTasks().add(task);
				} else {
					DownloadTask task = downloadFile(id);

					result.getFreshTasks().add(task);
				}
			}
		}

		return result;
	}

	/**
	 * This method will authorize and begin download
	 * 
	 * @param ids
	 *            download id to authorize
	 */
	private synchronized static void authorizeAndBeginDownload(long[] ids) {
		if (null == ids)
			return;

		batchUpdateDownloadState(ids, DownloadState.ADDED_AUTHORIZED);

		List<Integer> listIds = getAsIntegerList(ids);

		for (Integer id : listIds) {
			if (null != id) {
				/**
				 * If contains task cancel and remove it as it should not have
				 * been added before resume is called
				 */
				if (downloadTasks.containsKey(id)) {
					DownloadTask task = downloadTasks.get(id);
					task.cancel(true);
				}

				downloadFile(id);
			}
		}
	}

	/**
	 * This method will get integer list from long array
	 * 
	 * @param ids
	 *            array of long
	 * @return list of integer
	 */
	private static List<Integer> getAsIntegerList(long[] ids) {
		List<Integer> primitiveList = new ArrayList<Integer>();

		for (long id : ids)
			primitiveList.add((int) id);

		return primitiveList;
	}

	/**
	 * This method will return all current tasks id.
	 * 
	 * @param isOnlyActive
	 *            means those that are not paused.
	 * @return all current active ids
	 */
	private synchronized static long[] getCurrentTaskIds(boolean isOnlyActive) {

		ArrayList<Integer> taskIds = new ArrayList<Integer>();

		Iterator<Entry<Integer, DownloadTask>> currentTaskItreator = downloadTasks
				.entrySet().iterator();

		while (currentTaskItreator.hasNext()) {
			Map.Entry<Integer, DownloadTask> taskPair = currentTaskItreator
					.next();

			DownloadTask task = taskPair.getValue();

			if (null != task)
				if (isOnlyActive
						&& DownloadState.DOWNLOADING.equals(task
								.getDownloadState())) {
					taskIds.add(taskPair.getKey());
				} else {
					taskIds.add(taskPair.getKey());
				}
		}

		HashSet<Integer> uniqueTaskSet = new HashSet<Integer>(taskIds);

		long[] uniqueTaskIds = new long[uniqueTaskSet.size()];
		int count = 0;

		for (Integer uniqueTaskId : uniqueTaskSet)
			uniqueTaskIds[count++] = uniqueTaskId;

		return uniqueTaskIds;
	}

	/**
	 * This method will cancel all ongoing tasks
	 * 
	 * @return all sucessfully cancelled tasks
	 */
	public synchronized HashSet<DownloadTask> cancelOngoingTasks() {
		Collection<DownloadTask> ongoingTasks = downloadTasks.values();
		HashSet<DownloadTask> cancelledTasks = new HashSet<DownloadTask>();

		for (DownloadTask task : ongoingTasks) {
			if (null != task) {
				if (task.cancel(true))
					cancelledTasks.add(task);
			}
		}

		return cancelledTasks;
	}

	/**
	 * This method will attach callback for given id.
	 * 
	 * @param id
	 *            is the id
	 * @param callback
	 *            is the callback
	 */
	public synchronized void attachCallback(final int id,
			final DownloadListener callback) {
		if (attachedCallbacks.containsKey(id)) {
			if (!attachedCallbacks.get(id).contains(callback)) {
				attachedCallbacks.get(id).add(callback);

				Log.d(getLogTag(), "Attaching new callback");
			}
		} else {
			final HashSet<DownloadListener> set = new HashSet<DownloadListener>();
			set.add(callback);

			attachedCallbacks.put(id, set);

			Log.d(getLogTag(), "Attaching new callback");
		}
	}

	/**
	 * This method will attach callback for given id and callback.
	 * 
	 * @param id
	 *            is the id
	 * @param callback
	 *            is the callback
	 */
	public synchronized void detachCallback(final int id,
			final DownloadListener callback) {
		if (attachedCallbacks.containsKey(id)) {
			if (attachedCallbacks.get(id).contains(callback)) {
				attachedCallbacks.get(id).remove(callback);

				Log.d(getLogTag(), "Detaching callback");
			}
		}
	}

	/**
	 * This method will notify to callbacks that the task has been started
	 * 
	 * @param download
	 *            is the download object
	 */
	private static void notifyCallbacksStarted(final Download download) {
		if (null != download && null != download.getId()) {
			if (attachedCallbacks.containsKey(download.getId())) {
				final HashSet<DownloadListener> callbacks = attachedCallbacks
						.get(download.getId());

				for (DownloadListener callback : callbacks) {
					callback.onDownloadStarted(download);
				}
			}
		}
	}

	/**
	 * This method will notify to callbacks that the task has been cancelled
	 * 
	 * @param download
	 *            is the download object
	 */
	private static void notifyCallbacksCancelled(final Download download) {
		if (null != download && null != download.getId()) {
			if (attachedCallbacks.containsKey(download.getId())) {
				final HashSet<DownloadListener> callbacks = attachedCallbacks
						.get(download.getId());

				for (DownloadListener callback : callbacks) {
					callback.onDownloadCancelled(download);
				}
			}
		}
	}

	/**
	 * This method will notify to callbacks that the task has been complete
	 * 
	 * @param download
	 *            is the download object
	 */
	private static void notifyCallbacksCompleted(final Download download) {
		if (null != download && null != download.getId()) {
			if (attachedCallbacks.containsKey(download.getId())) {
				final HashSet<DownloadListener> callbacks = attachedCallbacks
						.get(download.getId());

				for (DownloadListener callback : callbacks) {
					callback.onDownloadCompleted(download);
				}
			}
		}
	}

	/**
	 * This method will notify to callbacks that the task has been failed
	 * 
	 * @param download
	 *            is the download object
	 */
	private static void notifyCallbacksFailed(final Download download) {
		if (null != download && null != download.getId()) {
			if (attachedCallbacks.containsKey(download.getId())) {
				final HashSet<DownloadListener> callbacks = attachedCallbacks
						.get(download.getId());

				download.setState(DownloadState.FAILED);

				for (DownloadListener callback : callbacks) {
					callback.onDownloadFailed(download);
				}
			}
		}
	}

	/**
	 * This method will notify progress to callbacks
	 * 
	 * @param id
	 *            is the id of the task
	 * @param download
	 *            is the download object
	 * @param progress
	 *            is the progress
	 */
	private static void notifyCallbacksProgress(final Download download,
			final int progress) {
		if (null != download && null != download.getId()) {
			if (attachedCallbacks.containsKey(download.getId())) {
				final HashSet<DownloadListener> callbacks = attachedCallbacks
						.get(download.getId());

				for (DownloadListener callback : callbacks) {
					callback.onDownloadProgress(download, progress);
				}
			}
		}
	}

	/**
	 * This method will update the given download size using a content provider
	 * 
	 * @param download
	 *            is the download object
	 * @state is the size is the download size/content length
	 */
	private static void updateDownloadSize(Download download, String size) {

		ContentValues values = new ContentValues();
		values.put(DownloadsDatabase.COLUMN_SIZE, size);

		if (null != Downloader.getDownloaderContext())
			Downloader
					.getDownloaderContext()
					.getContentResolver()
					.update(DownloadContentProvider.CONTENT_URI_DOWNLOADS,
							values,
							QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID,
									download.getId(), true), null);
	}

	/**
	 * This method will update the given download state, path and failure reason
	 * using a content provider
	 * 
	 * @param download
	 *            is the download object
	 * @state is the download state
	 */
	private static void updateDownloadState(Download download) {

		ContentValues values = new ContentValues();
		values.put(DownloadsDatabase.COLUMN_PATH, download.getPath());
		values.put(DownloadsDatabase.COLUMN_STATE, download.getState()
				.toString());

		if (null != download.getFailReason())
			values.put(DownloadsDatabase.COLUMN_FAIL_REASON, download
					.getFailReason().toString());

		if (DownloadState.COMPLETED.equals(download.getState()))
			values.put(DownloadsDatabase.COLUMN_DATE_COMPLETED,
					download.getDateCompleted());

		if (null != Downloader.getDownloaderContext())
			Downloader
					.getDownloaderContext()
					.getContentResolver()
					.update(DownloadContentProvider.CONTENT_URI_DOWNLOADS,
							values,
							QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID,
									download.getId(), true), null);
	}

	/**
	 * This method will update the given download state in batch using a content
	 * provider
	 * 
	 * @param downloadIds
	 *            is the ids of the downloads
	 * @state is the download state
	 */
	private static void batchUpdateDownloadState(long[] downloadIds,
			DownloadState state) {
		ArrayList<ContentProviderOperation> updateOperations = new ArrayList<ContentProviderOperation>();

		for (long downloadId : downloadIds) {
			updateOperations
					.add(ContentProviderOperation
							.newUpdate(
									DownloadContentProvider.CONTENT_URI_DOWNLOADS)
							.withSelection(
									DownloadsDatabase.COLUMN_ID + " = ?",
									new String[] { Long.toString(downloadId) })
							.withValue(DownloadsDatabase.COLUMN_STATE,
									state.toString()).build());
		}

		try {
			ContentProviderResult[] operationsResult = Downloader
					.getDownloaderContext()
					.getContentResolver()
					.applyBatch(DownloadContentProvider.AUTHORITY,
							updateOperations);

			if (operationsResult.length > 0)
				Log.d(getLogTag(), "Batch update successfull");
			else
				Log.d(getLogTag(), "Batch update failed");
		} catch (RemoteException e) {
			// ignored
		} catch (OperationApplicationException e) {
			// ignored
		}
	}

	/**
	 * This is a task to download the file
	 * 
	 * 
	 * This will perform validation and init download when all validation
	 * passes. Validation is performed in the following steps:
	 * 
	 * 1.Check if parameters of {@link Download} is valid.
	 * 
	 * 2.Check if network is connected.
	 * 
	 * 3.Check if storage is writable.
	 * 
	 * 4.Check if file size exceeded. (If authorized by user this will be
	 * ignored)
	 * 
	 * 5.Check if storage is available.
	 * 
	 * 6.Check if file already exists.
	 * 
	 * @author Milan
	 * 
	 */
	public static class DownloadTask extends AsyncTask<Void, Integer, Download> {

		private Download download;

		private Builder progressBuilder;

		private TimerTask notificationUpdateTask;
		private Timer notificationUpdateTimer;
		private int progress = 0;

		private boolean deleteRequested = false;
		private boolean preserveStateOnCancel = false;

		// Download constancts
		private static final String RANGE_HEADER = "Range";
		private static final String RANGE_VALUE = "bytes=%d-";
		private static final String TEMP_SUFFIX = ".tmp";
		private static final int BUFFER_SIZE = 8 * 1024;

		// IO objects
		private static InputStream remoteContentStream = null;
		private static BufferedInputStream bufferedFileStream = null;

		private static File targetLocalFile = null;
		private static File targetTempFile = null;
		private static RandomAccessFile targetWriteFile = null;

		public DownloadTask(Download download) {
			this.download = download;

			notificationUpdateTask = new TimerTask() {

				@Override
				public void run() {
					publishProgress(progress);
				}
			};

			notificationUpdateTimer = new Timer();
		}

		@Override
		protected Download doInBackground(Void... arg) {

			if (null != Downloader.getDownloaderContext()) {
				if (!download.isValid()) {
					Log.d(getLogTag(), "Parameters invalid");

					download.setState(DownloadState.FAILED);
					download.setFailReason(FailedReason.UNKNOWN_ERROR);

					updateDownloadState(download);
				} else if (!NetworkUtils.isNetworkConnected(Downloader
						.getDownloaderContext())) {
					Log.d(getLogTag(), "network disconnected");

					download.setState(DownloadState.FAILED);
					download.setFailReason(FailedReason.NETWORK_NOTAVAILABLE);

					updateDownloadState(download);
				} else if (!FileUtils.isStorageWritable()) {
					Log.d(getLogTag(), "storage not writable");

					download.setState(DownloadState.FAILED);
					download.setFailReason(FailedReason.STORAGE_NOTWRITABLE);

					updateDownloadState(download);
				} else {
					Log.d(getLogTag(),
							"parms valid, network available and storage writable");

					if (Downloader.HTTP_VALIDATOR.isValid(download.getUrl())) {
						performHTTPDownload();
					} else if (Downloader.FTP_VALIDATOR.isValid(download
							.getUrl())) {
						performFTPDownload();
					} else {
						download.setState(DownloadState.FAILED);
						download.setFailReason(FailedReason.UNKNOWN_ERROR);

						updateDownloadState(download);

						notifyCallbacksFailed(download);
					}

				}
			}

			if (null != notificationUpdateTask)
				notificationUpdateTask.cancel();

			return download;
		}

		/**
		 * This method performs an FTP download
		 */
		private void performFTPDownload() {
			final FTPClient downloadClient = NetworkUtils.getFTPClient();

			try {
				final URL downloadUrl = new URL(download.getUrl());
				final Credential credential = new Credential()
						.retrieve(Downloader
								.getDownloaderContext()
								.getContentResolver()
								.query(CredentialContentProvider.CONTENT_URI_CREDENTIALS,
										null,
										QueryHelper
												.getWhere(
														CredentialsDatabase.COLUMN_HOST,
														downloadUrl.getHost(),
														true), null, null));

				final int downloadPort = downloadUrl.getPort() == -1 ? downloadUrl
						.getDefaultPort() : downloadUrl.getPort();

				downloadClient.connect(downloadUrl.getHost(), downloadPort);
				downloadClient.enterLocalPassiveMode();

				if (downloadClient.login(credential.getUsername(),
						credential.getPassword())) {
					downloadClient.setFileType(FTPClient.BINARY_FILE_TYPE);

					/**
					 * SIZE is an optional command; i.e. even RFC 3659 compliant
					 * servers are not required to support it
					 */
					final FTPFile remoteFile = downloadClient
							.mlistFile(downloadUrl.getPath());

					if (null == remoteFile)
						throw new IOException("remoteFile is null");

					final long fileSize = remoteFile.getSize();
					long tempfileSize = 0L;

					updateDownlaodSize(fileSize);

					if (DownloadState.ADDED_NOTAUTHORIZED.equals(download
							.getState())
							&& fileSize >= FileUtils.getStorageSizeAsByte(
									downloadLimitType, downloadLimitSize)) {
						Log.d(getLogTag(), "not authorized and limit exceeded");

						download.setState(DownloadState.ADDED_NOTAUTHORIZED);

						updateDownloadState(download);

						showWarningNotification(download);
					} else {
						Log.d(getLogTag(), "authorized and limit not exceeded");

						download.setState(DownloadState.ADDED_AUTHORIZED);

						updateDownloadState(download);

						if (!FileUtils.isStorageSpaceAvailable(fileSize)) {
							download.setState(DownloadState.FAILED);
							download.setFailReason(FailedReason.STORAGE_NOTAVAILABLE);

							updateDownloadState(download);
						} else {
							Log.d(getLogTag(), "storage available");

							targetLocalFile = new File(download.getPath());

							// If file exist mark completed otherwise
							// progress
							if (targetLocalFile.exists()
									&& fileSize == targetLocalFile.length()) {
								Log.d(getLogTag(), "file exists");

								download.setState(DownloadState.COMPLETED);
								download.setDateCompleted(new Date().getTime());

								updateDownloadState(download);
							} else {
								Log.d(getLogTag(), "file does not exist");

								byte[] buffer = new byte[BUFFER_SIZE];
								int chunkSize = 0;
								int chunkCompleted = 0;
								int chunkCopied = 0;

								targetTempFile = new File(
										FilenameUtils.getFullPath(download
												.getPath()),
										FilenameUtils.getName(download
												.getPath()) + TEMP_SUFFIX);

								/*
								 * If temp file exist add header to request
								 * remainin content
								 */
								if (targetTempFile.exists()
										&& targetTempFile.length() < fileSize) {
									downloadClient
											.setRestartOffset(targetTempFile
													.length());

									remoteContentStream = downloadClient
											.retrieveFileStream(downloadUrl
													.getPath());

									tempfileSize = targetTempFile.length();

									chunkCompleted = (int) tempfileSize;

									Log.d(getLogTag(), "temp exists");
								} else
									remoteContentStream = downloadClient
											.retrieveFileStream(downloadUrl
													.getPath());

								if (null == remoteContentStream)
									throw new IOException(
											"remoteContentStream is null");

								targetWriteFile = new RandomAccessFile(
										targetTempFile, "rw");

								bufferedFileStream = new BufferedInputStream(
										remoteContentStream, BUFFER_SIZE);

								// Seek to target. If temp seeks to length
								// otherwise
								// 0
								targetWriteFile.seek(targetWriteFile.length());

								notificationUpdateTimer.schedule(
										notificationUpdateTask, 0,
										NOTIFICATION_PROGRESS_TIMER);
								showOrUpdateProgress(null);

								Log.d(getLogTag(), "download started");

								while (-1 != (chunkSize = remoteContentStream
										.read(buffer))) {
									targetWriteFile.write(buffer, 0, chunkSize);

									chunkCompleted += chunkSize;
									chunkCopied += chunkSize;

									progress = (int) ((double) chunkCompleted
											/ (double) fileSize * 100.0);
								}

								showOrUpdateProgress(NOTIFICATION_MAX_PROGRESS);

								if ((tempfileSize + chunkCopied) != fileSize
										&& fileSize != -1) {
									Log.d(getLogTag(), "Incomplete download");

									download.setState(DownloadState.FAILED);
									download.setFailReason(FailedReason.FILE_INCOMPLETE);

									updateDownloadState(download);
								} else {
									Log.d(getLogTag(), "Complete download");

									targetTempFile.renameTo(targetLocalFile);

									download.setState(DownloadState.COMPLETED);
									download.setDateCompleted(new Date()
											.getTime());

									updateDownloadState(download);
								}
							}
						}
					}
				} else {
					download.setState(DownloadState.FAILED);
					download.setFailReason(FailedReason.NETWORK_UNAUTHORIZED);

					updateDownloadState(download);
				}

			} catch (MalformedURLException ex) {
				Log.e(getLogTag(), "Malformed url exception occoured", ex);

				download.setState(DownloadState.FAILED);
				download.setFailReason(FailedReason.NETWORK_ERROR);

				updateDownloadState(download);
			} catch (ConnectTimeoutException ex) {
				Log.e(getLogTag(), "Connection timeout exception occoured", ex);

				download.setState(DownloadState.FAILED);
				download.setFailReason(FailedReason.NETWORK_ERROR);

				updateDownloadState(download);
			} catch (IOException ex) {
				Log.e(getLogTag(), "IO exception occoured", ex);

				download.setState(DownloadState.FAILED);
				download.setFailReason(FailedReason.IO_ERROR);

				updateDownloadState(download);
			} finally {
				NetworkUtils.killFTPClient(downloadClient);

				IOUtils.close(targetWriteFile);
				IOUtils.close(remoteContentStream);
				IOUtils.close(bufferedFileStream);
			}

			cancelNotification(NOTIFICATION_TAG_PROGRESS, download.getId());
		}

		/**
		 * This method performs an HTTP download
		 */
		private void performHTTPDownload() {
			try {
				HttpClient downloadClient = NetworkUtils.getHttpClient();

				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setSoTimeout(params, 60000);

				HttpGet request = new HttpGet(download.getUrl());
				request.setParams(params);

				HttpResponse response = downloadClient.execute(request);

				remoteContentStream = response.getEntity().getContent();

				if (null == remoteContentStream)
					throw new IOException("remoteContentStream is null");

				long fileSize = response.getEntity().getContentLength();
				long tempfileSize = 0L;

				updateDownlaodSize(fileSize);

				if (DownloadState.ADDED_NOTAUTHORIZED.equals(download
						.getState())
						&& fileSize >= FileUtils.getStorageSizeAsByte(
								downloadLimitType, downloadLimitSize)) {
					Log.d(getLogTag(), "not authorized and limit exceeded");

					download.setState(DownloadState.ADDED_NOTAUTHORIZED);

					updateDownloadState(download);

					showWarningNotification(download);
				} else {
					Log.d(getLogTag(), "authorized and limit not exceeded");

					download.setState(DownloadState.ADDED_AUTHORIZED);

					updateDownloadState(download);

					if (!FileUtils.isStorageSpaceAvailable(fileSize)) {
						download.setState(DownloadState.FAILED);
						download.setFailReason(FailedReason.STORAGE_NOTAVAILABLE);

						updateDownloadState(download);
					} else {
						Log.d(getLogTag(), "storage available");

						targetLocalFile = new File(download.getPath());

						// If file exist mark completed otherwise
						// progress
						if (targetLocalFile.exists()
								&& fileSize == targetLocalFile.length()) {
							Log.d(getLogTag(), "file exists");

							download.setState(DownloadState.COMPLETED);
							download.setDateCompleted(new Date().getTime());

							updateDownloadState(download);
						} else {
							Log.d(getLogTag(), "file does not exist");

							byte[] buffer = new byte[BUFFER_SIZE];
							int chunkSize = 0;
							int chunkCompleted = 0;
							int chunkCopied = 0;

							targetTempFile = new File(
									FilenameUtils.getFullPath(download
											.getPath()),
									FilenameUtils.getName(download.getPath())
											+ TEMP_SUFFIX);

							/*
							 * If temp file exist add header to request remainin
							 * content
							 */
							if (targetTempFile.exists()
									&& targetTempFile.length() < fileSize) {
								request.addHeader(RANGE_HEADER, String.format(
										RANGE_VALUE, targetTempFile.length()));

								response = downloadClient.execute(request);

								remoteContentStream = response.getEntity()
										.getContent();

								if (null == remoteContentStream)
									throw new IOException(
											"remoteContentStream is null");

								tempfileSize = targetTempFile.length();

								chunkCompleted = (int) tempfileSize;

								Log.d(getLogTag(), "temp exists");
							}

							targetWriteFile = new RandomAccessFile(
									targetTempFile, "rw");

							bufferedFileStream = new BufferedInputStream(
									remoteContentStream, BUFFER_SIZE);

							// Seek to target. If temp seeks to length
							// otherwise
							// 0
							targetWriteFile.seek(targetWriteFile.length());

							notificationUpdateTimer.schedule(
									notificationUpdateTask, 0,
									NOTIFICATION_PROGRESS_TIMER);
							showOrUpdateProgress(null);

							Log.d(getLogTag(), "download started");

							while (-1 != (chunkSize = remoteContentStream
									.read(buffer))) {
								targetWriteFile.write(buffer, 0, chunkSize);

								chunkCompleted += chunkSize;
								chunkCopied += chunkSize;

								progress = (int) ((double) chunkCompleted
										/ (double) fileSize * 100.0);
							}

							showOrUpdateProgress(NOTIFICATION_MAX_PROGRESS);

							if ((tempfileSize + chunkCopied) != fileSize
									&& fileSize != -1) {
								Log.d(getLogTag(), "Incomplete download");

								download.setState(DownloadState.FAILED);
								download.setFailReason(FailedReason.FILE_INCOMPLETE);

								updateDownloadState(download);
							} else {
								Log.d(getLogTag(), "Complete download");

								targetTempFile.renameTo(targetLocalFile);

								download.setState(DownloadState.COMPLETED);
								download.setDateCompleted(new Date().getTime());

								updateDownloadState(download);
							}
						}

					}
				}

			} catch (ClientProtocolException ex) {
				Log.e(getLogTag(), "Client protocol exception occoured", ex);

				download.setState(DownloadState.FAILED);
				download.setFailReason(FailedReason.NETWORK_ERROR);

				updateDownloadState(download);

			} catch (ConnectTimeoutException ex) {
				Log.e(getLogTag(), "Connection timeout exception occoured", ex);

				download.setState(DownloadState.FAILED);
				download.setFailReason(FailedReason.NETWORK_ERROR);

				updateDownloadState(download);

			} catch (FileNotFoundException ex) {
				Log.e(getLogTag(), "File not found exception occoured", ex);

				download.setState(DownloadState.FAILED);
				download.setFailReason(FailedReason.IO_ERROR);

				updateDownloadState(download);

			} catch (IOException ex) {
				Log.e(getLogTag(), "IO exception occoured", ex);

				download.setState(DownloadState.FAILED);
				download.setFailReason(FailedReason.IO_ERROR);

				updateDownloadState(download);
			} finally {
				IOUtils.close(targetWriteFile);
				IOUtils.close(remoteContentStream);
				IOUtils.close(bufferedFileStream);
			}

			cancelNotification(NOTIFICATION_TAG_PROGRESS, download.getId());
		}

		/**
		 * This method will show or update notification progress
		 * 
		 * @param progress
		 *            null if intermediate otherwise number
		 */
		private void showOrUpdateProgress(final Integer progress) {
			if (null == progressBuilder)
				progressBuilder = getProgressNotification(download);

			if (null != progressBuilder) {
				if (null == progress)
					progressBuilder.setProgress(0, 0, true);
				else
					progressBuilder.setProgress(NOTIFICATION_MAX_PROGRESS,
							progress, false);

				DownloadService.notify(NOTIFICATION_TAG_PROGRESS,
						download.getId(), progressBuilder.build());
			}
		}

		/**
		 * This method will update download size
		 */
		private void updateDownlaodSize(long fileSize) {
			if (null == download.getSize()) {
				ByteType type = PreferenceHelper
						.getDownloadLimitType(Downloader.getDownloaderContext());

				StringBuilder sizeBuilder = new StringBuilder("");

				sizeBuilder.append(FileUtils.getStorageSizeAs(type, fileSize));
				sizeBuilder.append(" ");
				sizeBuilder.append(type.toString());

				download.setSize(sizeBuilder.toString());

				updateDownloadSize(download, download.getSize());
			}
		}

		/**
		 * Deletes temp file if exist and is a file
		 */
		public void delete() {
			deleteRequested = true;

			cancel(true);

			cancelNotification(NOTIFICATION_TAG_PROGRESS, download.getId());

			if (null != targetTempFile && targetTempFile.isFile()
					&& targetTempFile.exists())
				targetTempFile.delete();
		}

		/**
		 * This method return the download state. if it does not exist it will
		 * return state unknown
		 * 
		 * @return
		 */
		public DownloadState getDownloadState() {
			if (null != download && null != download.getState())
				return download.getState();

			return DownloadState.UNKNOWN;
		}

		/**
		 * This method will cancel this task allowing caller to persist the
		 * download state. Useful when pausing download
		 * 
		 * @param preserveStateOnCancel
		 *            if true will persist the current state on cancel
		 * @param mayInterruptIfRunning
		 */
		public void cancel(boolean preserveStateOnCancel,
				boolean mayInterruptIfRunning) {
			this.preserveStateOnCancel = preserveStateOnCancel;

			cancel(mayInterruptIfRunning);
		}

		@Override
		protected void onCancelled(Download result) {
			if (null != notificationUpdateTask)
				notificationUpdateTask.cancel();

			if (!deleteRequested) {
				if (preserveStateOnCancel) {
					if (null != download && null != download.getState())
						updateDownloadState(download);
				} else {
					download.setState(DownloadState.CANCELLED);
					updateDownloadState(download);
				}

				notifyCallbacksCancelled(result);
			}

			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Download result) {
			if (DownloadState.FAILED.equals(download.getState())) {
				showCompletedNotification(download);

				notifyCallbacksFailed(download);
			} else if (DownloadState.COMPLETED.equals(download.getState())) {
				showCompletedNotification(download);

				notifyCallbacksCompleted(result);
			}

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			download.setState(DownloadState.DOWNLOADING);
			updateDownloadState(download);

			notifyCallbacksStarted(download);

			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			showOrUpdateProgress(values[0]);

			// For now all
			notifyCallbacksProgress(download, values[0]);

			super.onProgressUpdate(values);
		}
	}

	/**
	 * This is a result state for task state request. It contains tasks whose
	 * states were changed successfully. It will also return set of ids for task
	 * that was not available
	 * 
	 * @author Milan
	 * 
	 */
	public static class TaskStateResult {
		private HashSet<DownloadTask> tasks = new HashSet<DownloadTask>();
		private HashSet<DownloadTask> freshTasks = new HashSet<DownloadTask>();

		/**
		 * @return the tasks
		 */
		public HashSet<DownloadTask> getTasks() {
			return tasks;
		}

		/**
		 * @param tasks
		 *            the tasks to set
		 */
		public void setTasks(HashSet<DownloadTask> tasks) {
			this.tasks = tasks;
		}

		/**
		 * @return the freshTasks
		 */
		public HashSet<DownloadTask> getFreshTasks() {
			return freshTasks;
		}

		/**
		 * @param freshTasks
		 *            the freshTasks to set
		 */
		public void setFreshTasks(HashSet<DownloadTask> freshTasks) {
			this.freshTasks = freshTasks;
		}

	}

	/**
	 * Receiver that receives action to be recieved by the service
	 * 
	 * @author Milan
	 * 
	 */
	public static class ServiceActionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null != intent && null != intent.getExtras())
				if (intent.getAction().equals(
						ConnectivityManager.CONNECTIVITY_ACTION)) {
					Integer action = getNetworkChangesAction(context);

					if (null != action) {
						if (action == HANDLE_NETWORK_CONNECTED)
							intent.getExtras().putLongArray(
									KEY_DOWNLOAD_ACTIVEIDS,
									getCurrentTaskIds(false));
						else if (action == HANDLE_NETWORK_DISCONNECTED)
							intent.getExtras().putLongArray(
									KEY_DOWNLOAD_ACTIVEIDS,
									getCurrentTaskIds(true));

						postAction(intent.getExtras(), action);
					}
				} else if (intent.getAction().equals(ACTION_CONTINUE)) {
					cancelNotification(intent.getExtras());

					if (intent.getIntExtra(KEY_DOWNLOADID, -1) > -1)
						postAction(intent.getExtras(), HANDLE_CONTINUE_DOWNLOAD);
				} else if (intent.getAction().equals(ACTION_PAUSE)) {
					if (intent.getIntExtra(KEY_DOWNLOADID, -1) > -1)
						postAction(intent.getExtras(), HANDLE_PAUSE_DOWNLOAD);
				} else if (intent.getAction().equals(ACTION_RESUME)) {
					if (intent.getIntExtra(KEY_DOWNLOADID, -1) > -1)
						postAction(intent.getExtras(), HANDLE_RESUME_DOWNLOAD);
				} else if (intent.getAction().equals(ACTION_COMPLETE)) {
					cancelNotification(intent.getExtras());
				} else if (intent.getAction().equals(ACTION_OPEN)) {
					viewFile(intent.getExtras());
				}
		}

		/**
		 * This method will get network state and check if anything has changed
		 * 
		 * @param context
		 *            is the callers context
		 * 
		 * @return action to be performed. Null if nothing has to be done
		 */
		private Integer getNetworkChangesAction(Context context) {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();

			if (networkInfo != null)
				if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI
						&& NetworkType.WIFI.equals(downloadNetworkType)) {

					if (networkInfo.isConnected())
						return HANDLE_NETWORK_CONNECTED;
					else
						return HANDLE_NETWORK_DISCONNECTED;
				} else if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
						|| networkInfo.getType() == ConnectivityManager.TYPE_MOBILE
						|| networkInfo.getType() == ConnectivityManager.TYPE_WIMAX
						|| networkInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
					if (networkInfo.isConnected())
						return HANDLE_NETWORK_CONNECTED;
					else
						return HANDLE_NETWORK_DISCONNECTED;
				}

			return null;
		}

		/**
		 * This method will post recieved action to the service
		 * 
		 * @param data
		 *            is the data to be posted
		 * @param what
		 *            is an action to be performed
		 */
		private void postAction(Bundle data, int what) {
			Message msg = Message.obtain(null, what);
			msg.setData(data);

			if (null != serviceActionHandler)
				serviceActionHandler.sendMessage(msg);
		}

	}

}
