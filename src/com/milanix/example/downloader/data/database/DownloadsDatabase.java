package com.milanix.example.downloader.data.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadState;

/**
 * This class contains logic for storing/retrieving and querying the database
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

	// Date the file was downloaded
	public static final String COLUMN_DATE = "date";

	// State of the download
	public static final String COLUMN_STATE = "state";

	private static final String DATABASE_NAME = "download_db";

	public static class DownloadsDBHelper extends SQLiteOpenHelper {
		private static DownloadsDBHelper databaseOpenHelper = null;

		private static final String DATABASE_CREATE = "CREATE TABLE "
				+ TABLE_DOWNLOADS + "(" + COLUMN_ID
				+ " integer primary key autoincrement," + COLUMN_URL
				+ " varchar(1024)," + COLUMN_PATH + " varchar(1024),"
				+ COLUMN_NAME + " varchar(1024)," + COLUMN_TYPE + " integer,"
				+ COLUMN_DATE + " long," + COLUMN_STATE + " varchar(16));";

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

	/**
	 * This method will return download object from the cursor
	 * 
	 * @param cursor
	 *            to retrieve data from
	 * @return download object if successfull otherwise null
	 */
	public static Download getDownloadFromCursor(Cursor cursor) {
		if (null == cursor)
			return null;

		return new Download(cursor.getInt(cursor
				.getColumnIndex(DownloadsDatabase.COLUMN_ID)),
				cursor.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_URL)),
				cursor.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_NAME)),
				cursor.getInt(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_TYPE)),
				cursor.getLong(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_DATE)),
				DownloadState.getEnum(cursor.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_STATE))));
	}

}