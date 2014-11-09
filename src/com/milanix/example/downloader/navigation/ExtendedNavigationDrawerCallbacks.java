package com.milanix.example.downloader.navigation;

/**
 * Callbacks interface for callers to listen to extends changes
 */
public interface ExtendedNavigationDrawerCallbacks extends
		NavigationDrawerCallbacks {

	/**
	 * Called when a drawer is opened
	 */
	public void onDrawerOpened();

	/**
	 * Called when a drawer is closed
	 */
	public void onDrawerClosed();
}
