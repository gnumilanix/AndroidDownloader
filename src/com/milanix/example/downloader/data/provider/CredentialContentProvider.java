package com.milanix.example.downloader.data.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.milanix.example.downloader.data.database.CredentialsDatabase;
import com.milanix.example.downloader.data.database.CredentialsDatabase.CredentialsDBHelper;

/**
 * This content provider contains code for providing credential content
 * 
 * TODO replace all extra authorities with odata type URI
 * 
 * @author Milan
 * 
 */
public class CredentialContentProvider extends ContentProvider {

	public static String AUTHORITY = "com.milanix.example.downloader.data.provider.CredentialContentProvider";

	public static final String CONTENT_PATH = "credentials";

	public static final Uri CONTENT_URI_CREDENTIALS = Uri.parse("content://"
			+ AUTHORITY + "/" + CONTENT_PATH);

	private SQLiteDatabase database;

	private static final int GET_CREDENTIALS = 0;

	private static final UriMatcher credentialUriMatcher = getUriMatcher();

	/**
	 * This method will build and return UriMatcher
	 * 
	 * @return UriMatcher for this content provider
	 */
	private static UriMatcher getUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, CONTENT_PATH, GET_CREDENTIALS);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		database = CredentialsDBHelper.getInstance(getContext())
				.getWritableDatabase();

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		switch (credentialUriMatcher.match(uri)) {
		case GET_CREDENTIALS:
			return getCredentials(uri, projection, selection, selectionArgs,
					sortOrder);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (credentialUriMatcher.match(uri)) {
		case GET_CREDENTIALS:
			return CONTENT_URI_CREDENTIALS.toString();
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (credentialUriMatcher.match(uri)) {
		case GET_CREDENTIALS:
			return insertCredential(uri, values);
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (credentialUriMatcher.match(uri)) {
		case GET_CREDENTIALS:
			return deleteCredential(uri, selection, selectionArgs);
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (credentialUriMatcher.match(uri)) {
		case GET_CREDENTIALS:
			return updateCredential(uri, values, selection, selectionArgs);
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * This method will return cursor from given uri with all credentials
	 * results
	 * 
	 * @param uri
	 *            is the uri
	 * @return cursor retrieved from the query
	 */
	private Cursor getCredentials(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {

		return database.query(CredentialsDatabase.TABLE_CREDENTIALS,
				projection, selection, selectionArgs, null, null, sortOrder,
				null);
	}

	/**
	 * This method will update given crendential. This will only notify changes
	 * if result is success
	 * 
	 * @param uri
	 *            uri of the content
	 * @param values
	 *            values to update
	 * @return uri of the newly added content
	 */
	private Uri insertCredential(Uri uri, ContentValues values) {
		long rowId = database.insert(CredentialsDatabase.TABLE_CREDENTIALS,
				null, values);

		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(uri, null);

			return new Uri.Builder().path(CONTENT_URI_CREDENTIALS.toString())
					.appendPath(Long.toString(rowId)).build();
		}

		return null;
	}

	/**
	 * This method will delete given credential. This will only notify changes
	 * if result is success
	 * 
	 * @param uri
	 *            uri of the content
	 * @param selection
	 *            selection to be made
	 * @param selectionArgs
	 *            arguments to be added in the selection
	 * @return number of rows affected
	 */
	private int deleteCredential(Uri uri, String selection,
			String[] selectionArgs) {
		int rowsAffected = database
				.delete(CredentialsDatabase.TABLE_CREDENTIALS, selection,
						selectionArgs);

		if (rowsAffected > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		return rowsAffected;
	}

	/**
	 * This method will update given credential. This will only notify changes
	 * if result is success
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
	private int updateCredential(Uri uri, ContentValues values,
			String selection, String[] selectionArgs) {
		int rowsAffected = database.update(
				CredentialsDatabase.TABLE_CREDENTIALS, values, selection,
				selectionArgs);

		if (rowsAffected > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		return rowsAffected;
	}

}
