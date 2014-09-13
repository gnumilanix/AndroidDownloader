package com.milanix.example.downloader.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import android.util.Log;

/**
 * This class contains basic file utils
 * 
 * @author Milan
 * 
 */
public class FileUtils {
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
	 * Convenience method to get local path. If default path exist, will create
	 * new one with long time otherwise just the default path
	 * 
	 * @param root
	 *            is the root directory
	 * @param url
	 *            is the download url. This will be used to get name nad the
	 *            extension
	 * @return file
	 */
	public static String getLocalDownloadPath(String root, String url) {
		StringBuilder pathBuilder = new StringBuilder("").append(root)
				.append("/").append(FilenameUtils.getName(url));

		if (new File(pathBuilder.toString()).exists()) {
			StringBuilder pathBuilderWithDate = new StringBuilder("")
					.append(root).append("/")
					.append(FilenameUtils.getBaseName(url)).append("_")
					.append(new Date().getTime()).append(".")
					.append(FilenameUtils.getExtension(url));

			return pathBuilderWithDate.toString();
		} else {
			return pathBuilder.toString();
		}
	}
}
