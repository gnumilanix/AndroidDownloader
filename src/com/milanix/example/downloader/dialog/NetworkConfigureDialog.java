package com.milanix.example.downloader.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.util.PreferenceHelper;

/**
 * This dialog allows user to network configure downloads
 * 
 * @author Milan
 * 
 */
public class NetworkConfigureDialog extends DialogFragment {
	/**
	 * Enum to define network type
	 * 
	 * @author Milan
	 * 
	 */
	public static enum NetworkType {
		WIFI, MOBILE, BOTH
	}

	private OnNetworkConfigureListener onNetworkConfigureListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.networkconfigure_title)
				.setIcon(R.drawable.ic_icon_network)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.networktype),
						getNetworkTypePosition(PreferenceHelper
								.getDownloadNetwork(getActivity())),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								switch (which) {
								case 2:
									PreferenceHelper.setDownloadNetwork(
											getActivity(), NetworkType.BOTH);

									onNetworkConfigureListener
											.onNetworkConfigured(NetworkType.BOTH);
									break;
								case 1:
									PreferenceHelper.setDownloadNetwork(
											getActivity(), NetworkType.MOBILE);

									onNetworkConfigureListener
											.onNetworkConfigured(NetworkType.MOBILE);
									break;
								default:
									PreferenceHelper.setDownloadNetwork(
											getActivity(), NetworkType.WIFI);

									onNetworkConfigureListener
											.onNetworkConfigured(NetworkType.WIFI);
									break;
								}

								dismiss();
							}

						}).create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onNetworkConfigureListener = (OnNetworkConfigureListener) getTargetFragment();
		} catch (ClassCastException e) {
			throw new ClassCastException(getTargetFragment().getTag()
					+ " must implement OnNetworkConfigureListener");
		}
	}

	/**
	 * This method will get network type position from network type
	 * 
	 * @param type
	 *            networktype
	 * @return position in an array
	 */
	private int getNetworkTypePosition(NetworkType type) {
		switch (type) {
		case BOTH:
			return 2;
		case MOBILE:
			return 1;
		default:
			return 0;

		}
	}

	/**
	 * This is an call back interface for the caller
	 * 
	 * @author Milan
	 * 
	 */
	public interface OnNetworkConfigureListener {

		/**
		 * THis method will be called when the user confirms pool configuration
		 * 
		 * 
		 * @param networkType
		 *            is the networkType
		 * 
		 */
		public void onNetworkConfigured(NetworkType networkType);
	}
}
