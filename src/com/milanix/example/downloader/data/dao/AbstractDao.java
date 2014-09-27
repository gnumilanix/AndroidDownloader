package com.milanix.example.downloader.data.dao;

import android.content.Context;

/**
 * Abstract dao class
 * 
 * @author Milan
 * 
 */
public abstract class AbstractDao<T> {

	/**
	 * Gets id of this dao
	 * 
	 * @return id of this dao
	 */
	public abstract Integer getId();

	/**
	 * Gets this dao from given id
	 * 
	 * @param context
	 *            is the applications context
	 * @param id
	 *            is the id of the object
	 * @return AbstractDao<T>
	 */
	public abstract T retrieve(Context context, int id);

	/**
	 * Checks if instance and it's field is valid for processing
	 * 
	 * @return true if valid, otherwise false
	 */
	public abstract boolean isValid();
}
