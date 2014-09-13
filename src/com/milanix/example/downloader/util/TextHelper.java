package com.milanix.example.downloader.util;

import android.text.format.DateUtils;

/**
 * This method contains validator
 * 
 * @author Milan
 * 
 */
public class TextHelper {
	/**
	 * This method will check if the string is empty or null
	 * 
	 * @param string
	 *            to validate
	 * @return ture if empty otherwise false
	 */
	public static boolean isStringEmpty(String string) {
		if (null == string || "".equals(string))
			return true;
		else
			return false;
	}

	/**
	 * This method will get relative date string
	 * 
	 * @param date
	 *            is the date
	 * @param ifNotValid
	 *            is a string to return if invalid
	 * @return
	 */
	public static String getRelativeDateString(Long date, String ifNotValid) {

		if (null != date)
			return DateUtils.getRelativeTimeSpanString(date).toString();

		return ifNotValid;
	}
}
