package com.milanix.example.downloader.data.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.DownloadsDatabase.DownloadsDBHelper;

/**
 * This content provider contains code for providing download content
 * 
 * TODO replace all extra authorities with odata type URI
 * 
 * @author Milan
 * 
 */
public class DownloadContentProvider extends ContentProvider {

	public static String AUTHORITY = "com.milanix.example.downloader.data.provider.DownloadContentProvider";

	public static final String CONTENT_PATH = "downloads";

	public static final Uri CONTENT_URI_DOWNLOADS = Uri.parse("content://"
			+ AUTHORITY + "/" + CONTENT_PATH);

	private SQLiteDatabase database;

	private static final int GET_DOWNLOADS = 0;

	private static final UriMatcher downloadUriMatcher = getUriMatcher();

	/**
	 * This method will build and return UriMatcher
	 * 
	 * @return UriMatcher for this content provider
	 */
	private static UriMatcher getUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, CONTENT_PATH, GET_DOWNLOADS);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		database = DownloadsDBHelper.getInstance(getContext())
				.getWritableDatabase();

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		switch (downloadUriMatcher.match(uri)) {
		case GET_DOWNLOADS:
			return getDownloads(uri, projection, selection, selectionArgs,
					sortOrder);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (downloadUriMatcher.match(uri)) {
		case GET_DOWNLOADS:
			return CONTENT_URI_DOWNLOADS.toString();
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (downloadUriMatcher.match(uri)) {
		case GET_DOWNLOADS:
			return insertDownload(uri, values);
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (downloadUriMatcher.match(uri)) {
		case GET_DOWNLOADS:
			return deleteDownload(uri, selection, selectionArgs);
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (downloadUriMatcher.match(uri)) {
		case GET_DOWNLOADS:
			return updateDownload(uri, values, selection, selectionArgs);
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * This method will return cursor from given uri with all downloads results
	 * 
	 * @param uri
	 *            is the uri
	 * @return cursor retrieved from the query
	 */
	private Cursor getDownloads(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		return database.query(DownloadsDatabase.TABLE_DOWNLOADS, projection,
				selection, selectionArgs, null, null, sortOrder, null);
	}

	/**
	 * This method will update given download. This will only notify changes if
	 * result is success
	 * 
	 * @param uri
	 *            uri of the content
	 * @param values
	 *            values to update
	 * @return uri of the newly added content
	 */
	private Uri insertDownload(Uri uri, ContentValues values) {
		long rowId = database.insert(DownloadsDatabase.TABLE_DOWNLOADS, null,
				values);

		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(uri, null);

			return new Uri.Builder().path(CONTENT_URI_DOWNLOADS.toString())
					.appendPath(Long.toString(rowId)).build();
		}

		return null;
	}

	/**
	 * This method will delete given download. This will only notify changes if
	 * result is success
	 * 
	 * @param uri
	 *            uri of the content
	 * @param selection
	 *            selection to be made
	 * @param selectionArgs
	 *            arguments to be added in the selection
	 * @return number of rows affected
	 */
	private int deleteDownload(Uri uri, String selection, String[] selectionArgs) {
		int rowsAffected = database.delete(DownloadsDatabase.TABLE_DOWNLOADS,
				selection, selectionArgs);

		if (rowsAffected > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		return rowsAffected;
	}

	/**
	 * This method will update given download. This will only notify changes if
	 * result is success
	 * 
	 * @param uri
	 *            uri of the content
	 * @param values
	 *            values to update
	 * @param selection
	 *            selection to be made
	 * @param selectionArgs
	 *            arguments to be added in the selection
	 * @return number of rows affected
	 */
	private int updateDownload(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int rowsAffected = database.update(DownloadsDatabase.TABLE_DOWNLOADS,
				values, selection, selectionArgs);

		if (rowsAffected > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		return rowsAffected;
	}

}
