package com.milanix.example.downloader.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ActionMode;
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
import com.milanix.example.downloader.navigation.ExtendedNavigationDrawerCallbacks;
import com.milanix.example.downloader.navigation.NavigationDrawerFragment;
import com.milanix.example.downloader.navigation.NavigationDrawerFragment.RootFragment;
import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.service.DownloadService;
import com.milanix.example.downloader.util.NetworkUtils;
import com.milanix.example.downloader.util.ToastHelper;
import com.milanix.example.downloader.view.SlidingTabLayout;
import com.milanix.example.downloader.view.SlidingTabLayout.TabColorizer;

/**
 * This is the main activity of this app. It contains logic to control fragment
 * 
 * @author Milan
 * 
 */
public class HomeActivity extends ActionBarActivity implements
		ExtendedNavigationDrawerCallbacks, OnPageChangeListener,
		OnAddNewDownloadListener, OnClickListener {

	private static final String KEY_INTENT_PROCESSED = "INTENT_PROCESSED";

	private Toolbar toolbar_actionbar;
	private ImageButton download_add;
	private View view_rateme;

	private SlidingTabLayout slidingTabLayout;
	private ViewPager viewPager;
	private PagerAdapter pagerAdapter;

	private ActionMode actionMode;
	private ActionMode.Callback actionCallback;

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

		setUI();
		setToolBar();
		setListener();
		setDefinationBasedUI();

		// TODO Process only if intent is not already handled.
		if (savedInstanceState != null) {
			if (!savedInstanceState.getBoolean(KEY_INTENT_PROCESSED))
				handleIncoming(getIntent());

			if (savedInstanceState
					.containsKey(DownloadService.KEY_OPEN_DOWNLOADED))
				switchToFragment(RootFragment.DOWNLOADED);
		}
	}

	/**
	 * Sets toolbar_actionbar
	 */
	private void setToolBar() {
		setSupportActionBar(toolbar_actionbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
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
		toolbar_actionbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		download_add = (ImageButton) findViewById(R.id.download_add);
		view_rateme = (View) findViewById(R.id.view_rateme);

		view_rateme.setVisibility(View.GONE);
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
			setSlidingTab();
		} else {
			setupNativationDrawer();
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
	 * Sets up sliding tab
	 */
	private void setSlidingTab() {
		final ArrayList<FragmentStub> fragmentList = new ArrayList<FragmentStub>();
		fragmentList.add(new FragmentStub(new DownloadingFragment(),
				getString(R.string.tab_downloading)));
		fragmentList.add(new FragmentStub(new DownloadedFragment(),
				getString(R.string.tab_downloaded)));
		fragmentList.add(new FragmentStub(new SettingsFragment(),
				getString(R.string.tab_settings)));

		pagerAdapter = new PagerAdapter(getSupportFragmentManager(),
				fragmentList);

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setOnPageChangeListener(this);
		viewPager.setAdapter(pagerAdapter);

		slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
		slidingTabLayout.setCustomTabColorizer(new TabColorizer() {

			@Override
			public int getIndicatorColor(int position) {
				return getResources().getColor(R.color.slidingtab_indicator);
			}

			@Override
			public int getDividerColor(int position) {
				return getResources().getColor(R.color.slidingtab_indicator);
			}

		});
		slidingTabLayout.setViewPager(viewPager);

	}

	/**
	 * This method will setup navigation drawer
	 */
	private void setupNativationDrawer() {
		final NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		navigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout),
				toolbar_actionbar);
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

			final FragmentManager manager = getSupportFragmentManager();
			final FragmentTransaction transaction = manager.beginTransaction();

			Fragment fragment = manager.findFragmentByTag(tag);

			// Either get cached fragments of create a new one
			switch (selectedFragment) {
			case DOWNLOADING:
				title = getString(R.string.tab_downloading);
				tag = DownloadingFragment.class.getSimpleName();

				if (null == fragment)
					fragment = new DownloadingFragment();

				break;
			case DOWNLOADED:
				title = getString(R.string.tab_downloaded);
				tag = DownloadedFragment.class.getSimpleName();

				if (null == fragment)
					fragment = new DownloadedFragment();

				break;
			case SETTINGS:
				title = getString(R.string.tab_settings);
				tag = SettingsFragment.class.getSimpleName();

				if (null == fragment)
					fragment = new SettingsFragment();

				break;
			case BROWSE:
				title = getString(R.string.tab_browse);
				tag = BrowseFragment.class.getSimpleName();

				if (null == fragment)
					fragment = new BrowseFragment();

				break;
			}

			if (null != title && null != tag && null != fragment) {
				if (null != getSupportActionBar()) {
					getSupportActionBar().show();
					getSupportActionBar().setTitle(title);
				}

				if (!fragment.isHidden() && !fragment.isAdded())
					transaction.add(R.id.container, fragment, tag)
							.show(fragment).show(fragment);
				else
					transaction.show(fragment);

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
	public void onDrawerOpened() {
		if (null != actionMode) {
			actionMode.finish();
		}
	}

	@Override
	public void onDrawerClosed() {
		if (null != actionCallback && null != actionMode) {
			startActionMode(actionCallback);

		}
	}

	@Override
	public void onActionModeStarted(ActionMode actionMode) {
		super.onActionModeStarted(actionMode);

		this.actionMode = actionMode;
	}

	@Override
	public void onActionModeFinished(ActionMode actionMode) {
		super.onActionModeFinished(actionMode);

		this.actionMode = null;
	}

	@Override
	public ActionMode onWindowStartingActionMode(
			ActionMode.Callback actionCallback) {
		this.actionCallback = actionCallback;

		return super.onWindowStartingActionMode(actionCallback);
	}

	/**
	 * The {@link android.support.v4.view.PagerAdapter} used to display pages in
	 * this sample. The individual pages are simple and just display two lines
	 * of text. The important section of this class is the
	 * {@link #getPageTitle(int)} method which controls what is displayed in the
	 * {@link SlidingTabLayout}.
	 */
	private class PagerAdapter extends FragmentStatePagerAdapter {
		private ArrayList<FragmentStub> fragmentList;

		public PagerAdapter(FragmentManager fragmentManager,
				ArrayList<FragmentStub> fragmentList) {
			super(fragmentManager);

			this.fragmentList = fragmentList;
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {

			return fragmentList.get(position).title;
		}

		@Override
		public Fragment getItem(int position) {

			return fragmentList.get(position).fragment;
		}

	}

	/**
	 * Fragment stub object
	 * 
	 * @author Milan
	 * 
	 */
	private class FragmentStub {
		public Fragment fragment;
		public String title;

		public FragmentStub(Fragment fragment, String title) {
			this.fragment = fragment;
			this.title = title;
		}

	}

	@Override
	public void onPageScrollStateChanged(int position) {

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		if (null != getSupportActionBar() && null != pagerAdapter) {
			getSupportActionBar().show();
			getSupportActionBar().setTitle(pagerAdapter.getPageTitle(position));
		}
	}

}
