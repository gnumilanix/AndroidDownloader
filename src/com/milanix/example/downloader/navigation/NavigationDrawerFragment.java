package com.milanix.example.downloader.navigation;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.pref.PreferenceHelper;

/**
 * This is a navigation drawer fragment
 * 
 * @author Milan
 * 
 */
public class NavigationDrawerFragment extends Fragment implements
		NavigationDrawerCallbacks {

	private static final String KEY_SELECTED_FRAGMENT = "key_selected_fragment";

	private NavigationDrawerCallbacks navigationDrawerCallbacks;

	private ActionBarDrawerToggle drawerToggle;

	private DrawerLayout drawerLayout;

	private View fragmentContainerView;

	private RootFragment currentSelectedFragment = RootFragment.DOWNLOADING;
	private boolean fromSavedInstanceState;
	private boolean hasUserLearnedDrawer;

	private RecyclerView drawerList;

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
		setHasOptionsMenu(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_navdrawer, null);

		setView(view);
		setListener();
		setAdapter();

		navigateToSelected(currentSelectedFragment);

		return view;
	}

	/**
	 * This method will set UI components
	 * 
	 * @param view
	 */
	private void setView(View root) {
		LinearLayoutManager layoutManager = new LinearLayoutManager(
				getActivity());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

		drawerList = (RecyclerView) root.findViewById(R.id.drawerList);
		drawerList.setLayoutManager(layoutManager);
		drawerList.setHasFixedSize(true);
	}

	/**
	 * This method is set listener
	 */
	private void setListener() {
	}

	/**
	 * Sets navigation adapter
	 */
	private void setAdapter() {
		final NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(
				getMenu());
		adapter.setNavigationDrawerCallbacks(this);

		drawerList.setAdapter(adapter);
	}

	/**
	 * get navigation menu list
	 * 
	 * @return
	 */
	public List<NavigationItem> getMenu() {
		final List<NavigationItem> items = new ArrayList<NavigationItem>();
		items.clear();

		items.add(new NavigationItem(getString(R.string.tab_downloading),
				getResources().getDrawable(R.drawable.ic_icon_task_light),
				RootFragment.DOWNLOADING));
		items.add(new NavigationItem(getString(R.string.tab_downloaded),
				getResources().getDrawable(R.drawable.ic_icon_path_light),
				RootFragment.DOWNLOADED));
		items.add(new NavigationItem(
				getString(R.string.tab_settings),
				getResources().getDrawable(R.drawable.ic_action_settings_light),
				RootFragment.SETTINGS));

		return items;
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

	public void setUp(int navFragmentId, DrawerLayout drawerLayout,
			Toolbar toolbar) {
		fragmentContainerView = getActivity().findViewById(navFragmentId);
		this.drawerLayout = drawerLayout;

		drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,
				toolbar, R.string.navigation_drawer_open,
				R.string.navigation_drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);

				if (!isAdded())
					return;

				getActivity().invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded())
					return;

				if (!hasUserLearnedDrawer) {
					hasUserLearnedDrawer = true;

					PreferenceHelper.setHasLearnedDrawer(getActivity(),
							hasUserLearnedDrawer);
				}

				getActivity().invalidateOptionsMenu();
			}
		};

		if (!hasUserLearnedDrawer && !fromSavedInstanceState)
			drawerLayout.openDrawer(fragmentContainerView);

		drawerLayout.post(new Runnable() {
			@Override
			public void run() {
				drawerToggle.syncState();
			}
		});

		drawerLayout.setDrawerListener(drawerToggle);
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

	/**
	 * Returns if drawer is open
	 * 
	 * @return
	 */
	public boolean isDrawerOpen() {
		return drawerLayout != null
				&& drawerLayout.isDrawerOpen(fragmentContainerView);
	}

	/**
	 * Navigation item
	 */
	public class NavigationItem {
		private String text;
		private Drawable drawable;
		private RootFragment rootFragment;

		public NavigationItem(String text, Drawable drawable,
				RootFragment rootFragment) {
			this.text = text;
			this.drawable = drawable;
			this.rootFragment = rootFragment;
		}

		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}

		/**
		 * @param text
		 *            the text to set
		 */
		public void setText(String text) {
			this.text = text;
		}

		/**
		 * @return the drawable
		 */
		public Drawable getDrawable() {
			return drawable;
		}

		/**
		 * @param drawable
		 *            the drawable to set
		 */
		public void setDrawable(Drawable drawable) {
			this.drawable = drawable;
		}

		/**
		 * @return the rootFragment
		 */
		public RootFragment getRootFragment() {
			return rootFragment;
		}

		/**
		 * @param rootFragment
		 *            the rootFragment to set
		 */
		public void setRootFragment(RootFragment rootFragment) {
			this.rootFragment = rootFragment;
		}

	}

	@Override
	public void onNavigationDrawerItemSelected(RootFragment selectedFragment) {
		navigationDrawerCallbacks
				.onNavigationDrawerItemSelected(selectedFragment);

		navigateToSelected(selectedFragment);
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
					.onNavigationDrawerItemSelected(selectedFragment);

		// ((NavigationDrawerAdapter) drawerList.getAdapter())
		// .selectPosition(position);
	}
}
