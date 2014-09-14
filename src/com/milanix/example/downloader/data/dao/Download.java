package com.milanix.example.downloader.data.dao;

/**
 * This is an {@link Download} dao
 * 
 * @author Milan
 * 
 */
public class Download {
	/**
	 * Enum for {@link Download} states
	 * 
	 * @author Milan
	 * 
	 */
	public static enum DownloadState {
		ADDED("added"), DOWNLOADING("downloading"), COMPLETED("completed"), FAILED(
				"failed"), CANCELLED("cancelled"), UNKNOWN("unknown");

		private final String name;

		private DownloadState(String s) {
			name = s;
		}

		@Override
		public String toString() {
			return name;
		}

		public static DownloadState getEnum(String value) {
			if (ADDED.toString().equals(value))
				return ADDED;
			else if (DOWNLOADING.toString().equals(value))
				return DOWNLOADING;
			else if (COMPLETED.toString().equals(value))
				return COMPLETED;
			else if (FAILED.toString().equals(value))
				return FAILED;
			else
				return UNKNOWN;
		}
	}

	/**
	 * Enum for download task state
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
	private Long date;
	private DownloadState state;

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
			Long date, DownloadState state) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.type = type;
		this.date = date;
		this.state = state;
	}

	/**
	 * @return the id
	 */
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
