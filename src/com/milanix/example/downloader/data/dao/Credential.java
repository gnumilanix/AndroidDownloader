package com.milanix.example.downloader.data.dao;

import android.content.Context;
import android.database.Cursor;

import com.milanix.example.downloader.data.database.CredentialsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.CredentialContentProvider;

/**
 * This is an {@link Credential} dao
 * 
 * @author Milan
 * 
 */
public class Credential extends AbstractDao<Credential, Integer> {

	public static final String USERNAME_ANONOYMOUS = "anonymous";
	public static final String PASSWORD_ANONOYMOUS = "";

	private Integer id;
	private String host;
	private String protocol;
	private String username;
	private String password;

	/**
	 * This must be defined to support retrieve
	 */
	public Credential() {
	}

	/**
	 * This is the default construtor of this class
	 * 
	 * @param id
	 * @param host
	 * @param protocol
	 * @param username
	 * @param password
	 */
	public Credential(Integer id, String host, String protocol,
			String username, String password) {
		this.id = id;
		this.host = host;
		this.protocol = protocol;
		this.username = username;
		this.password = password;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public Credential retrieve(Context context, Integer id) {
		Cursor cursor = context.getContentResolver().query(
				CredentialContentProvider.CONTENT_URI_CREDENTIALS, null,
				QueryHelper.getWhere(CredentialsDatabase.COLUMN_ID, id, true),
				null, null);

		return retrieve(cursor);
	}

	@Override
	public Credential retrieve(Cursor cursor) {
		if (cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				if (-1 != cursor.getColumnIndex(CredentialsDatabase.COLUMN_ID))
					setId(cursor.getInt(cursor
							.getColumnIndex(CredentialsDatabase.COLUMN_ID)));

				if (-1 != cursor
						.getColumnIndex(CredentialsDatabase.COLUMN_HOST))
					setHost(cursor.getString(cursor
							.getColumnIndex(CredentialsDatabase.COLUMN_HOST)));

				if (-1 != cursor
						.getColumnIndex(CredentialsDatabase.COLUMN_PROTOCOL))
					setProtocol(cursor
							.getString(cursor
									.getColumnIndex(CredentialsDatabase.COLUMN_PROTOCOL)));

				if (-1 != cursor
						.getColumnIndex(CredentialsDatabase.COLUMN_USERNAME))
					setUsername(cursor
							.getString(cursor
									.getColumnIndex(CredentialsDatabase.COLUMN_USERNAME)));

				if (-1 != cursor
						.getColumnIndex(CredentialsDatabase.COLUMN_PASSWORD))
					setPassword(cursor
							.getString(cursor
									.getColumnIndex(CredentialsDatabase.COLUMN_PASSWORD)));
			}
		} else {
			setUsername(Credential.USERNAME_ANONOYMOUS);
			setPassword(Credential.PASSWORD_ANONOYMOUS);

			return null;
		}

		return this;
	}

	@Override
	public boolean isValid() {
		if (null == this || null == this.getId() || null == this.getHost()
				|| null == this.getProtocol())
			return false;

		return true;
	}

}
