package com.milanix.example.downloader;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;

/**
 * This class contains a tab listener
 * 
 * @author Milan
 * 
 * @param <T>
 */
public class TabListener<T extends Fragment> implements ActionBar.TabListener {
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