package com.milanix.example.downloader;

import java.io.File;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.milanix.example.downloader.data.database.DownloadsDatabase.DownloadsDBHelper;
import com.milanix.example.downloader.fragment.DownloadedFragment;
import com.milanix.example.downloader.fragment.DownloadingFragment;
import com.milanix.example.downloader.service.DownloadService;
import com.milanix.example.downloader.util.PreferenceHelper;

/**
 * This is the main activity of this app. It contains logic to control fragment
 * 
 * @author Milan
 * 
 */
public class HomeActivity extends ActionBarActivity {

	private SQLiteDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		addActionBarTabs();
		setDownloadPath();

		startService(new Intent(this, DownloadService.class));

	}

	/**
	 * This method will set download path if not set previously or not exist
	 */
	private void setDownloadPath() {
		File downloadDir = new File(Environment.getExternalStorageDirectory()
				+ PreferenceHelper.PATH_DOWNLOAD);

		if (!downloadDir.exists() || !downloadDir.isDirectory())
			downloadDir.mkdir();

		PreferenceHelper.setDownloadPath(this, downloadDir.getAbsolutePath());
	}

	/**
	 * This method will set actionbar tabs
	 */
	private void addActionBarTabs() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);

		Tab tab = actionBar
				.newTab()
				.setText(R.string.tab_downloading)
				.setTabListener(
						new TabListener<DownloadingFragment>(this,
								DownloadingFragment.class.getSimpleName(),
								DownloadingFragment.class));
		actionBar.addTab(tab);

		tab = actionBar
				.newTab()
				.setText(R.string.tab_downloaded)
				.setTabListener(
						new TabListener<DownloadedFragment>(this,
								DownloadedFragment.class.getSimpleName(),
								DownloadedFragment.class));
		actionBar.addTab(tab);
	}

	@Override
	protected void onDestroy() {
		closeDatabase();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * This method will return downloads database
	 * 
	 * @return download database e
	 */
	public SQLiteDatabase getDatabase() {
		return openDatabase();
	}

	/**
	 * This method will open connection to the database
	 * 
	 * @return database that was opened, otherwise creates a new one
	 */
	private SQLiteDatabase openDatabase() {
		if (null == database || !database.isOpen())
			database = DownloadsDBHelper.getInstance(this)
					.getWritableDatabase();

		return database;
	}

	/**
	 * This method will close the database if exist and is open
	 */
	public void closeDatabase() {
		if (null != database && database.isOpen())
			database.close();
	}
}
