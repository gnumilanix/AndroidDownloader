package com.milanix.example.downloader.activity;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.milanix.example.downloader.Downloader;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.dialog.AddNewDownloadDialog;
import com.milanix.example.downloader.dialog.AddNewDownloadDialog.OnAddNewDownloadListener;
import com.milanix.example.downloader.fragment.BrowseFragment;
import com.milanix.example.downloader.fragment.DownloadedFragment;
import com.milanix.example.downloader.fragment.DownloadingFragment;
import com.milanix.example.downloader.fragment.SettingsFragment;
import com.milanix.example.downloader.navigation.NavigationDrawerFragment;
import com.milanix.example.downloader.navigation.NavigationDrawerFragment.RootFragment;
import com.milanix.example.downloader.navigation.NavigationSpinnerAdapter;
import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.service.DownloadService;
import com.milanix.example.downloader.util.NetworkUtils;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This is the main activity of this app. It contains logic to control fragment
 * 
 * @author Milan
 * 
 */
public class HomeActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks,
		OnNavigationListener, OnAddNewDownloadListener, OnClickListener {

	private static final String KEY_INTENT_PROCESSED = "INTENT_PROCESSED";

	private HashMap<RootFragment, Fragment> fragmentCache = new HashMap<RootFragment, Fragment>();

	private ImageButton download_add;

	private boolean bound = false;

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			bound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}
	};

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
	public void onStart() {
		super.onStart();

		bindService(new Intent(this, DownloadService.class), connection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();

		if (bound) {
			unbindService(connection);

			bound = false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService(new Intent(this, DownloadService.class));

		setContentView(R.layout.activity_home);

		setVersionBasedPolicy();
		setDownloadPath();

		setDefinationBasedUI();

		setUI();
		setListener();

		// TODO Process only if intent is not already handled.
		if (savedInstanceState != null) {

			if (!savedInstanceState.getBoolean(KEY_INTENT_PROCESSED))
				handleIncoming(getIntent());

			if (savedInstanceState
					.containsKey(DownloadService.KEY_OPEN_DOWNLOADED))
				switchToFragment(RootFragment.DOWNLOADED);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(KEY_INTENT_PROCESSED, true);
	}

	/**
	 * This method will set UI components
	 */
	private void setUI() {
		download_add = (ImageButton) findViewById(R.id.download_add);
	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
		download_add.setOnClickListener(this);
	}

	/**
	 * Handles incoming intent
	 * 
	 * @param intent
	 */
	private void handleIncoming(Intent intent) {
		if (null != intent && null != intent.getData()) {
			String incomingUri = intent.getData().toString();

			if (!TextUtils.isEmpty(incomingUri)) {
				if (Downloader.HTTP_VALIDATOR.isValid(incomingUri)
						|| Downloader.FTP_VALIDATOR.isValid(incomingUri)) {
					Bundle bundle = new Bundle();
					bundle.putString(AddNewDownloadDialog.KEY_ADDNEW_URL,
							incomingUri);

					showAddNewDialog(bundle);
				} else {
					ToastHelper.showToast(this,
							getString(R.string.addnew_request_invalid));
				}
			}
		}
	}

	/**
	 * This method will set ui based on the device definition
	 */
	private void setDefinationBasedUI() {
		if (DeviceDefinition.TABLET.equals(getDeviceDefinition())) {
			setupNativationDrawer();
			// addActionBarTabs();
		} else {
			addActionBarSpinner();
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
	 * This method will setup navigation drawer
	 */
	private void addActionBarSpinner() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(
				new NavigationSpinnerAdapter(this), this);
	}

	/**
	 * This method will setup navigation drawer
	 */
	private void setupNativationDrawer() {
		final NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		navigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	/**
	 * This method will set actionbar tabs
	 */
	private void addActionBarTabs() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
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
				.setIcon(R.drawable.ic_action_settings_dark)
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
							+ PreferenceHelper.DEFAULT_DOWNLOAD_FOLDER);

			if (!downloadDir.exists() || !downloadDir.isDirectory())
				downloadDir.mkdir();

			PreferenceHelper.setDownloadPath(this,
					downloadDir.getAbsolutePath());
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.download_add) {
			showAddNewDialog(null);
		}
	}

	/**
	 * This method will switch fragment based on id
	 * 
	 * @param id
	 */
	private void switchToFragment(RootFragment selectedFragment) {
		if (null != selectedFragment) {
			String title = null;
			String tag = null;

			Fragment fragment = null;

			getSupportActionBar().show();

			// Either get cached fragments of create a new one
			switch (selectedFragment) {
			case DOWNLOADING:
				title = getString(R.string.tab_downloading);
				tag = DownloadingFragment.class.getSimpleName();

				if (fragmentCache.containsKey(selectedFragment))
					fragment = fragmentCache.get(selectedFragment);
				else
					fragment = new DownloadingFragment();

				break;
			case DOWNLOADED:
				title = getString(R.string.tab_downloaded);
				tag = DownloadedFragment.class.getSimpleName();

				if (fragmentCache.containsKey(selectedFragment))
					fragment = fragmentCache.get(selectedFragment);
				else
					fragment = new DownloadedFragment();

				break;
			case SETTINGS:
				title = getString(R.string.tab_settings);
				tag = SettingsFragment.class.getSimpleName();

				if (fragmentCache.containsKey(selectedFragment))
					fragment = fragmentCache.get(selectedFragment);
				else
					fragment = new SettingsFragment();

				break;
			case BROWSE:
				title = getString(R.string.tab_browse);
				tag = BrowseFragment.class.getSimpleName();

				if (fragmentCache.containsKey(selectedFragment))
					fragment = fragmentCache.get(selectedFragment);
				else
					fragment = new BrowseFragment();

				break;
			}

			if (null != title && null != tag && null != fragment) {
				getSupportActionBar().show();
				getSupportActionBar().setTitle(title);

				invalidateOptionsMenu();

				final FragmentManager manager = getSupportFragmentManager();
				final FragmentTransaction transaction = manager
						.beginTransaction();
				final Fragment existingFragment = manager
						.findFragmentByTag(tag);

				if (null == existingFragment) {
					transaction.add(R.id.container, fragment, tag)
							.show(fragment).show(fragment);

					fragmentCache.put(selectedFragment, fragment);
				} else {
					transaction.show(existingFragment);
				}

				List<Fragment> existingFragments = manager.getFragments();

				if (null != existingFragments && !existingFragments.isEmpty())
					for (Fragment stackFragment : existingFragments) {
						if (null != stackFragment) {
							if (!stackFragment.getClass().equals(
									fragment.getClass()))
								transaction.hide(stackFragment);
						}
					}

				transaction.commit();

			}
		}
	}

	/**
	 * This method will show add new download dialog
	 * 
	 * @param bundle
	 *            to be passed
	 */
	private void showAddNewDialog(Bundle bundle) {
		DialogFragment newFragment = new AddNewDownloadDialog();
		newFragment.setArguments(bundle);
		newFragment.setCancelable(true);
		newFragment.show(getSupportFragmentManager(),
				AddNewDownloadDialog.class.getSimpleName());
	}

	@Override
	public void onNewDownloadAdded(Integer id) {
		if (null != id) {
			Download addedDownload = new Download().retrieve(this, id);

			if (null != addedDownload && null != addedDownload.getUrl()) {
				ToastHelper.showToast(this, String.format(
						getString(R.string.download_add_success),
						addedDownload.getUrl()));

				if (NetworkUtils.isNetworkConnected(this))
					pushDownloadToService(addedDownload);
				else
					ToastHelper.showToast(this,
							getString(R.string.download_disconnected));
			} else {
				ToastHelper.showToast(this,
						getString(R.string.download_add_fail));
			}

		} else
			ToastHelper.showToast(this, getString(R.string.download_add_fail));
	}

	/**
	 * This method will push download to the service
	 * 
	 * @param download
	 */
	protected void pushDownloadToService(Download download) {
		if (bound) {
			DownloadService.downloadFile(download.getId());
		}
	}

	@Override
	public void onNavigationDrawerItemSelected(RootFragment selectedFragment) {
		switchToFragment(selectedFragment);
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		switch (position) {
		case 0:
			switchToFragment(RootFragment.DOWNLOADING);
			return true;
		case 1:
			switchToFragment(RootFragment.DOWNLOADED);
			return true;
		case 2:
			switchToFragment(RootFragment.SETTINGS);
			return true;
		case 3:
			switchToFragment(RootFragment.BROWSE);
			return true;
		}

		return false;
	}

	/**
	 * This class contains a tab listener
	 * 
	 * @author Milan
	 * 
	 * @param <T>
	 */
	public class TabListener<T extends Fragment> implements
			ActionBar.TabListener {
		private Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		/**
		 * Constructor used each time a new tab is created.
		 * 
		 * @param activity
		 *            The host Activity, used to instantiate the fragment
		 * @param tag
		 *            The identifier tag for the fragment
		 * @param clazz
		 *            The fragment's Class, used to instantiate the fragment
		 */
		public TabListener(Activity activity, String tag, Class<T> clazz) {
			mActivity = activity;
			mTag = tag;
			mClass = clazz;
		}

		/* The following are each of the ActionBar.TabListener callbacks */

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (mFragment == null) {
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				ft.attach(mFragment);
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				ft.detach(mFragment);
			}
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// User selected the already selected tab. Usually do nothing.
		}

	}

}
