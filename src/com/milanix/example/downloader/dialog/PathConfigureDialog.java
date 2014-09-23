package com.milanix.example.downloader.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.view.NavigationView;
import com.milanix.example.downloader.view.NavigationView.NavigationTab;
import com.milanix.example.downloader.view.NavigationView.OnNavigationTabClickListener;

/**
 * This dialog allows user to configure download path
 * 
 * @author Milan
 * 
 */
public class PathConfigureDialog extends DialogFragment {

	private NavigationView navigationView;

	private OnPathConfigureListener onPathConfigureListener;

	private String downloadPath = "/mnt/Download";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_path, null);

		setUI(rootView);
		setListener();

		return new AlertDialog.Builder(getActivity())
				.setView(rootView)
				.setTitle(R.string.pathconfigure_title)
				.setIcon(R.drawable.ic_icon_path)
				.setPositiveButton(R.string.btn_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// PreferenceHelper.setDownloadPath(getActivity(),
								// downloadPath);

								onPathConfigureListener
										.onPathConfigured(downloadPath);
							}
						})
				.setNegativeButton(R.string.btn_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dismiss();
							}
						}).create();
	}

	/**
	 * This method will set UI components
	 */
	private void setUI(View rootView) {
		navigationView = (NavigationView) rootView
				.findViewById(R.id.navigationView);

		addDummyTabs();
	}

	/**
	 * This method will add dummy tabs
	 */
	private void addDummyTabs() {
		for (int i = 0; i < 10; i++) {
			final StringBuilder folderNameBuilder = new StringBuilder("Folder ");
			folderNameBuilder.append(i);

			NavigationTab tab = new NavigationTab(folderNameBuilder.toString(),
					new OnNavigationTabClickListener() {

						@Override
						public void OnNavigationTabClicked(
								NavigationTab navigationTab,
								Object attachedObject) {
							Toast.makeText(getActivity(),
									folderNameBuilder.toString(),
									Toast.LENGTH_SHORT).show();
						}
					}, null);

			navigationView.addNavigationTab(tab);
		}

	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onPathConfigureListener = (OnPathConfigureListener) getTargetFragment();
		} catch (ClassCastException e) {
			throw new ClassCastException(getTargetFragment().getTag()
					+ " must implement OnPathConfigureListener");
		}
	}

	/**
	 * This is an call back interface for the caller
	 * 
	 * @author Milan
	 * 
	 */
	public interface OnPathConfigureListener {

		/**
		 * THis method will be called when the user confirms location
		 * configuration
		 * 
		 * 
		 * @param path
		 *            is the download path
		 * 
		 */
		public void onPathConfigured(String path);
	}

}
