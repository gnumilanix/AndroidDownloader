package com.milanix.example.downloader.data.dao;

import android.content.Context;
import android.database.Cursor;

import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;

/**
 * This is an {@link Download} dao
 * 
 * @author Milan
 * 
 */
public class Download extends AbstractDao<Download, Integer> {
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
				"completed"), FAILED("failed"), CANCELLED("cancelled"), PAUSED(
				"paused"), UNKNOWN("unknown");

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
			else if (ADDED_NOTAUTHORIZED.toString().equals(value))
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
				"Error while downloading file."), FILE_INCOMPLETE(
				"Incomplete file."), UNKNOWN_ERROR("Unknown error occoured.");

		private final String name;

		private FailedReason(String s) {
			name = s;
		}

		@Override
		public String toString() {
			return name;
		}

		public static FailedReason getEnum(String value) {
			if (STORAGE_NOTWRITABLE.toString().equals(value))
				return STORAGE_NOTWRITABLE;
			else if (STORAGE_NOTAVAILABLE.toString().equals(value))
				return STORAGE_NOTAVAILABLE;
			else if (NETWORK_NOTAVAILABLE.toString().equals(value))
				return NETWORK_NOTAVAILABLE;
			else if (IO_ERROR.toString().equals(value))
				return IO_ERROR;
			else if (NETWORK_ERROR.toString().equals(value))
				return NETWORK_ERROR;
			else if (FILE_INCOMPLETE.toString().equals(value))
				return FILE_INCOMPLETE;
			else
				return UNKNOWN_ERROR;
		}
	}

	private Integer id;
	private String url;
	private String path;
	private String name;
	private Integer type;
	private String size;
	private Long dateAdded;
	private Long dateCompleted;
	private DownloadState state;
	private FailedReason failReason;

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
			String size, Long dateAdded, DownloadState state,
			FailedReason failReason) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.type = type;
		this.size = size;
		this.dateAdded = dateAdded;
		this.state = state;
		this.failReason = failReason;
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

	/**
	 * @return the failReason
	 */
	public FailedReason getFailReason() {
		return failReason;
	}

	/**
	 * @param failReason
	 *            the failReason to set
	 */
	public void setFailReason(FailedReason failReason) {
		this.failReason = failReason;
	}

	/**
	 * @return the dateAdded
	 */
	public Long getDateAdded() {
		return dateAdded;
	}

	/**
	 * @param dateAdded
	 *            the dateAdded to set
	 */
	public void setDateAdded(Long dateAdded) {
		this.dateAdded = dateAdded;
	}

	/**
	 * @return the dateCompleted
	 */
	public Long getDateCompleted() {
		return dateCompleted;
	}

	/**
	 * @param dateCompleted
	 *            the dateCompleted to set
	 */
	public void setDateCompleted(Long dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	@Override
	public Download retrieve(Context context, Integer id) {
		Cursor cursor = context.getContentResolver().query(
				DownloadContentProvider.CONTENT_URI_DOWNLOADS, null,
				QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID, id, true),
				null, null);

		return retrieve(cursor);
	}

	@Override
	public Download retrieve(Cursor cursor) {
		if (cursor.getCount() > 0) {
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

				if (-1 != cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_DATE_ADDED))
					setDateAdded(cursor
							.getLong(cursor
									.getColumnIndex(DownloadsDatabase.COLUMN_DATE_ADDED)));

				if (-1 != cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_DATE_COMPLETED))
					setDateCompleted(cursor
							.getLong(cursor
									.getColumnIndex(DownloadsDatabase.COLUMN_DATE_COMPLETED)));

				if (-1 != cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_FAIL_REASON))
					setFailReason(FailedReason
							.getEnum(cursor.getString(cursor
									.getColumnIndex(DownloadsDatabase.COLUMN_FAIL_REASON))));

				if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_PATH))
					setPath(cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_PATH)));

			}
		} else {
			return null;
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
		 * Called when download is failed. Includes DownloadState failed and
		 * FailedReason will reason of failure
		 * 
		 * @param download
		 *            object
		 */
		public void onDownloadFailed(Download download);

		/**
		 * Called when download progress in updated
		 * 
		 * @param download
		 *            object
		 * 
		 * @param int is a progress
		 */
		public void onDownloadProgress(Download download, Integer progress);
	}

}
