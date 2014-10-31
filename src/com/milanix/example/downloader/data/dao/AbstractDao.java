package com.milanix.example.downloader.data.dao;

import android.content.Context;
import android.database.Cursor;

/**
 * Abstract DAO class
 * 
 * @author Milan
 * 
 * @param <T>
 *            model type of this DAO
 * @param <ID>
 *            id type of the DAO
 */
public abstract class AbstractDao<T, ID> {

	/**
	 * Gets id of this DAO
	 * 
	 * @return id of this DAO
	 */
	public abstract ID getId();

	/**
	 * Gets this DAO from given id
	 * 
	 * @param context
	 *            is the applications context
	 * @param id
	 *            is the id of the object
	 * @return AbstractDao<T>
	 */
	public abstract T retrieve(Context context, ID id);

	/**
	 * Gets this DAO from given cursor
	 * 
	 * 
	 * @param cursor
	 *            cursor to retrieve object from
	 * @return AbstractDao<T>
	 */
	public abstract T retrieve(Cursor cursor);

	/**
	 * Checks if instance and it's field is valid for processing
	 * 
	 * @return true if valid, otherwise false
	 */
	public abstract boolean isValid();
}
