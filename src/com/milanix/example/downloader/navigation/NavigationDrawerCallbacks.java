package com.milanix.example.downloader.navigation;

import com.milanix.example.downloader.navigation.NavigationDrawerFragment.RootFragment;

/**
 * Callbacks interface for callers to listen to changes
 */
public interface NavigationDrawerCallbacks {

	/**
	 * Called when an item in the navigation drawer is selected.
	 * 
	 * @param selectedFragment
	 *            fragment that was selected
	 * 
	 */
	public void onNavigationDrawerItemSelected(RootFragment selectedFragment);

}