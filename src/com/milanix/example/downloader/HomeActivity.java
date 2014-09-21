package com.milanix.example.downloader;

import java.io.File;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.milanix.example.downloader.fragment.DownloadedFragment;
import com.milanix.example.downloader.fragment.DownloadingFragment;
import com.milanix.example.downloader.fragment.SettingsFragment;
import com.milanix.example.downloader.service.DownloadService;
import com.milanix.example.downloader.util.PreferenceHelper;

/**
 * This is the main activity of this app. It contains logic to control fragment
 * 
 * @author Milan
 * 
 */
public class HomeActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		setVersionBasedPolicy();
		addActionBarTabs();
		setDownloadPath();

		startService(new Intent(this, DownloadService.class));

	}

	/**
	 * This method will set actionbar tabs
	 */
	private void addActionBarTabs() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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

		tab = actionBar
				.newTab()
				.setIcon(R.drawable.ic_action_settings)
				.setTabListener(
						new TabListener<SettingsFragment>(this,
								SettingsFragment.class.getSimpleName(),
								SettingsFragment.class));
		actionBar.addTab(tab);
	}

	/**
	 * This method will set thread policy
	 */
	private void setVersionBasedPolicy() {
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();

			StrictMode.setThreadPolicy(policy);
		}

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
