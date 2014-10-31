package com.milanix.example.downloader;

import org.apache.commons.validator.routines.UrlValidator;

import android.app.Application;
import android.content.Context;

/**
 * Base application implementation
 * 
 * @author Milan
 * 
 */
public class Downloader extends Application {

	public static final String HTTP = "http";
	public static final String HTTPS = "https";
	public static final String FTP = "ftp";

	private static final String[] HTTP_SCHEMAS = { HTTP, HTTPS };
	private static final String[] FTP_SCHEMAS = { FTP };

	public static final UrlValidator HTTP_VALIDATOR = new UrlValidator(
			HTTP_SCHEMAS);
	public static final UrlValidator FTP_VALIDATOR = new UrlValidator(
			FTP_SCHEMAS);

	private static Application application;

	@Override
	public void onCreate() {
		if (null == application)
			application = this;

		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	/**
	 * Returns application context
	 * 
	 * @return
	 */
	public static Context getDownloaderContext() {
		return application;
	}

}
