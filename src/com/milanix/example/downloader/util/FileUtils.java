package com.milanix.example.downloader.util;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * This class contains file utils
 * 
 * @author Milan
 * 
 */
public class FileUtils {
	private static final String TAG = FileUtils.class.getSimpleName();

	// Size constants
	private final static long SIZE_KB = 1024L;
	private final static long SIZE_MB = SIZE_KB * SIZE_KB;
	private final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

	// Treshold in percentage
	public static final long STORAGE_THRESHOLD = 10;

	/**
	 * Enum to define storage types
	 * 
	 * @author Milan
	 * 
	 */
	public static enum StorageSize {
		KB, MB, GB
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

	/**
	 * This method will check if the external storage is writitable. This method
	 * checks if the storage is mounted
	 * 
	 * @return true is RW otherwise false
	 */
	public static boolean isStorageWritable() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}

		return false;
	}

	/**
	 * This method will return available storage in the external storage
	 * 
	 * @return
	 */
	public static long getAvailableStorageInBytes() {
		try {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				return (long) stat.getAvailableBlocksLong()
						* (long) stat.getBlockSizeLong();
			} else {
				@SuppressWarnings("deprecation")
				Long availableSpace = (long) stat.getAvailableBlocks()
						* (long) stat.getBlockSize();

				return availableSpace;
			}

		} catch (RuntimeException ex) {
			Log.e(TAG, "Error while retrieving stats.", ex);

			return -1L;
		}
	}

	/**
	 * This method will return available storage in given size. If not the type
	 * it will return in bytes
	 * 
	 * @param size
	 *            is the StorageSize type
	 * @return size
	 */
	public static long getAvailableStorage(StorageSize size) {
		long availableStorageInBytes = getAvailableStorageInBytes();

		switch (size) {
		case KB:
			return availableStorageInBytes / SIZE_KB;
		case MB:
			return availableStorageInBytes / SIZE_MB;
		case GB:
			return availableStorageInBytes / SIZE_GB;
		default:
			return availableStorageInBytes;
		}
	}

	/**
	 * This method will return is storage is available. This will internally
	 * call isStorageSpaceAvailable(FileUtils.STORAGE_TRESHOLD,size);
	 * 
	 * @param sizeToWrite
	 *            requested
	 * @return true is available otherwise false
	 */
	public static boolean isStorageSpaceAvailable(long sizeToWrite) {
		return isStorageSpaceAvailable(STORAGE_THRESHOLD, sizeToWrite);
	}

	/**
	 * This method will return is storage is available
	 * 
	 * @treshold is a treshold in percentage of allowed writable. This will
	 *           always ensure that the given threshold is available
	 * @param sizeToWrite
	 *            requested
	 * @return true is available otherwise false
	 */
	public static boolean isStorageSpaceAvailable(long treshold,
			long sizeToWrite) {
		if (getAvailableStorageInBytes() < (sizeToWrite + ((treshold / 100) * sizeToWrite)))
			return false;
		else
			return true;
	}

}
