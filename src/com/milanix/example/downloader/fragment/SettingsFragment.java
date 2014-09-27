package com.milanix.example.downloader.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog.NetworkType;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog.OnNetworkConfigureListener;
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
		View.OnClickListener, OnPoolConfigureListener, OnPathConfigureListener,
		OnNetworkConfigureListener {

	private View rootView;

	private ViewGroup location_base;
	private ViewGroup tasks_base;
	private ViewGroup warning_base;
	private ViewGroup network_base;

	private TextView location_config;
	private TextView tasks_config;
	private TextView warning_config;
	private TextView network_config;

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
		warning_base = (ViewGroup) rootView.findViewById(R.id.warning_base);
		network_base = (ViewGroup) rootView.findViewById(R.id.network_base);

		location_config = (TextView) rootView
				.findViewById(R.id.location_config);
		tasks_config = (TextView) rootView.findViewById(R.id.tasks_config);
		warning_config = (TextView) rootView.findViewById(R.id.warning_config);
		network_config = (TextView) rootView.findViewById(R.id.network_config);

		location_config
				.setText(PreferenceHelper.getDownloadPath(getActivity()));
		tasks_config.setText(getResources().getQuantityString(
				R.plurals.tasks_configured,
				PreferenceHelper.getDownloadPoolSize(getActivity()),
				PreferenceHelper.getDownloadPoolSize(getActivity())));

		setNetworkTypeString(PreferenceHelper.getDownloadNetwork(getActivity()));
	}

	@Override
	protected void setListener() {
		location_base.setOnClickListener(this);
		tasks_base.setOnClickListener(this);
		network_base.setOnClickListener(this);
		warning_config.setOnClickListener(this);
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
		} else if (view.getId() == R.id.network_base) {
			showNetworkConfigureDialog();
		} else if (view.getId() == R.id.warning_config) {
			showWarningConfigureDialog();
		}
	}

	/**
	 * This method will show task configure dialog
	 */
	private void showTaskConfigureDialog() {
		PoolConfigureDialog taskConfigureDialog = new PoolConfigureDialog();
		taskConfigureDialog.setTargetFragment(this, -1);
		taskConfigureDialog.setCancelable(true);
		taskConfigureDialog.show(getFragmentManager(),
				PoolConfigureDialog.class.getSimpleName());
	}

	/**
	 * This method will show task configure dialog
	 */
	private void showPathConfigureDialog() {
		PathConfigureDialog pathConfigureDialog = new PathConfigureDialog();
		pathConfigureDialog.setTargetFragment(this, -1);
		pathConfigureDialog.setCancelable(true);
		pathConfigureDialog.show(getFragmentManager(),
				PathConfigureDialog.class.getSimpleName());
	}

	/**
	 * This method will show network configure dialog
	 */
	private void showNetworkConfigureDialog() {
		NetworkConfigureDialog networkConfigureDialog = new NetworkConfigureDialog();
		networkConfigureDialog.setTargetFragment(this, -1);
		networkConfigureDialog.setCancelable(true);
		networkConfigureDialog.show(getFragmentManager(),
				NetworkConfigureDialog.class.getSimpleName());
	}

	/**
	 * This method will show storage warning configure dialog
	 */
	private void showWarningConfigureDialog() {
		// TODO Auto-generated method stub

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
		if (null != path) {
			ToastHelper.showToast(getActivity(), String.format(
					getString(R.string.pathconfigure_sucess), path));

			location_config.setText(path);
		}
	}

	@Override
	public void onNetworkConfigured(NetworkType networkType) {
		setNetworkTypeString(networkType);
	}

	/**
	 * This method will set network type string based on the network type
	 * 
	 * @param networkType
	 *            to set as string
	 */
	private void setNetworkTypeString(NetworkType networkType) {
		switch (networkType) {
		case WIFI:
			network_config.setText(getString(R.string.title_network_wifi));
			break;
		case ANY:
			network_config.setText(getString(R.string.title_network_any));
			break;
		}
	}
}
