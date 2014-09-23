package com.milanix.example.downloader.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * This class will help to convert dimensionPixel to pixel
 * 
 * @author Milan
 * 
 */
public class DipToPixelHelper {
	private static final String TAG = DipToPixelHelper.class.getSimpleName();

	private static final String ERROR_CONTEXTNULL = "You cannot use a null context.";
	private static final String ERROR_DIMENSIONPIXELNULL = "You cannot use a null dimension.";

	/**
	 * This method converts dp to pixel
	 * 
	 * @param context
	 *            is the application context
	 * @param dimensionPixel
	 *            is the dp to be converted to px
	 * @return respective pixels or null if params are null
	 * 
	 * @throws IllegalArgumentException
	 *             if context or dimensionPixel is passed null
	 */
	public static Integer getPixelFromDip(Context context,
			Integer dimensionPixel) {
		if (null == context)
			throw new IllegalArgumentException(TAG + ERROR_CONTEXTNULL);

		if (null == dimensionPixel)
			throw new IllegalArgumentException(TAG + ERROR_DIMENSIONPIXELNULL);

		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dimensionPixel, context.getResources().getDisplayMetrics());

	}
}
