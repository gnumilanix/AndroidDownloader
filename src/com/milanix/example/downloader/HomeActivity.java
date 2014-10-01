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
import android.util.Log;

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

	private static final String TAG = HomeActivity.class.getSimpleName();

	/**
	 * This enum defines device definition to choose between layouts
	 * 
	 * @author Milan
	 * 
	 */
	private enum DeviceDefinition {
		MOBILE, TABLET
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		setVersionBasedPolicy();
		setDownloadPath();
		addActionBarTabs();
		setDefinationBasedUI();

		startService(new Intent(this, DownloadService.class));
	}

	/**
	 * This method will set ui based on the device definition
	 */
	private void setDefinationBasedUI() {
		if (DeviceDefinition.TABLET.equals(getDeviceDefinition())) {
			Log.d(TAG, "This is a tablet");
		} else {
			Log.d(TAG, "This is a phone");
		}
	}

	/**
	 * This method will get device definition based on the layout. The default
	 * definition will be mobile if not defined any
	 * 
	 * @return DeviceDefination
	 */
	private DeviceDefinition getDeviceDefinition() {
		if (getResources().getInteger(R.integer.grid_column) > 1)
			return DeviceDefinition.TABLET;
		else
			return DeviceDefinition.MOBILE;
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
		File currentDownloadRoot = new File(
				PreferenceHelper.getDownloadPath(this));

		if (null == currentDownloadRoot || !currentDownloadRoot.exists()
				|| !currentDownloadRoot.isDirectory()) {
			File downloadDir = new File(
					Environment.getExternalStorageDirectory()
							+ PreferenceHelper.PATH_DOWNLOAD);

			if (!downloadDir.exists() || !downloadDir.isDirectory())
				downloadDir.mkdir();

			PreferenceHelper.setDownloadPath(this,
					downloadDir.getAbsolutePath());
		}
	}
}
