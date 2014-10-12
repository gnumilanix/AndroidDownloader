package com.milanix.example.downloader.navigation;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanix.example.downloader.HomeActivity;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.pref.PreferenceHelper;

/**
 * This is a navigation drawer fragment
 * 
 * @author Milan
 * 
 */
public class NavigationDrawerFragment extends Fragment implements
		View.OnClickListener {

	private static final String KEY_SELECTED_FRAGMENT = "key_selected_fragment";

	private NavigationDrawerCallbacks navigationDrawerCallbacks;

	private ActionBarDrawerToggle drawerToggle;

	private DrawerLayout drawerLayout;

	private View fragmentContainerView;

	private RootFragment currentSelectedFragment = RootFragment.DOWNLOADING;
	private boolean fromSavedInstanceState;
	private boolean hasUserLearnedDrawer;

	private TextView tab_downloading;
	private TextView tab_downloaded;
	private TextView tab_settings;
	private TextView tab_browse;

	/**
	 * Root fragments for this navigation drawer
	 * 
	 * @author Milan
	 * 
	 */
	public static enum RootFragment {
		DOWNLOADING, DOWNLOADED, SETTINGS, BROWSE
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			currentSelectedFragment = RootFragment.valueOf(savedInstanceState
					.getString(KEY_SELECTED_FRAGMENT));
			fromSavedInstanceState = true;
		}

		hasUserLearnedDrawer = PreferenceHelper
				.getHasLearnedDrawer(getActivity());

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_navdrawer, null);

		setView(view);
		setListener();

		return view;
	}

	/**
	 * This method will set UI components
	 * 
	 * @param view
	 */
	private void setView(View root) {
		tab_downloading = (TextView) root.findViewById(R.id.tab_downloading);
		tab_downloaded = (TextView) root.findViewById(R.id.tab_downloaded);
		tab_settings = (TextView) root.findViewById(R.id.tab_settings);
		tab_browse = (TextView) root.findViewById(R.id.tab_browse);
	}

	/**
	 * This method is set listener
	 */
	private void setListener() {
		tab_downloading.setOnClickListener(this);
		tab_downloaded.setOnClickListener(this);
		tab_settings.setOnClickListener(this);
		tab_browse.setOnClickListener(this);
	}

	/**
	 * This method checks if drawer is open
	 * 
	 * @return
	 */
	public boolean isDrawerOpen() {
		return drawerLayout != null
				&& drawerLayout.isDrawerOpen(fragmentContainerView);
	}

	/**
	 * Users of this fragment must call this method to set up the navigation
	 * drawer interactions.
	 * 
	 * @param navFragmentId
	 *            The android:id of this fragment in its activity's layout.
	 * @param drawerLayout
	 *            The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int navFragmentId, DrawerLayout navDrawerLayout) {
		navigateToSelected(currentSelectedFragment);

		fragmentContainerView = getActivity().findViewById(navFragmentId);
		drawerLayout = navDrawerLayout;

		drawerLayout.setDrawerShadow(R.drawable.ic_navdrawer_shadow,
				GravityCompat.START);

		((HomeActivity) getActivity()).getSupportActionBar()
				.setDisplayHomeAsUpEnabled(true);
		((HomeActivity) getActivity()).getSupportActionBar()
				.setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,
				R.drawable.ic_navdrawer, R.string.navigation_drawer_open,
				R.string.navigation_drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}

				getActivity().invalidateOptionsMenu();

			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}

				if (!hasUserLearnedDrawer) {
					hasUserLearnedDrawer = true;

					PreferenceHelper.setHasLearnedDrawer(getActivity(),
							hasUserLearnedDrawer);
				}

				getActivity().invalidateOptionsMenu();
			}

		};

		if (!hasUserLearnedDrawer && !fromSavedInstanceState) {
			drawerLayout.openDrawer(fragmentContainerView);
		}

		drawerToggle.syncState();

		drawerLayout.setDrawerListener(drawerToggle);
	}

	/**
	 * This method will navigate to selected item
	 * 
	 * @param position
	 */
	private void navigateToSelected(RootFragment selectedFragment) {
		currentSelectedFragment = selectedFragment;

		if (drawerLayout != null)
			drawerLayout.closeDrawer(fragmentContainerView);

		if (navigationDrawerCallbacks != null)
			navigationDrawerCallbacks
					.onNavigationDrawerItemSelected(currentSelectedFragment);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			navigationDrawerCallbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		navigationDrawerCallbacks = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_SELECTED_FRAGMENT,
				currentSelectedFragment.toString());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.tab_downloading) {
			unselectTabs(tab_downloading);

			navigateToSelected(RootFragment.DOWNLOADING);
		} else if (view.getId() == R.id.tab_downloaded) {
			unselectTabs(tab_downloaded);

			navigateToSelected(RootFragment.DOWNLOADED);
		} else if (view.getId() == R.id.tab_settings) {
			unselectTabs(tab_settings);

			navigateToSelected(RootFragment.SETTINGS);
		} else if (view.getId() == R.id.tab_browse) {
			unselectTabs(tab_browse);

			navigateToSelected(RootFragment.BROWSE);
		}
	}

	/**
	 * This method will unselect tabs except one that is to be selected
	 * 
	 * @param toBeSelected
	 *            is the tab to be selected
	 */
	private void unselectTabs(View toBeSelected) {
		tab_downloading.setBackgroundColor(getResources().getColor(
				R.color.navigation_tab_normal));
		tab_downloaded.setBackgroundColor(getResources().getColor(
				R.color.navigation_tab_normal));
		tab_settings.setBackgroundColor(getResources().getColor(
				R.color.navigation_tab_normal));
		tab_browse.setBackgroundColor(getResources().getColor(
				R.color.navigation_tab_normal));

		if (null != toBeSelected)
			toBeSelected.setBackgroundColor(getResources().getColor(
					R.color.navigation_tab_selected));
	}

	/**
	 * Callbacks interface for callers to listen to changes
	 */
	public static interface NavigationDrawerCallbacks {

		/**
		 * Called when an item in the navigation drawer is selected.
		 * 
		 * @param selectedFragment
		 *            fragment that was selected
		 * 
		 */
		void onNavigationDrawerItemSelected(RootFragment selectedFragment);

	}

}
