package com.milanix.example.downloader.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.milanix.example.downloader.HomeActivity;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadListener;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.dao.Download.FailedReason;
import com.milanix.example.downloader.data.dao.Download.TaskState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog.NetworkType;
import com.milanix.example.downloader.util.FileUtils;
import com.milanix.example.downloader.util.FileUtils.ByteType;
import com.milanix.example.downloader.util.IOUtils;
import com.milanix.example.downloader.util.NetworkUtils;
import com.milanix.example.downloader.util.PreferenceHelper;
import com.milanix.example.downloader.util.TextHelper;

/**
 * This is a download service
 * 
 * @author Milan
 * 
 */
public class DownloadService extends Service {

	// Notification ids
	private static final int NOTIFICATION_ID_WARNING = 1000;

	// Notification request codes
	private static final int NOTIFICATION_REQCODE_CONTINUE = 1000;
	private static final int NOTIFICATION_REQCODE_RESUME = 1001;
	private static final int NOTIFICATION_REQCODE_PAUSE = 1002;

	// Handler's whatss
	private static final int HANDLE_NETWORK_CONNECTED = 0;
	private static final int HANDLE_NETWORK_DISCONNECTED = 1;

	private static final int HANDLE_CONTINUE_DOWNLOAD = 100;
	private static final int HANDLE_PAUSE_DOWNLOAD = 101;
	private static final int HANDLE_RESUME_DOWNLOAD = 102;

	// Notification tags
	private static final String NOTIFICATION_TAG_WARNING = "notification_tag_warning";
	private static final String NOTIFICATION_TAG_PROGRESS = "notification_tag_progress";

	// Notification actions
	private static final String ACTION_CONTINUE = "action_continue";
	private static final String ACTION_RESUME = "action_resume";
	private static final String ACTION_PAUSE = "action_pause";

	// Notification keys
	private static final String KEY_DOWNLOADID = "key_downloadid";
	private static final String KEY_NOTIFICATION_TAG = "key_notification_tag";
	private static final String KEY_NOTIFICATION_ID = "key_notification_id";
	private static final String KEY_DOWNLOAD_ACTIVEIDS = "key_download_activeids";

	private static Context context;

	private ServiceActionReceiver serviceActionReceiver;
	private static NotificationManager notificationManager;

	// Shared preference instance
	private static SharedPreferences sharedPreferenced;
	private static OnSharedPreferenceChangeListener sharedPrefChangeListener;

	// Download user configured params
	private static Integer downloadPoolSize;
	private static NetworkType downloadNetworkType;
	private static Integer downloadLimitSize;
	private static ByteType downloadLimitType;

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
		if (null == context)
			context = getApplicationContext();

		if (null == serviceActionReceiver)
			serviceActionReceiver = new ServiceActionReceiver();

