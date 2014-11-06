package com.milanix.example.downloader.util;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class will create a http client for our download
 * 
 * @author Milan
 * 
 */
public class NetworkUtils {

	/**
	 * This method will build and return http client. If exist it will return
	 * the existing instance
	 * 
	 * @return http client
	 */
	public static synchronized HttpClient getHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);

		ConnManagerParams.setTimeout(params, 1000);

		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 10000);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
				SSLSocketFactory.getSocketFactory(), 443));

		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);

		return new DefaultHttpClient(conMgr, params);
	}

	/**
	 * This method will build and return ftp client. If exist it will return the
	 * existing instance
	 * 
	 * @return ftp client
	 */
	public static synchronized FTPClient getFTPClient() {
		return new FTPClient();
	}

	/**
	 * Disconnect and closes given FTP client
	 * 
	 * @param ftpClient
	 *            to close
	 */
	public static synchronized void killFTPClient(FTPClient ftpClient) {
		try {
			if (ftpClient.isConnected()) {
				ftpClient.noop();
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (IOException ignored) {

		}
	}

	/**
	 * This method will check if the network is connected. This will not sure it
	 * is connected to the internet
	 * 
	 * @param context
	 *            is the application context
	 * @return true is connected othrwise false
	 */
	public static boolean isNetworkConnected(Context context) {
		NetworkInfo networkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (networkInfo == null || !networkInfo.isConnected())
			return false;

		return true;
	}
}
