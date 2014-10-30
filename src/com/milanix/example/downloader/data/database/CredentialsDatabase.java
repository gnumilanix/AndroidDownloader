package com.milanix.example.downloader.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class contains logic for storing/retrieving and querying the credentials
 * database
 * 
 * @author Milan
 * 
 */
public class CredentialsDatabase {
	private static final int databaseVersion = 1;

	public static final String TABLE_CREDENTIALS = "credentials";

	// Id of the credential
	public static final String COLUMN_ID = "_id";

	// Host the credential is for
	public static final String COLUMN_HOST = "host";

	// Protocol for this host
	public static final String COLUMN_PROTOCOL = "protocol";

	// Username for this host
	public static final String COLUMN_USERNAME = "username";

	// Password for this host
	public static final String COLUMN_PASSWORD = "password";

	private static final String DATABASE_NAME = "credential_db";

	public static class CredentialsDBHelper extends SQLiteOpenHelper {
		private static CredentialsDBHelper databaseOpenHelper = null;

		private static final String DATABASE_CREATE = "CREATE TABLE "
				+ TABLE_CREDENTIALS + "(" + COLUMN_ID
				+ " integer primary key autoincrement," + COLUMN_HOST
				+ " varchar(1024)," + COLUMN_PROTOCOL + " varchar(2048),"
				+ COLUMN_USERNAME + " varchar(2048)," + COLUMN_PASSWORD
				+ " varchar(2048));";

		public CredentialsDBHelper(Context context) {
			super(context, DATABASE_NAME, null, databaseVersion);
		}

		/**
		 * This method return singleton instance for the database
		 * 
		 * @param context
		 * @return
		 */
		public synchronized static CredentialsDBHelper getInstance(
				Context context) {
			if (databaseOpenHelper == null)
				databaseOpenHelper = new CredentialsDBHelper(
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
			database.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);

			onCreate(database);
		}
	}
}
