package com.milanix.example.downloader.fragment.abs;

import android.support.v4.app.Fragment;

/**
 * This is an abstract specification of fragments used in this app
 * 
 * @author Milan
 * 
 */
public abstract class AbstractFragment extends Fragment {
	/**
	 * This method should be called to init ui components
	 */
	protected void onInit() {
		setUI();
		setListener();
	}

	/**
	 * This method will set UI components
	 */
	protected abstract void setUI();

	/**
	 * This method will set listeners to the components
	 */
	protected abstract void setListener();

	/**
	 * This method will return tag
	 */
	public abstract String getLogTag();
}
