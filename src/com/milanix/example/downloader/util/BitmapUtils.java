package com.milanix.example.downloader.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This class contains method related to work with bitmap
 * 
 * @author Milan
 * 
 */
public class BitmapUtils {
	/**
	 * This method calculates image sample size
	 * 
	 * @param options
	 *            bitmap options
	 * @param reqWidth
	 *            requestedWidth
	 * @param reqHeight
	 *            requestedHeight
	 * @return sample size to calculate bitmap in
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight == -1 ? options.outHeight
				: options.outHeight;
		final int width = options.outWidth == -1 ? options.outWidth
				: options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			/*
			 * Calculate the largest inSampleSize value that is a power of 2 and
			 * keeps both height and width larger than the requested height and
			 * width.
			 */
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	/**
	 * Decodes sampled bitmap from resources. Returns null on failure
	 * 
	 * @param res
	 *            android resources
	 * @param resId
	 *            res id of the bitmap
	 * @param reqWidth
	 *            requested width
	 * @param reqHeight
	 *            requested height
	 * @return decoded bitmap
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Decodes sampled bitmap from resources. Returns null on failure
	 * 
	 * @param res
	 *            android resources
	 * @param path
	 *            path of the file containing bitmap
	 * @param reqWidth
	 *            requested width
	 * @param reqHeight
	 *            requested height
	 * @return decoded bitmap
	 */

	public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth,
			int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}
}
