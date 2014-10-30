package com.milanix.example.downloader.util;

/**
 * This class contains utility to parse string to numbers
 * 
 * @author milan
 * 
 */
public class NumberParser {

	/**
	 * This method will parse string to integer.
	 * 
	 * @param string
	 *            to be parsed
	 * @return parse value or 0 if there's an exception
	 */
	public static int getInteger(String string) {
		return getIntegerObject(string) == null ? 0 : getIntegerObject(string);
	}

	/**
	 * This method will parse string to integer.
	 * 
	 * @param string
	 *            to be parsed
	 * @return parse value or null if there's an exception
	 */
	public static Integer getIntegerObject(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * This method will parse string to long.
	 * 
	 * @param string
	 *            to be parsed
	 * @return parse value or 0 if there's an exception
	 */
	public static long getLong(String string) {
		return getLongObject(string) == null ? 0 : getLongObject(string);
	}

	/**
	 * This method will parse string to long.
	 * 
	 * @param string
	 *            to be parsed
	 * @return parse value or null if there's an exception
	 */
	public static Long getLongObject(String string) {
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
