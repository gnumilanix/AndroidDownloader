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
	 * This method will set UI components
	 */
	public abstract void setUI();

	/**
	 * This method will set listeners to the components
	 */
	public abstract void setListener();

	/**
	 * This method will return tag
	 */
	public abstract String getLogTag();
}
