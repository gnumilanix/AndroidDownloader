package com.milanix.example.downloader.fragment;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.dialog.LimitConfigureDialog;
import com.milanix.example.downloader.dialog.LimitConfigureDialog.OnLimitConfigureListener;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog.NetworkType;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog.OnNetworkConfigureListener;
import com.milanix.example.downloader.dialog.PathConfigureDialog;
import com.milanix.example.downloader.dialog.PathConfigureDialog.OnPathConfigureListener;
import com.milanix.example.downloader.dialog.PoolConfigureDialog;
import com.milanix.example.downloader.dialog.PoolConfigureDialog.OnPoolConfigureListener;
import com.milanix.example.downloader.fragment.abs.AbstractFragment;
import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.util.FileUtils.ByteType;

/**
 * This fragment contains settings configuration.
 */
public class SettingsFragment extends AbstractFragment implements
		View.OnClickListener, OnCheckedChangeListener, OnPoolConfigureListener,
		OnPathConfigureListener, OnNetworkConfigureListener,
		OnLimitConfigureListener {

	private View rootView;

	private SwitchCompat autostart_switch;

	private CheckBox aggregate_notification;

	private ViewGroup location_base;
	private ViewGroup tasks_base;
	private ViewGroup limit_base;
	private ViewGroup network_base;

	private TextView location_config;
	private TextView tasks_config;
	private TextView limit_config;
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

		setData();

		return rootView;
	}

	@Override
	protected void setUI() {
		autostart_switch = (SwitchCompat) rootView
				.findViewById(R.id.autostart_switch);

		aggregate_notification = (CheckBox) rootView
				.findViewById(R.id.aggregate_notification);

		location_base = (ViewGroup) rootView.findViewById(R.id.location_base);
		tasks_base = (ViewGroup) rootView.findViewById(R.id.tasks_base);
		limit_base = (ViewGroup) rootView.findViewById(R.id.limit_base);
		network_base = (ViewGroup) rootView.findViewById(R.id.network_base);

		location_config = (TextView) rootView
				.findViewById(R.id.location_config);
		tasks_config = (TextView) rootView.findViewById(R.id.tasks_config);
		limit_config = (TextView) rootView.findViewById(R.id.limit_config);
		network_config = (TextView) rootView.findViewById(R.id.network_config);
	}

	@Override
	protected void setListener() {
		autostart_switch.setOnCheckedChangeListener(this);
		aggregate_notification.setOnCheckedChangeListener(this);

		location_base.setOnClickListener(this);
		tasks_base.setOnClickListener(this);
		network_base.setOnClickListener(this);
		limit_base.setOnClickListener(this);
	}

	/**
	 * This method will set the UI data
	 */
	private void setData() {
		autostart_switch.setChecked(PreferenceHelper
				.getIsAutoStart(getActivity()));
		aggregate_notification.setChecked(PreferenceHelper
				.getIsAggregateDownload(getActivity()));

		location_config
				.setText(PreferenceHelper.getDownloadPath(getActivity()));
		tasks_config.setText(getResources().getQuantityString(
				R.plurals.tasks_configured,
				PreferenceHelper.getDownloadPoolSize(getActivity()),
				PreferenceHelper.getDownloadPoolSize(getActivity())));
		limit_config.setText(String.format(
				getString(R.string.title_size_configured),
				PreferenceHelper.getDownloadLimitSize(getActivity()),
				PreferenceHelper.getDownloadLimitType(getActivity())));

		setNetworkTypeString(PreferenceHelper.getDownloadNetwork(getActivity()));
	}

	@Override
	public String getLogTag() {
		return SettingsFragment.class.getSimpleName();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.autostart_switch) {
			PreferenceHelper.setIsAutoStart(getActivity(), isChecked);
		} else if (buttonView.getId() == R.id.aggregate_notification) {
			PreferenceHelper.setIsAggregateDownload(getActivity(), isChecked);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.location_base) {
			showPathConfigureDialog();
		} else if (view.getId() == R.id.tasks_base) {
			showTaskConfigureDialog();
		} else if (view.getId() == R.id.network_base) {
			showNetworkConfigureDialog();
		} else if (view.getId() == R.id.limit_base) {
			showlimitConfigureDialog();
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
	 * This method will show storage limit configure dialog
	 */
	private void showlimitConfigureDialog() {
		LimitConfigureDialog limitConfigureDialog = new LimitConfigureDialog();
		limitConfigureDialog.setTargetFragment(this, -1);
		limitConfigureDialog.setCancelable(true);
		limitConfigureDialog.show(getFragmentManager(),
				LimitConfigureDialog.class.getSimpleName());
	}

	@Override
	public void onPoolConfigured(Integer poolSize) {
		tasks_config.setText(getResources().getQuantityString(
				R.plurals.tasks_configured,
				PreferenceHelper.getDownloadPoolSize(getActivity()),
				PreferenceHelper.getDownloadPoolSize(getActivity())));
	}

	@Override
	public void onPathConfigured(String path) {
		location_config
				.setText(PreferenceHelper.getDownloadPath(getActivity()));
	}

	@Override
	public void onNetworkConfigured(NetworkType networkType) {
		setNetworkTypeString(PreferenceHelper.getDownloadNetwork(getActivity()));
	}

	@Override
	public void onLimitConfigured(Integer size, ByteType type) {
		limit_config.setText(String.format(
				getString(R.string.title_size_configured),
				PreferenceHelper.getDownloadLimitSize(getActivity()),
				PreferenceHelper.getDownloadLimitType(getActivity())));
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
