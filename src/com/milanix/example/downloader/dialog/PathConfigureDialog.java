package com.milanix.example.downloader.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.dialog.adapter.FolderAdapter;
import com.milanix.example.downloader.util.FileUtils;
import com.milanix.example.downloader.util.PreferenceHelper;
import com.milanix.example.downloader.view.NavigationView;
import com.milanix.example.downloader.view.NavigationView.NavigationTab;
import com.milanix.example.downloader.view.NavigationView.OnNavigationTabClickListener;

/**
 * This dialog allows user to configure download path
 * 
 * @author Milan
 * 
 */
public class PathConfigureDialog extends DialogFragment implements
		OnItemClickListener {

	private NavigationView navigation_folder;
	private ListView list_folder;

	private FolderAdapter folderAdapter;

	private OnPathConfigureListener onPathConfigureListener;

	private String downloadPath;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_path, null);

		setUI(rootView);
		setAdapter();
		setListener();

		return new AlertDialog.Builder(getActivity())
				.setView(rootView)
				.setTitle(R.string.pathconfigure_title)
				.setIcon(R.drawable.ic_icon_path)
				.setPositiveButton(R.string.btn_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								downloadPath = folderAdapter.getRootFile().getAbsolutePath();

								PreferenceHelper.setDownloadPath(getActivity(),
										downloadPath);

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
		navigation_folder = (NavigationView) rootView
				.findViewById(R.id.navigation_folder);
		list_folder = (ListView) rootView.findViewById(R.id.list_folder);
	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
		list_folder.setOnItemClickListener(this);
	}

	/**
	 * This method will set adapter
	 */
	private void setAdapter() {
		folderAdapter = new FolderAdapter(getActivity());

		list_folder.setAdapter(folderAdapter);

		setPathView(getDownloadPath());
	}

	/**
	 * This method will set current path view
	 */
	private void setPathView(File file) {
		if (null != folderAdapter) {
			folderAdapter.setRootFile(file);

			setPathTabs(file);
		}
	}

	/**
	 * This method will get download path. If does not exist will try to create
	 * a default in /root/Download, if failed will try to set to /root. If
	 * successful will return a Folder otherwise null
	 */
	private File getDownloadPath() {
		downloadPath = PreferenceHelper.getDownloadPath(getActivity());

		File root = new File(downloadPath);

		if (!root.exists()) {
			File extRoot = Environment.getExternalStorageDirectory();

			StringBuilder downloadPathBuilder = new StringBuilder(
					extRoot.getAbsolutePath());
			downloadPathBuilder.append("/Download");

			if (FileUtils.isStorageWritable()) {
				File defaultDownload = new File(downloadPathBuilder.toString());

				if (!defaultDownload.exists()) {
					if (defaultDownload.mkdir()) {
						root = defaultDownload;

					} else {
						root = extRoot;
					}
				} else {
					root = extRoot;
				}
			} else {
				root = null;
			}
		}

		if (null != root)
			PreferenceHelper.setDownloadPath(getActivity(),
					root.getAbsolutePath());

		return root;
	}

	/**
	 * This method will set tabs for the path
	 * 
	 * @param file
	 *            path to create tab over
	 */
	private void setPathTabs(File file) {
		if (null != file && file.isDirectory()) {

			navigation_folder.clearNavigationTabs();

			ArrayList<File> tabFolders = new ArrayList<File>();

			File parent = file;

			while (null != parent.getParentFile()) {
				tabFolders.add(parent);

				parent = parent.getParentFile();
			}

			Collections.reverse(tabFolders);

			for (File tabFolder : tabFolders) {
				addTab(tabFolder);
			}
		}
	}

	/**
	 * This method will add dummy tabs
	 * 
	 * @param file
	 *            is a folder
	 */
	private void addTab(File file) {
		NavigationTab tab = new NavigationTab(file.getName(),
				new OnNavigationTabClickListener() {

					@Override
					public void OnNavigationTabClicked(
							NavigationTab navigationTab, Object attachedObject) {
						if (attachedObject instanceof File)
							setPathView((File) attachedObject);
					}
				}, file);

		navigation_folder.addNavigationTab(tab);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		File navigateToFolder = folderAdapter.getFolder(position);

		setPathView(navigateToFolder);
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
