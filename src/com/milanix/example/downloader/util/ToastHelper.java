package com.milanix.example.downloader.util;

import android.content.Context;
import android.widget.Toast;

/**
 * This method contains helper methods to display toast quickly
 * 
 * @author Milan
 * 
 */
public class ToastHelper {

	/**
	 * This method will display toast using given parameters. Length will be set
	 * to Toast.LENGTH_SHORT
	 * 
	 * @param context
	 *            is the application context
	 * @param message
	 *            is the message to be displayed
	 */
	public static void showToast(Context context, String message) {
		showToast(context, message, Toast.LENGTH_SHORT);
	}

	/**
	 * This method will display toast using given parameters
	 * 
	 * @param context
	 *            is the application context
	 * @param message
	 *            is the message to be displayed
	 * @param length
	 *            is the time length of the toast
	 */
	public static void showToast(Context context, String message, int length) {
		Toast.makeText(context, message, length).show();
	}
}
