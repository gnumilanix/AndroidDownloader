package com.milanix.example.downloader.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.fragment.abs.AbstractFragment;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This fragment contains settings configuration.
 */
public class SettingsFragment extends AbstractFragment implements
		View.OnClickListener {

	private View rootView;

	private ViewGroup location_base;
	private ViewGroup tasks_base;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(false);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_settings, container,
				false);

		return rootView;
	}

	@Override
	protected void setUI() {
		location_base = (ViewGroup) rootView.findViewById(R.id.location_base);
		tasks_base = (ViewGroup) rootView.findViewById(R.id.tasks_base);
	}

	@Override
	protected void setListener() {
		location_base.setOnClickListener(this);
		tasks_base.setOnClickListener(this);
	}

	@Override
	public String getLogTag() {
		return SettingsFragment.class.getSimpleName();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.location_base) {
			ToastHelper.showToast(getActivity(),
					"Location settings not yet implemented");
		} else if (view.getId() == R.id.tasks_base) {
			ToastHelper.showToast(getActivity(),
					"Downloads settings not yet implemented");
		}
	}
}
