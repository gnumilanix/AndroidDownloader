package com.milanix.example.downloader.data.dao;

import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;

import android.content.Context;
import android.database.Cursor;

/**
 * This is an {@link Download} dao
 * 
 * @author Milan
 * 
 */
public class Download extends AbstractDao<Download> {
	/**
	 * Enum for {@link Download} states
	 * 
	 * {@link #ADDED_AUTHORIZED} states that the download file passed all the
	 * checks
	 * 
	 * {@link #ADDED_NOTAUTHORIZED} states that the download file has not passed
	 * all the checks
	 * 
	 * {@link #DOWNLOADING} states that the download is in progress
	 * 
	 * {@link #COMPLETED} states that the download has been completed
	 * 
	 * {@link #FAILED} states that the download failed
	 * 
	 * {@link #CANCELLED} states that the download has been cancelled
	 * 
	 * {@link #UNKNOWN} states that the download state is unknown
	 * 
	 * @author Milan
	 * 
	 */
	public static enum DownloadState {
		ADDED_AUTHORIZED("added_authorized"), ADDED_NOTAUTHORIZED(
				"added_notauthorized"), DOWNLOADING("downloading"), COMPLETED(
				"completed"), FAILED("failed"), CANCELLED("cancelled"), UNKNOWN(
				"unknown");

		private final String name;

		private DownloadState(String s) {
			name = s;
		}

		@Override
		public String toString() {
			return name;
		}

		public static DownloadState getEnum(String value) {
			if (ADDED_AUTHORIZED.toString().equals(value))
				return ADDED_AUTHORIZED;
			if (ADDED_NOTAUTHORIZED.toString().equals(value))
				return ADDED_NOTAUTHORIZED;
			else if (DOWNLOADING.toString().equals(value))
				return DOWNLOADING;
			else if (COMPLETED.toString().equals(value))
				return COMPLETED;
			else if (FAILED.toString().equals(value))
				return FAILED;
			else if (CANCELLED.toString().equals(value))
				return CANCELLED;
			else
				return UNKNOWN;
		}
	}

	/**
	 * Enum for download task state
	 * 
	 * {@link #RESUMED} states that the download state is resumed
	 * 
	 * {@link #PAUSED} states that the download state is paused
	 * 
	 * @author Milan
	 * 
	 */
	public static enum TaskState {
		RESUMED, PAUSED
	}

	/**
	 * Enum for {@link DownloadState} failed reason
	 * 
	 * @author Milan
	 * 
	 */
	public static enum FailedReason {
		STORAGE_NOTWRITABLE("External storage is not writable."), STORAGE_NOTAVAILABLE(
				"External storage space is not available."), NETWORK_NOTAVAILABLE(
				"Network is not available."), IO_ERROR(
				"Error while reading/writing file."), NETWORK_ERROR(
				"Error while downloading file."), UNKNOWN_ERROR(
				"Unknown error occoured.");

		private final String name;

		private FailedReason(String s) {
			name = s;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Integer id;
	private String url;
	private String path;
	private String name;
	private Integer type;
	private String size;
	private Long date;
	private DownloadState state;

	/**
	 * This must be defined to support retrieve
	 */
	public Download() {

	};

	/**
	 * This is the default construtor of this class
	 * 
	 * @param id
	 * @param url
	 * @param name
	 * @param type
	 * @param date
	 * @param state
	 */
	public Download(Integer id, String url, String name, Integer type,
			String size, Long date, DownloadState state) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.type = type;
		this.size = size;
		this.date = date;
		this.state = state;
	}

	/**
	 * @return the id
	 */
	@Override
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * @return the size
	 */
	public String getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * @return the date
	 */
	public Long getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Long date) {
		this.date = date;
	}

	/**
	 * @return the state
	 */
	public DownloadState getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(DownloadState state) {
		this.state = state;
	}

	@Override
	public Download retrieve(Context context, int id) {
		Cursor cursor = context.getContentResolver().query(
				DownloadContentProvider.CONTENT_URI_DOWNLOADS, null,
				QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID, id, true),
				null, null);

		if (cursor.getCount() > 0)
			if (cursor.moveToFirst()) {
				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_ID))
					setId(cursor.getInt(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_ID)));

				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_URL))
					setUrl(cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_URL)));

				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_NAME))
					setName(cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_NAME)));

				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_TYPE))
					setType(cursor.getInt(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_TYPE)));

				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_SIZE))
					setPath(cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_SIZE)));

				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_DATE))
					setDate(cursor.getLong(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_DATE)));

				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_STATE))
					setState(DownloadState.getEnum(cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_STATE))));

				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_PATH))
					setPath(cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_PATH)));

			}

		return this;
	}

	@Override
	public boolean isValid() {
		if (null == this || null == this.getId() || null == this.getUrl()
				|| null == this.getPath())
			return false;

		return true;
	}

	/**
	 * This is an interface that should be used to monitor download progress
	 * which is called based on the asynctask implementation
	 * 
	 * @author Milan
	 * 
	 */
	public static interface DownloadListener {
		/**
		 * Called when download is started
		 * 
		 * @param download
		 *            object
		 */
		public void onDownloadStarted(Download download);

		/**
		 * Called when download is cancelled
		 * 
		 * @param download
		 *            object
		 */
		public void onDownloadCancelled(Download download);

		/**
		 * Called when download is completed
		 * 
		 * @param download
		 *            object
		 */
		public void onDownloadCompleted(Download download);

		/**
		 * Called when download is failed
		 * 
		 * @param download
		 *            object
		 */
		public void onDownloadFailed(Download download, FailedReason reason);

		/**
		 * Called when download progress in updated
		 * 
		 * @param download
		 *            object
		 * 
		 * @param int is a progress
		 */
		public void onDownloadProgress(TaskState taskState, Download download,
				Integer progress);
	}

}
