package com.milanix.example.downloader.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class contains logic for storing/retrieving and querying the downloads
 * database
 * 
 * @author Milan
 * 
 */
public class DownloadsDatabase {
	private static final int databaseVersion = 1;

	public static final String TABLE_DOWNLOADS = "downloads";

	// Id of the download
	public static final String COLUMN_ID = "_id";

	// URL to download from
	public static final String COLUMN_URL = "url";

	// Local file path of download
	public static final String COLUMN_PATH = "path";

	// Name of the file
	public static final String COLUMN_NAME = "name";

	// Type of the file
	public static final String COLUMN_TYPE = "type";

	// Size of the file
	public static final String COLUMN_SIZE = "size";

	// Date the file was downloaded
	public static final String COLUMN_DATE_ADDED = "date_added";

	// Date the file was downloaded
	public static final String COLUMN_DATE_COMPLETED = "date_completed";

	// State of the download
	public static final String COLUMN_STATE = "state";

	// Reason for failure
	public static final String COLUMN_FAIL_REASON = "fail_reason";

	private static final String DATABASE_NAME = "download_db";

	public static class DownloadsDBHelper extends SQLiteOpenHelper {
		private static DownloadsDBHelper databaseOpenHelper = null;

		private static final String DATABASE_CREATE = "CREATE TABLE "
				+ TABLE_DOWNLOADS + "(" + COLUMN_ID
				+ " integer primary key autoincrement," + COLUMN_URL
				+ " varchar(1024)," + COLUMN_PATH + " varchar(2048),"
				+ COLUMN_NAME + " varchar(512)," + COLUMN_TYPE
				+ " varchar(32)," + COLUMN_SIZE + " varchar(32),"
				+ COLUMN_DATE_ADDED + " long," + COLUMN_DATE_COMPLETED
				+ " long," + COLUMN_STATE + " varchar(16),"
				+ COLUMN_FAIL_REASON + " varchar(128));";

		public DownloadsDBHelper(Context context) {
			super(context, DATABASE_NAME, null, databaseVersion);
		}

		/**
		 * This method return singleton instance for the database
		 * 
		 * @param context
		 * @return
		 */
		public synchronized static DownloadsDBHelper getInstance(Context context) {
			if (databaseOpenHelper == null)
				databaseOpenHelper = new DownloadsDBHelper(
						context.getApplicationContext());

			return databaseOpenHelper;
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion,
				int newVersion) {
			database.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOADS);

			onCreate(database);
		}
	}

}