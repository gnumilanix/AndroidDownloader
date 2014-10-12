package com.milanix.example.downloader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.milanix.example.downloader.pref.PreferenceHelper;

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

	/**
	 * This method will get date from given date string and format
	 * 
	 * @param context
	 *            is the base application context
	 * @param format
	 *            is the expected format
	 * @param dateString
	 *            is the date
	 * @return date if parsable, otherwise null
	 */
	public static Date getAsDate(Context context, String format,
			String dateString) {
		if (null == context || TextUtils.isEmpty(format)
				|| TextUtils.isEmpty(dateString))
			return null;

		try {
			return new SimpleDateFormat(format).parse(PreferenceHelper
					.getBasicScheduleStart(context));
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * This method will get calendar from given date
	 * 
	 * @param date
	 *            is the date object
	 * @return calendar if date is valid, otherwise null
	 */
	public static Calendar getDateAsCalendar(Date date) {
		if (null == date)
			return null;

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		return calendar;
	}
}
