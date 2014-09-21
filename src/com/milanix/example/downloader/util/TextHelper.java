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

	/**
	 * This method will parse string as integer. If parsing failed, will return
	 * -1
	 * 
	 * @param string
	 *            to be parsed as int
	 * @return int value if successfull otherwise -1
	 */
	public static int getValueAsInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return -1;
	}

	/**
	 * This method will get given array as string
	 * 
	 * @param numbers
	 *            is an array that extends {@link Number} abstract class.
	 *            Supports {@link Long} {@link Integer} {@link Float}
	 *            {@link Double} {@link Short}
	 * @return string array
	 */
	public static String[] getAsStringArray(Number[] numbers) {
		if (null == numbers)
			return null;

		String[] downloadIdsString = new String[numbers.length];

		for (int i = 0; i < numbers.length; i++) {

			if (numbers instanceof Long[])
				downloadIdsString[i] = Long.toString((Long) numbers[0]);
			else if (numbers instanceof Integer[])
				downloadIdsString[i] = Integer.toString((Integer) numbers[0]);
			else if (numbers instanceof Float[])
				downloadIdsString[i] = Float.toString((Float) numbers[0]);
			else if (numbers instanceof Double[])
				downloadIdsString[i] = Double.toString((Double) numbers[0]);
			else if (numbers instanceof Short[])
				downloadIdsString[i] = Short.toString((Short) numbers[0]);
		}

		return downloadIdsString;
	}
}
