package com.milanix.example.downloader.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.dialog.PathConfigureDialog;
import com.milanix.example.downloader.dialog.PathConfigureDialog.OnPathConfigureListener;
import com.milanix.example.downloader.dialog.PoolConfigureDialog;
import com.milanix.example.downloader.dialog.PoolConfigureDialog.OnPoolConfigureListener;
import com.milanix.example.downloader.fragment.abs.AbstractFragment;
import com.milanix.example.downloader.util.PreferenceHelper;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This fragment contains settings configuration.
 */
public class SettingsFragment extends AbstractFragment implements
		View.OnClickListener, OnPoolConfigureListener, OnPathConfigureListener {

	private View rootView;

	private ViewGroup location_base;
	private ViewGroup tasks_base;

	private TextView location_path;
	private TextView tasks_config;

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

		onInit();

		return rootView;
	}

	@Override
	protected void setUI() {
		location_base = (ViewGroup) rootView.findViewById(R.id.location_base);
		tasks_base = (ViewGroup) rootView.findViewById(R.id.tasks_base);

		location_path = (TextView) rootView.findViewById(R.id.location_path);
		tasks_config = (TextView) rootView.findViewById(R.id.tasks_config);

		location_path.setText(PreferenceHelper.getDownloadPath(getActivity()));
		tasks_config.setText(getResources().getQuantityString(
				R.plurals.tasks_configured,
				PreferenceHelper.getDownloadPoolSize(getActivity()),
				PreferenceHelper.getDownloadPoolSize(getActivity())));
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
			showPathConfigureDialog();
		} else if (view.getId() == R.id.tasks_base) {
			showTaskConfigureDialog();
		}
	}

	/**
	 * This method will show task configure dialog
	 */
	private void showTaskConfigureDialog() {
		PoolConfigureDialog newFragment = new PoolConfigureDialog();
		newFragment.setTargetFragment(this, -1);
		newFragment.setCancelable(true);
		newFragment.show(getFragmentManager(),
				PoolConfigureDialog.class.getSimpleName());
	}

	/**
	 * This method will show task configure dialog
	 */
	private void showPathConfigureDialog() {
		PathConfigureDialog newFragment = new PathConfigureDialog();
		newFragment.setTargetFragment(this, -1);
		newFragment.setCancelable(true);
		newFragment.show(getFragmentManager(),
				PathConfigureDialog.class.getSimpleName());
	}

	@Override
	public void onPoolConfigured(Integer poolSize) {
		if (null != poolSize) {
			ToastHelper.showToast(getActivity(), String.format(
					getString(R.string.taskconfigure_sucess), poolSize));

			tasks_config.setText(getResources().getQuantityString(
					R.plurals.tasks_configured, poolSize, poolSize));
		}
	}

	@Override
	public void onPathConfigured(String path) {
		if (null != path)
			ToastHelper.showToast(getActivity(), String.format(
					getString(R.string.pathconfigure_sucess), path));
	}
}