		if (null == notificationManager) {
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			notificationManager.cancelAll();
		}

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
	 * This method can be used to retrieve context from static methods
	 * 
	 * @return current application context. May be null
	 */
	private static Context getStaticContext() {
		return context;
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
	 * This method will show progress notification for given download
	 * 
	 * @param download
	 *            to be attached to this notification
	 */
	private static void showProgressNotification(Download download) {
		if (download.isValid() && null != getStaticContext()) {

			Intent resultIntent = new Intent(getStaticContext(),
					HomeActivity.class);

			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					getStaticContext(), 0, resultIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			Intent resumeIntent = new Intent(getStaticContext(),
					ServiceActionReceiver.class);
			resumeIntent.setAction(ACTION_RESUME);
			resumeIntent.putExtra(KEY_DOWNLOADID, download.getId());
			resumeIntent.putExtra(KEY_NOTIFICATION_TAG,
					NOTIFICATION_TAG_PROGRESS);
			resumeIntent.putExtra(KEY_NOTIFICATION_ID, download.getId());

			Intent pauseIntent = new Intent(getStaticContext(),
					ServiceActionReceiver.class);
			pauseIntent.setAction(ACTION_PAUSE);
			pauseIntent.putExtra(KEY_DOWNLOADID, download.getId());
			pauseIntent.putExtra(KEY_NOTIFICATION_TAG,
					NOTIFICATION_TAG_PROGRESS);
			pauseIntent.putExtra(KEY_NOTIFICATION_ID, download.getId());

			PendingIntent pendingIntentResume = PendingIntent.getBroadcast(
					getStaticContext(), NOTIFICATION_REQCODE_RESUME,
					resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			PendingIntent pendingIntentPause = PendingIntent.getBroadcast(
					getStaticContext(), NOTIFICATION_REQCODE_PAUSE,
					pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder progressBuilder = new NotificationCompat.Builder(
					getStaticContext());
			progressBuilder
					.setContentTitle(download.getName())
					.setContentText(download.getUrl())
					.setSmallIcon(R.drawable.ic_icon_download_dark)
					.setProgress(0, 0, true)
					.setContentIntent(resultPendingIntent)
					.setDefaults(Notification.DEFAULT_ALL)
					.setStyle(
							new NotificationCompat.BigTextStyle()
									.bigText(download.getUrl()))
					.addAction(R.drawable.ic_action_resume_dark,
							getStaticContext().getString(R.string.btn_resume),
							pendingIntentResume)
					.addAction(R.drawable.ic_action_pause_dark,
							getStaticContext().getString(R.string.btn_pause),
							pendingIntentPause).setOngoing(true);

			notificationManager.notify(NOTIFICATION_TAG_PROGRESS,
					download.getId(), progressBuilder.build());
		}

	}

	/**
	 * This method will show warning notification for given download
	 * 
	 * @param download
	 *            to be attached to this notification
	 */
	private static void showWarningNotification(Download download) {
		if (download.isValid() && null != getStaticContext()) {
			Intent resultIntent = new Intent(getStaticContext(),
					HomeActivity.class);

			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					getStaticContext(), 0, resultIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			Intent continueIntent = new Intent(getStaticContext(),
					ServiceActionReceiver.class);
			continueIntent.setAction(ACTION_CONTINUE);
			continueIntent.putExtra(KEY_DOWNLOADID, download.getId());
			continueIntent.putExtra(KEY_NOTIFICATION_TAG,
					NOTIFICATION_TAG_WARNING);
			continueIntent.putExtra(KEY_NOTIFICATION_ID,
					NOTIFICATION_ID_WARNING);

			PendingIntent pendingIntentContinue = PendingIntent.getBroadcast(
					getStaticContext(), NOTIFICATION_REQCODE_CONTINUE,
					continueIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			String message = "";
			String title = "";

			// Message context based on if size is determined or not
			if (null == download.getSize()) {
				title = getStaticContext().getString(
						R.string.download_size_unknown_title);

				message = String.format(
						getStaticContext().getString(
								R.string.download_size_unknown_message),
						download.getName());
			} else {
				title = getStaticContext().getString(
						R.string.download_size_exceeded_title);

				message = String.format(
						getStaticContext().getString(
								R.string.download_size_exceeded_message),
						download.getName(), PreferenceHelper
								.getDownloadLimitSize(getStaticContext()),
						PreferenceHelper
								.getDownloadLimitType(getStaticContext()));
			}

			NotificationCompat.Builder warningBuilder = new NotificationCompat.Builder(
					getStaticContext())
					.setSmallIcon(R.drawable.ic_icon_limit_dark)
					.setContentTitle(title)
					.setContentText(message)
					.setContentIntent(resultPendingIntent)
					.setDefaults(Notification.DEFAULT_ALL)
					.setStyle(
							new NotificationCompat.BigTextStyle()
									.bigText(message))
					.addAction(
							R.drawable.ic_action_resume_dark,
							getStaticContext().getString(R.string.btn_continue),
							pendingIntentContinue);

			notificationManager.notify(NOTIFICATION_TAG_WARNING,
					NOTIFICATION_ID_WARNING, warningBuilder.build());
		}
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
			Integer notificationId = bundle.getInt(KEY_NOTIFICATION_ID);
			String notificationTag = bundle.getString(KEY_NOTIFICATION_TAG);

			cancelNotification(notificationTag, notificationId);
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
	public static DownloadTask downloadFile(Integer downloadId) {
		if (null == downloadId)
			return null;

		Download download = new Download().retrieve(context, downloadId);

		if (!download.isValid())
			notifyCallbacksFailed(download, FailedReason.UNKNOWN_ERROR);

		// If contains task with given id return the reference instead
		if (downloadTasks.containsKey(download.getId()))
			return downloadTasks.get(download.getId());

		if (TextHelper.isStringEmpty(download.getUrl()))
			notifyCallbacksFailed(download, FailedReason.UNKNOWN_ERROR);

		DownloadTask taskToStart = new DownloadTask(download);
		taskToStart.executeOnExecutor(executor);

		downloadTasks.put(download.getId(), taskToStart);

		return taskToStart;
	}

	/**
	 * This method will pause task with given id if exist otherwise null
	 * 
	 * @param ids
	 *            with attached tasks to be paused
	 * @return TaskStateResult of successfully paused task
	 */
	public static TaskStateResult pauseDownload(long[] ids) {
		if (null == ids)
			return null;

		List<Integer> listIds = getAsIntegerList(ids);

		TaskStateResult result = new TaskStateResult();

		for (Integer id : listIds) {
			if (null != id) {
				if (downloadTasks.containsKey(id)) {
					DownloadTask task = downloadTasks.get(id);
					task.pauseTask();

					result.getSuccessTasks().add(task);
				} else {
					// Paused tasks need nothing to be done
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
	public static TaskStateResult resumeDownload(long[] ids) {
		if (null == ids)
			return null;

		List<Integer> listIds = getAsIntegerList(ids);

		TaskStateResult result = new TaskStateResult();

		for (Integer id : listIds) {
			if (null != id) {
				if (downloadTasks.containsKey(id)) {
					DownloadTask task = downloadTasks.get(id);
					task.resumeTask();

					result.getSuccessTasks().add(task);
				} else {
					DownloadTask freshTask = downloadFile(id);

					if (null != freshTask)
						result.getFreshTasks().add(freshTask);
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
	private static void authorizeAndBeginDownload(long[] ids) {
		if (null == ids)
			return;

		batchUpdateDownloadState(ids, DownloadState.ADDED_AUTHORIZED);

		List<Integer> listIds = getAsIntegerList(ids);

		for (Integer id : listIds) {
			if (null != id) {
				if (downloadTasks.containsKey(id)) {
					DownloadTask task = downloadTasks.get(id);
					task.updateState(DownloadState.ADDED_AUTHORIZED);
					task.resumeTask();
					task.initDownload();
				} else {
					downloadFile(id);
				}
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
	private static long[] getCurrentTaskIds(boolean isOnlyActive) {

		ArrayList<Integer> taskIds = new ArrayList<Integer>();

		Iterator<Entry<Integer, DownloadTask>> currentTaskItreator = downloadTasks
				.entrySet().iterator();

		while (currentTaskItreator.hasNext()) {
			Map.Entry<Integer, DownloadTask> taskPair = currentTaskItreator
					.next();

			DownloadTask task = taskPair.getValue();

			if (null != task)
				if (isOnlyActive && TaskState.RESUMED.equals(task.taskState)) {
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
	public HashSet<DownloadTask> cancelOngoingTasks() {
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
	public void attachCallback(final int id, final DownloadListener callback) {
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
	public void detachCallback(final int id, final DownloadListener callback) {
		if (attachedCallbacks.containsKey(id)) {
			if (!attachedCallbacks.get(id).contains(callback)) {
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
	private static void notifyCallbacksFailed(final Download download,
			final FailedReason reason) {
		if (null != download && null != download.getId()) {
			if (attachedCallbacks.containsKey(download.getId())) {
				final HashSet<DownloadListener> callbacks = attachedCallbacks
						.get(download.getId());

				download.setState(DownloadState.FAILED);

				for (DownloadListener callback : callbacks) {
					callback.onDownloadFailed(download, reason);
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
	private static void notifyCallbacksProgress(final TaskState taskState,
			final Download download, final int progress) {
		if (null != download && null != download.getId()) {
			if (attachedCallbacks.containsKey(download.getId())) {
				final HashSet<DownloadListener> callbacks = attachedCallbacks
						.get(download.getId());

				for (DownloadListener callback : callbacks) {
					callback.onDownloadProgress(taskState, download, progress);
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

		if (null != getStaticContext())
			getStaticContext().getContentResolver().update(
					DownloadContentProvider.CONTENT_URI_DOWNLOADS,
					values,
					QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID,
							download.getId(), true), null);
	}

	/**
	 * This method will update the given download state using a content provider
	 * 
	 * @param download
	 *            is the download object
	 * @state is the download state
	 */
	private static void updateDownloadState(Download download,
			DownloadState state) {

		ContentValues values = new ContentValues();
		values.put(DownloadsDatabase.COLUMN_PATH, download.getPath());
		values.put(DownloadsDatabase.COLUMN_STATE, state.toString());

		if (null != getStaticContext())
			getStaticContext().getContentResolver().update(
					DownloadContentProvider.CONTENT_URI_DOWNLOADS,
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
			ContentProviderResult[] operationsResult = getStaticContext()
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
	 * @author Milan
	 * 
	 */
	public static class DownloadTask extends AsyncTask<Void, Integer, Download> {

		private Download download;
		private TaskState taskState = TaskState.RESUMED;
		private Boolean isPauseNotified = false;

		private static final String RANGE_HEADER = "Range";
		private static final String RANGE_VALUE = "bytes=%d-";
		private static final String TEMP_SUFFIX = ".tmp";
		private static final int BUFFER_SIZE = 1024;

		public DownloadTask(Download download) {
			this.download = download;
		}

		@Override
		protected Download doInBackground(Void... arg) {
			if (null != getStaticContext())
				initDownload();

			return download;
		}

		/**
		 * This method will perform validation and init download when all
		 * validation passes. Validation is performed in the following steps:
		 * 
		 * 1.Check if paramters of {@link Download} is valid.
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
		 */
		private void initDownload() {

			if (!download.isValid()) {
				Log.d(getLogTag(), "Parameters invalid");

				updateDownloadState(download, DownloadState.FAILED);

				notifyCallbacksFailed(download, FailedReason.UNKNOWN_ERROR);
			} else if (!NetworkUtils.isNetworkConnected(getStaticContext())) {
				Log.d(getLogTag(), "network disconnected");

				notifyCallbacksFailed(download,
						FailedReason.NETWORK_NOTAVAILABLE);
			} else if (!FileUtils.isStorageWritable()) {
				Log.d(getLogTag(), "storage not writable");

				notifyCallbacksFailed(download,
						FailedReason.STORAGE_NOTWRITABLE);
			} else {
				Log.d(getLogTag(),
						"parms valid, network available and storage writable");

				InputStream remoteContentStream = null;
				BufferedInputStream bufferedFileStream = null;

				File targetLocalFile = null;
				File targetTempFile = null;
				RandomAccessFile targetWriteFile = null;

				try {
					HttpClient downloadClient = NetworkUtils.getHttpClient();

					HttpParams params = new BasicHttpParams();
					HttpConnectionParams.setSoTimeout(params, 60000);

					HttpGet request = new HttpGet(download.getUrl());
					request.setParams(params);

					HttpResponse response = downloadClient.execute(request);

					remoteContentStream = response.getEntity().getContent();

					long fileSize = response.getEntity().getContentLength();
					long tempfileSize = 0L;

					updateDownlaodSize(fileSize);

					if (DownloadState.ADDED_NOTAUTHORIZED.equals(download
							.getState())
							&& (FileUtils.getStorageSizeAs(downloadLimitType,
									fileSize) >= FileUtils.getStorageSizeAs(
									downloadLimitType, downloadLimitSize))) {
						Log.d(getLogTag(), "not authorized and limit exceeded");

						updateDownloadState(download,
								DownloadState.ADDED_NOTAUTHORIZED);

						showWarningNotification(download);
					} else {
						Log.d(getLogTag(), "authorized and limit not exceeded");

						updateDownloadState(download,
								DownloadState.ADDED_AUTHORIZED);

						if (!FileUtils.isStorageSpaceAvailable(fileSize)) {
							notifyCallbacksFailed(download,
									FailedReason.STORAGE_NOTAVAILABLE);
						} else {
							Log.d(getLogTag(), "storage available");

							targetLocalFile = new File(download.getPath());

							// If file exist mark completed otherwise progress
							if (targetLocalFile.exists()
									&& fileSize == targetLocalFile.length()) {
								Log.d(getLogTag(), "file exists");

								download.setState(DownloadState.COMPLETED);
							} else {
								Log.d(getLogTag(), "file does not exist");

								byte[] buffer = new byte[BUFFER_SIZE];
								int chunkSize = 0;
								int chunkCompleted = 0;
								int chunkProgress = 0;
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
									request.addHeader(RANGE_HEADER, String
											.format(RANGE_VALUE,
													targetTempFile.length()));
									
									response = downloadClient.execute(request);

									remoteContentStream = response.getEntity().getContent();

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

								showProgressNotification(download);

								while (-1 != (chunkSize = remoteContentStream
										.read(buffer))) {
									if (TaskState.RESUMED.equals(taskState)) {
										if (isPauseNotified)
											isPauseNotified = false;

										targetWriteFile.write(buffer, 0,
												chunkSize);

										chunkCompleted += chunkSize;
										chunkCopied += chunkSize;

										chunkProgress = (int) ((double) chunkCompleted
												/ (double) fileSize * 100.0);

										publishProgress(chunkProgress);
									} else {
										if (!isPauseNotified) {
											isPauseNotified = true;

											publishProgress(chunkProgress);
										}
									}
								}

								if ((tempfileSize + chunkCopied) != fileSize
										&& fileSize != -1) {
									Log.d(getLogTag(), "Incomplete download");

									updateDownloadState(download,
											DownloadState.FAILED);

									notifyCallbacksFailed(download,
											FailedReason.IO_ERROR);
								} else {
									Log.d(getLogTag(), "Complete download");

									targetTempFile.renameTo(targetLocalFile);
									download.setState(DownloadState.COMPLETED);
								}

								cancelNotification(NOTIFICATION_TAG_PROGRESS,
										download.getId());
							}
						}
					}

				} catch (ClientProtocolException ex) {
					Log.e(getLogTag(), "IO exception occoured", ex);

					updateDownloadState(download, DownloadState.FAILED);

					notifyCallbacksFailed(download, FailedReason.NETWORK_ERROR);
				} catch (IOException ex) {
					Log.e(getLogTag(), "IO exception occoured", ex);

					updateDownloadState(download, DownloadState.FAILED);

					notifyCallbacksFailed(download, FailedReason.IO_ERROR);
				} finally {
					IOUtils.close(targetWriteFile);
					IOUtils.close(remoteContentStream);
					IOUtils.close(bufferedFileStream);
				}
			}
		}

		/**
		 * This method will update download size
		 */
		private void updateDownlaodSize(long fileSize) {
			if (null == download.getSize()) {
				ByteType type = PreferenceHelper
						.getDownloadLimitType(getStaticContext());

				StringBuilder sizeBuilder = new StringBuilder("");

				sizeBuilder.append(FileUtils.getStorageSizeAs(type, fileSize));
				sizeBuilder.append(" ");
				sizeBuilder.append(type.toString());

				download.setSize(sizeBuilder.toString());

				updateDownloadSize(download, download.getSize());
			}
		}

		/**
		 * This method will update download state for this task
		 * 
		 * @param state
		 *            download state
		 */
		public void updateState(DownloadState state) {
			if (null != download)
				download.setState(state);
		}

		/**
		 * This method will pause the current task.
		 */
		public synchronized void pauseTask() {
			taskState = TaskState.PAUSED;
		}

		/**
		 * This method will resume the current task.
		 */
		public synchronized void resumeTask() {
			taskState = TaskState.RESUMED;
		}

		@Override
		protected void onCancelled(Download result) {
			updateDownloadState(download, DownloadState.CANCELLED);

			notifyCallbacksCancelled(result);

			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Download result) {
			updateDownloadState(download, result.getState());

			notifyCallbacksCompleted(result);

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			updateDownloadState(download, DownloadState.DOWNLOADING);

			notifyCallbacksStarted(download);

			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			notifyCallbacksProgress(taskState, download, values[0]);

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
		private HashSet<DownloadTask> successTasks = new HashSet<DownloadTask>();

		private HashSet<DownloadTask> freshTasks = new HashSet<DownloadTask>();

		/**
		 * @return the successTasks
		 */
		public HashSet<DownloadTask> getSuccessTasks() {
			return successTasks;
		}

		/**
		 * @param successTasks
		 *            the successTasks to set
		 */
		public void setSuccessTasks(HashSet<DownloadTask> successTasks) {
			this.successTasks = successTasks;
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
