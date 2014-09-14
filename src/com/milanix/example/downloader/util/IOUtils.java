package com.milanix.example.downloader.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.util.Log;

/**
 * This class contains io utils
 * 
 * @author Milan
 * 
 */
public class IOUtils {
	private static final String TAG = FileUtils.class.getSimpleName();

	/**
	 * THis method will close the given input stream
	 * 
	 * @param stream
	 *            to close
	 */
	public static void close(InputStream stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Log.e(TAG, "Error while closing input stream.", e);
			}
		}
	}

	/**
	 * THis method will close the given output stream
	 * 
	 * @param stream
	 *            to close
	 */
	public static void close(OutputStream stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Log.e(TAG, "Error while closing input stream.", e);
			}
		}
	}

	/**
	 * THis method will close the given RandomAccessFile file
	 * 
	 * @param file
	 *            to close
	 */
	public static void close(RandomAccessFile file) {
		if (file != null) {
			try {
				file.close();
			} catch (IOException e) {
				Log.e(TAG, "Error while closing input stream.", e);
			}
		}
	}
}
