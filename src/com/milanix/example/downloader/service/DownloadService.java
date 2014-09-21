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
import java.util.List;
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

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadListener;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.dao.Download.FailedReason;
import com.milanix.example.downloader.data.dao.Download.TaskState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;
import com.milanix.example.downloader.util.FileUtils;
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

	// Map to support multiple callback with same ids but different caller
	private HashMap<Integer, HashSet<DownloadListener>> attachedCallbacks = new HashMap<Integer, HashSet<DownloadListener>>();

	// Map to keep track of async task
	private HashMap<Integer, DownloadTask> downloadTasks = new HashMap<Integer, DownloadTask>();

	// Executor for parallel downloads
	private ThreadPoolExecutor executor;

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

	private final IBinder mBinder = new DownloadBinder();

	public class DownloadBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initDownloadPool();

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
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
	 * This method will init ThreadPoolExecutor with using {@link
	 * PreferenceHelper.getDownloadPoolSize()}
	 * 
	 */
	private void initDownloadPool() {
		if (null == executor)
			executor = new ThreadPoolExecutor(getCorePoolSizeFromPref(),
					getMaxPoolSizeFromPref(), POOL_KEEP_ALIVE,
					TimeUnit.SECONDS, POOL_WORKQUEUE, POOL_THREAD_FACTORY);
	}

	/**
	 * This method will set core pool size and relative max pool size from
	 * preferences. It will also call initDownloadPool() to ensure the pool
	 * exists
	 */
	public void ssetPoolSizeFromPref() {
		initDownloadPool();

		executor.setCorePoolSize(getCorePoolSizeFromPref());
		executor.setMaximumPoolSize(getMaxPoolSizeFromPref());
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
	 * @param download
	 *            is a download file
	 * @return asynctask with given file handler
	 */
	public DownloadTask downloadFile(Download download) {

		if (null == download) {
			notifyCallbacksFailed(download, FailedReason.UNKNOWN_ERROR);

			return null;
		}

		// If contains task with given id return the reference instead
		if (downloadTasks.containsKey(download.getId()))
			return downloadTasks.get(download.getId());

		if (TextHelper.isStringEmpty(download.getUrl()))
			notifyCallbacksFailed(download, FailedReason.UNKNOWN_ERROR);

		DownloadTask taskToStart = new DownloadTask(download);
		taskToStart.executeOnExecutor(executor, download.getUrl());

		downloadTasks.put(download.getId(), taskToStart);

		return taskToStart;
	}

	/**
	 * This method will pause task with given id if exist otherwise null
	 * 
	 * @param ids
	 *            with attached tasks to be paused
	 * @return TaskStateResult including success and other ids
	 */
	public TaskStateResult pauseDownload(long[] ids) {
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
					result.getUncreatedTasks().add(id);
				}
			}
		}

		return result;
	}

	/**
	 * This method will pause task with given id if exist otherwise null
	 * 
	 * @param download
	 *            with attached tasks to be paused
	 * @return TaskStateResult including success and other ids
	 */
	public TaskStateResult resumeDownload(long[] ids) {
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
					result.getUncreatedTasks().add(id);
				}
			}
		}

		return result;
	}

	/**
	 * This method will get integer list from long array
	 * 
	 * @param ids
	 *            array of long
	 * @return list of integer
	 */
	private List<Integer> getAsIntegerList(long[] ids) {
		List<Integer> primitiveList = new ArrayList<Integer>();

		for (long id : ids)
			primitiveList.add((int) id);

		return primitiveList;
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
	private void notifyCallbacksStarted(final Download download) {
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
	private void notifyCallbacksCancelled(final Download download) {
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
	private void notifyCallbacksCompleted(final Download download) {
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
	private void notifyCallbacksFailed(final Download download,
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
	private void notifyCallbacksProgress(final TaskState taskState,
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
	 * This method will update the given download state using a content provider
	 * 
	 * @param download
	 *            is the download object
	 * @state is the download state
	 */
	private void updateDownloadState(Download download, DownloadState state) {
		ContentValues values = new ContentValues();
		values.put(DownloadsDatabase.COLUMN_PATH, download.getPath());
		values.put(DownloadsDatabase.COLUMN_STATE, state.toString());

		getApplicationContext().getContentResolver().update(
				DownloadContentProvider.CONTENT_URI_DOWNLOADS,
				values,
				QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID,
						download.getId(), true), null);
	}

	/**
	 * This is a task to download the file
	 * 
	 * @author Milan
	 * 
	 */
	public class DownloadTask extends AsyncTask<String, Integer, Download> {

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
		protected Download doInBackground(String... arg) {
			if (!NetworkUtils.isNetworkConnected(getApplicationContext())) {

				notifyCallbacksFailed(download,
						FailedReason.NETWORK_NOTAVAILABLE);
			} else if (!FileUtils.isStorageWritable()) {
				notifyCallbacksFailed(download,
						FailedReason.STORAGE_NOTWRITABLE);
			} else {
				Log.d(getLogTag(), "network available and storage writable");

				InputStream remoteContentStream = null;
				BufferedInputStream bufferedFileStream = null;

				File targetLocalFile = null;
				File targetTempFile = null;
				RandomAccessFile targetWriteFile = null;

				try {
					HttpClient downloadClient = NetworkUtils.getHttpClient();

					HttpParams params = new BasicHttpParams();
					HttpConnectionParams.setSoTimeout(params, 60000);

					HttpGet request = new HttpGet(arg[0]);
					request.setParams(params);

					HttpResponse response = downloadClient.execute(request);

					remoteContentStream = response.getEntity().getContent();

					long fileSize = response.getEntity().getContentLength();
					long tempfileSize = 0L;

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
									FilenameUtils.getName(download.getPath())
											+ TEMP_SUFFIX);

							// If temp file exist add header to request
							// remaining
							// one
							if (targetTempFile.exists()) {
								request.addHeader(RANGE_HEADER, String.format(
										RANGE_VALUE, targetTempFile.length()));

								tempfileSize = targetTempFile.length();

								chunkCompleted = (int) tempfileSize;

								Log.d(getLogTag(), "temp exists");
							}

							targetWriteFile = new RandomAccessFile(
									targetTempFile, "rw");

							bufferedFileStream = new BufferedInputStream(
									remoteContentStream, BUFFER_SIZE);

							// Seek to target. If temp seeks to length otherwise
							// 0
							targetWriteFile.seek(targetWriteFile.length());

							while (-1 != (chunkSize = remoteContentStream
									.read(buffer))) {
								if (TaskState.RESUMED.equals(taskState)) {
									if (isPauseNotified)
										isPauseNotified = false;

									targetWriteFile.write(buffer, 0, chunkSize);

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

								throw new IOException("Download was incomplete");
							} else {
								Log.d(getLogTag(), "Complete download");

								targetTempFile.renameTo(targetLocalFile);
								download.setState(DownloadState.COMPLETED);
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

			return download;
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
	}

	/**
	 * This is a result state for taskstate request. It contains tasks whose
	 * states were changed successfully. It will also return set of ids for task
	 * that was not available
	 * 
	 * @author Milan
	 * 
	 */
	public static class TaskStateResult {
		private HashSet<DownloadTask> successTasks = new HashSet<DownloadTask>();

		private HashSet<Integer> uncreatedTasks = new HashSet<Integer>();

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
		 * @return the uncreatedTasks
		 */
		public HashSet<Integer> getUncreatedTasks() {
			return uncreatedTasks;
		}

		/**
		 * @param uncreatedTasks
		 *            the uncreatedTasks to set
		 */
		public void setUncreatedTasks(HashSet<Integer> uncreatedTasks) {
			this.uncreatedTasks = uncreatedTasks;
		}

	}
}
