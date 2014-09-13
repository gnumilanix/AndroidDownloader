package com.milanix.example.downloader.fragment;

import java.util.Collection;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.milanix.example.downloader.HomeActivity;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadListener;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.dialog.AddNewDownloadDialog;
import com.milanix.example.downloader.dialog.AddNewDownloadDialog.OnAddNewDownloadListener;
import com.milanix.example.downloader.dialog.DeleteDownloadDialog;
import com.milanix.example.downloader.dialog.DeleteDownloadDialog.OnDeleteDownloadListener;
import com.milanix.example.downloader.fragment.abs.AbstractFragment;
import com.milanix.example.downloader.fragment.adapter.DownloadListAdapter;
import com.milanix.example.downloader.service.DownloadService;
import com.milanix.example.downloader.service.DownloadService.DownloadBinder;
import com.milanix.example.downloader.util.FileUtils;
import com.milanix.example.downloader.util.NetworkUtils;
import com.milanix.example.downloader.util.PreferenceHelper;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This fragment contains downloading list and its related logic.
 */
public class DownloadingFragment extends AbstractFragment implements
		OnItemClickListener, OnItemLongClickListener, OnAddNewDownloadListener,
		OnDeleteDownloadListener {

	private View rootView;
	private ListView downloading_list;

	private DownloadListAdapter adapter;

	private HashMap<Integer, AsyncTask<String, Integer, Download>> downloadTasks = new HashMap<Integer, AsyncTask<String, Integer, Download>>();

	private DownloadService downloadService = null;

	private boolean bound = false;

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			downloadService = ((DownloadBinder) service).getService();

			bound = true;

			setAdapter(getDownloadingDownloads());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater
				.inflate(R.layout.downloading_home, container, false);

		setUI();
		setListener();

		return rootView;
	}

	@Override
	public void setUI() {
		downloading_list = (ListView) rootView
				.findViewById(R.id.downloading_list);
	}

	@Override
	public void setListener() {
		downloading_list.setOnItemClickListener(this);
		downloading_list.setOnItemLongClickListener(this);
	}

	/**
	 * This method will set adapter
	 * 
	 * @param cursor
	 */
	private void setAdapter(Cursor cursor) {
		if (null != cursor) {
			adapter = new DownloadListAdapter(getActivity(), cursor, false,
					downloadService);

			downloading_list.setAdapter(adapter);
		}
	}

	/**
	 * This method will set new cursor to the adapter
	 */
	private void refreshAdapter() {
		if (null != adapter) {
			adapter.changeCursor(getDownloadingDownloads());

			ToastHelper.showToast(getActivity(), "Refreshing list");
		}
	}

	/**
	 * This method will get downloads from the database and set to the adapter
	 * 
	 * @return cursor retrieved if successful otherwise null
	 */
	private Cursor getDownloadingDownloads() {
		if (getActivity() instanceof HomeActivity) {
			return ((HomeActivity) getActivity()).getDatabase().query(
					DownloadsDatabase.TABLE_DOWNLOADS,
					null,
					QueryHelper.getWhere(DownloadsDatabase.COLUMN_STATE,
							DownloadState.COMPLETED.toString(), false),
					null,
					null,
					null,
					QueryHelper.getOrdering(DownloadsDatabase.COLUMN_ID,
							QueryHelper.ORDERING_DESC), null);
		} else {
			return null;
		}
	}

	/**
	 * This method will show add new download dialog
	 */
	private void showAddNewDialog() {
		DialogFragment newFragment = new AddNewDownloadDialog();
		newFragment.setTargetFragment(this, -1);
		newFragment.setCancelable(true);
		newFragment.show(getFragmentManager(),
				AddNewDownloadDialog.class.getSimpleName());
	}

	/**
	 * This method will show add new download dialog
	 */
	private void showRemoveDialog(long id, String url) {
		Bundle bundle = new Bundle();
		bundle.putLong(DeleteDownloadDialog.KEY_DOWNLOADID, id);
		bundle.putString(DeleteDownloadDialog.KEY_DOWNLOADURL, url);

		DeleteDownloadDialog newFragment = new DeleteDownloadDialog();
		newFragment.setArguments(bundle);
		newFragment.setTargetFragment(this, -1);
		newFragment.setCancelable(true);
		newFragment.show(getFragmentManager(),
				DeleteDownloadDialog.class.getSimpleName());
	}

	@Override
	public String getLogTag() {
		return DownloadingFragment.class.getSimpleName();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Cursor cursor = adapter.getCursor();

		if (cursor.moveToPosition(position)) {
			showRemoveDialog(cursor.getLong(cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_ID)),
					cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_URL)));

			return true;
		}

		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_downloading, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_refresh:
			refreshAdapter();

			return true;
		case R.id.action_add:
			showAddNewDialog();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onNewDownloadAdded(Download download) {
		if (null != download) {
			ToastHelper.showToast(getActivity(),
					String.format(getString(R.string.download_add_success),
							download.getUrl()));

			refreshAdapter();

			if (NetworkUtils.isNetworkConnected(getActivity()))
				pushDownloadToService(download);
			else
				ToastHelper.showToast(getActivity(),
						getString(R.string.download_disconnected));
		} else
			ToastHelper.showToast(getActivity(),
					getString(R.string.download_add_fail));
	}

	@Override
	public void onDownloadDeleted(boolean isSuccess, String url) {
		if (isSuccess) {
			ToastHelper.showToast(getActivity(), String.format(
					getString(R.string.download_delete_success), url));

			refreshAdapter();
		} else
			ToastHelper.showToast(getActivity(), String.format(
					getString(R.string.download_delete_fail), url));
	}

	/**
	 * This method will push download to the service
	 * 
	 * @param download
	 */
	private void pushDownloadToService(Download download) {
		if (bound) {
			download.setPath(FileUtils.getLocalDownloadPath(
					PreferenceHelper.getDownloadPath(getActivity()),
					download.getUrl()));

			downloadService.attachCallback(download.getId(),
					new DownloadListener() {

						@Override
						public void onDownloadStarted(Download download) {
							Log.d(getLogTag(),
									"Download started " + download.getName());

							updateDownloadState(download,
									DownloadState.DOWNLOADING);
						}

						@Override
						public void onDownloadCancelled(Download download) {
							Log.d(getLogTag(),
									"Download cancelled " + download.getName());

							updateDownloadState(download,
									DownloadState.CANCELLED);
						}

						@Override
						public void onDownloadCompleted(Download download) {
							Log.d(getLogTag(),
									"Download completed " + download.getName());

							updateDownloadState(download, download.getState());
						}

						@Override
						public void onDownloadFailed(Download download) {
							Log.d(getLogTag(),
									"Download failed " + download.getName());

							updateDownloadState(download, DownloadState.FAILED);
						}

						@Override
						public void onDownloadProgress(Download download,
								Integer progress) {
						}
					});

			downloadTasks.put(download.getId(),
					downloadService.downloadFile(download));
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		getActivity().bindService(
				new Intent(getActivity(), DownloadService.class), connection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();

		if (bound) {
			getActivity().unbindService(connection);

			bound = false;
		}
	}

	@Override
	public void onDestroy() {
		cancelOngoingTasks();

		super.onDestroy();
	}

	/**
	 * This method will cancel all ongoing tasks
	 */
	private void cancelOngoingTasks() {
		Collection<AsyncTask<String, Integer, Download>> ongoingTasks = downloadTasks
				.values();

		for (AsyncTask<String, Integer, Download> task : ongoingTasks) {
			if (null != task) {
				task.cancel(true);
			}
		}
	}

	/**
	 * This method will update the given download state
	 * 
	 * @param download
	 *            is the download object
	 * @state is the download state
	 */
	private void updateDownloadState(Download download, DownloadState state) {
		if (getActivity() instanceof HomeActivity) {
			ContentValues values = new ContentValues();
			values.put(DownloadsDatabase.COLUMN_PATH, download.getPath());
			values.put(DownloadsDatabase.COLUMN_STATE, state.toString());

			if (0 != ((HomeActivity) getActivity()).getDatabase().update(
					DownloadsDatabase.TABLE_DOWNLOADS,
					values,
					QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID,
							download.getId(), true), null)) {
				refreshAdapter();
			}
		}

	}
}