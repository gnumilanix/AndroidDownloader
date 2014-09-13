package com.milanix.example.downloader.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadListener;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.util.FileUtils;
import com.milanix.example.downloader.util.NetworkUtils;
import com.milanix.example.downloader.util.TextHelper;

/**
 * This is a download service
 * 
 * @author Milan
 * 
 */
public class DownloadService extends Service {
	// Map to support miltple callback with same ids but different caller
	private HashMap<Integer, HashSet<DownloadListener>> attachedCallbacks = new HashMap<Integer, HashSet<DownloadListener>>();

	private final IBinder mBinder = new DownloadBinder();

	public class DownloadBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
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
	 * This method will download the given download file
	 * 
	 * @param download
	 *            is a download file
	 * @return asynctask with given file handler
	 */
	public AsyncTask<String, Integer, Download> downloadFile(Download download) {

		if (null == download)
			return null;

		if (TextHelper.isStringEmpty(download.getUrl()))
			notifyCallbacksFailed(download);

		return new DownloadTask(download).executeOnExecutor(
				AsyncTask.THREAD_POOL_EXECUTOR, download.getUrl());
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
	private void notifyCallbacksFailed(final Download download) {
		if (null != download && null != download.getId()) {
			if (attachedCallbacks.containsKey(download.getId())) {
				final HashSet<DownloadListener> callbacks = attachedCallbacks
						.get(download.getId());

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
	private void notifyCallbacksProgress(final Download download,
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
	 * This is a task to download the file
	 * 
	 * @author Milan
	 * 
	 */
	public class DownloadTask extends AsyncTask<String, Integer, Download> {

		private Download download;

		private static final int BUFFER_SIZE = 1024;
		private File targetFile;

		public DownloadTask(Download download) {
			this.download = download;

			if (null != this.download) {
				this.targetFile = new File(download.getPath());
			}
		}

		@Override
		protected Download doInBackground(String... arg) {
			InputStream remoteContentStream = null;
			OutputStream localFileStream = null;

			try {
				HttpClient downloadClient = NetworkUtils.getHttpClient();

				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setSoTimeout(params, 60000);

				HttpGet request = new HttpGet(arg[0]);
				request.setParams(params);

				HttpResponse response = downloadClient.execute(request);

				remoteContentStream = response.getEntity().getContent();

				long fileSize = response.getEntity().getContentLength();

				localFileStream = new FileOutputStream(targetFile);

				byte[] buffer = new byte[BUFFER_SIZE];
				int chunkSize = 0;
				int chunkCompleted = 0;

				while (-1 != (chunkSize = remoteContentStream.read(buffer))) {
					localFileStream.write(buffer, 0, chunkSize);

					chunkCompleted += chunkSize;

					publishProgress((int) ((double) chunkCompleted
							/ (double) fileSize * 100.0));
				}

				download.setState(DownloadState.COMPLETED);

			} catch (Exception e) {
				e.printStackTrace();

				download.setState(DownloadState.FAILED);
			} finally {
				FileUtils.close(remoteContentStream);
				FileUtils.close(localFileStream);
			}

			return download;
		}

		@Override
		protected void onCancelled(Download result) {
			notifyCallbacksCancelled(result);

			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Download result) {
			notifyCallbacksCompleted(result);

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			notifyCallbacksStarted(download);

			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			notifyCallbacksProgress(download, values[0]);

			super.onProgressUpdate(values);
		}
	}
}
