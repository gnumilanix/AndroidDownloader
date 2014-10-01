package com.milanix.example.downloader.fragment.abs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadListener;
import com.milanix.example.downloader.data.dao.Download.FailedReason;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;
import com.milanix.example.downloader.dialog.AddNewDownloadDialog;
import com.milanix.example.downloader.dialog.AddNewDownloadDialog.OnAddNewDownloadListener;
import com.milanix.example.downloader.dialog.DeleteDownloadDialog;
import com.milanix.example.downloader.dialog.DeleteDownloadDialog.OnDeleteDownloadListener;
import com.milanix.example.downloader.dialog.NetworkConfigureDialog;
import com.milanix.example.downloader.dialog.SortConfigureDialog;
import com.milanix.example.downloader.fragment.DownloadedFragment;
import com.milanix.example.downloader.fragment.adapter.DownloadListAdapter;
import com.milanix.example.downloader.service.DownloadService;
import com.milanix.example.downloader.service.DownloadService.DownloadBinder;
import com.milanix.example.downloader.util.NetworkUtils;
import com.milanix.example.downloader.util.PreferenceHelper;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This is an abstract download framgnet
 * 
 * @author Milan
 * 
 */
public abstract class AbstractDownloadFragment extends AbstractFragment
		implements OnDeleteDownloadListener, OnAddNewDownloadListener,
		OnItemClickListener, OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int HANDLE_REFRESH_ADAPTER = 0;

	private static final String HANDLE_KEY_ISSILENT = "key_issilent";

	private DownloadContentObserver downloadContentObserver;

	protected View rootView;
	protected GridView downloading_list;
	protected ImageButton download_add;
	protected DownloadListAdapter downloadListAdapter;

	// Don't allow these fields to be changed by child classes
	private SharedPreferences sharedPreferences;
	private OnSharedPreferenceChangeListener sharedPrefChangeListener;

	private Handler uiUpdateHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_REFRESH_ADAPTER:
				if (null != msg && null != msg.getData()
						&& msg.getData().containsKey(HANDLE_KEY_ISSILENT))
					refreshCursorLoader(msg.getData().containsKey(
							HANDLE_KEY_ISSILENT));
				else
					refreshCursorLoader(false);

				break;
			default:
				super.handleMessage(msg);
			}
		}
	};

	private DownloadService downloadService = null;
	private boolean bound = false;

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			downloadService = ((DownloadBinder) service).getService();

			bound = true;

			setAdapter();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}
	};

	public AbstractDownloadFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		setRetainInstance(true);

		registerPreferenceChangeListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_download, container,
				false);

		onInit();

		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterPreferenceChangeListener();
	}

	@Override
	public void onPause() {
		registerContentObserver();

		super.onPause();
	}

	@Override
	public void onResume() {
		unregisterContentObserver();

		super.onResume();
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

	/**
	 * Called to init view components
	 */
	@Override
	protected void onInit() {
		super.onInit();
	}

	/**
	 * This method will register preference change listener
	 */
	private void registerPreferenceChangeListener() {
		if (null == sharedPreferences)
			sharedPreferences = PreferenceHelper
					.getPreferenceInstance(getActivity());

		if (null == sharedPrefChangeListener)
			sharedPrefChangeListener = new OnSharedPreferenceChangeListener() {

				@Override
				public void onSharedPreferenceChanged(
						SharedPreferences sharedPreference, String key) {
					if (PreferenceHelper.KEY_ORDERING_FIELD.equals(key)) {
						refreshCursorLoader(true);
					} else if (PreferenceHelper.KEY_ORDERING_TYPE.equals(key)) {
						refreshCursorLoader(true);
					}
				}
			};

		sharedPreferences
				.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener);

	}

	/**
	 * This method will unregister preference change listener
	 */
	private void unregisterPreferenceChangeListener() {
		if (null == sharedPreferences && null != sharedPrefChangeListener)
			sharedPreferences
					.unregisterOnSharedPreferenceChangeListener(sharedPrefChangeListener);
	}

	@Override
	protected void setUI() {
		downloading_list = (GridView) rootView
				.findViewById(R.id.downloading_list);
		download_add = (ImageButton) rootView.findViewById(R.id.download_add);
	}

	@Override
	protected void setListener() {
		downloading_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		downloading_list
				.setMultiChoiceModeListener(getMultiChoiceModeListener());
		downloading_list.setOnItemClickListener(this);

		download_add.setOnClickListener(this);
	}

	/**
	 * This method will set adapter and init loader
	 * 
	 * @param cursor
	 */
	protected void setAdapter() {
		downloadListAdapter = new DownloadListAdapter(getActivity(), null,
				false, downloadService);

		downloading_list.setAdapter(downloadListAdapter);

		getLoaderManager().initLoader(0, null, this);
	}

	/**
	 * This method will register downloads content observer
	 */
	private void registerContentObserver() {
		if (null == downloadContentObserver)
			downloadContentObserver = new DownloadContentObserver(new Handler());

		getActivity().getContentResolver().registerContentObserver(
				DownloadContentProvider.CONTENT_URI_DOWNLOADS, true,
				downloadContentObserver);
	}

	/**
	 * This method will unregister downloads content observer
	 */
	private void unregisterContentObserver() {
		if (null != downloadContentObserver)
			getActivity().getContentResolver().unregisterContentObserver(
					downloadContentObserver);
	}

	/**
	 * This method will return if service is bound
	 * 
	 * @return true if the service is bound, otherwise false
	 */
	protected boolean isBound() {
		return bound;
	}

	/**
	 * This method will return download service in this class
	 * 
	 * @return download service instance.
	 */
	protected DownloadService getDownloadService() {
		return downloadService;
	}

	/**
	 * This method will post refresh cursor loader to the handler.
	 * 
	 * @param isSilent
	 *            if refresh should be silent
	 */
	protected void postRefreshCursorLoader(boolean isSilent) {
		Bundle data = new Bundle();
		data.putBoolean(HANDLE_KEY_ISSILENT, isSilent);

		Message msg = Message.obtain(null, HANDLE_REFRESH_ADAPTER);
		msg.setData(data);

		if (null != uiUpdateHandler)
			uiUpdateHandler.sendMessage(msg);
	}

	/**
	 * This method will restart a cursor loader
	 * 
	 * @param isSilent
	 *            if false will notify user otherwise nothing will be displayed
	 */
	protected void refreshCursorLoader(boolean isSilent) {
		if (null != downloadListAdapter) {
			getLoaderManager().restartLoader(0, null, this);

			if (!isSilent)
				ToastHelper.showToast(getActivity(), "Refreshing list");
		}
	}

	@Override
	public void onDownloadDeleted(boolean isSuccess) {
		if (isSuccess) {
			refreshCursorLoader(true);

			ToastHelper.showToast(getActivity(),
					getString(R.string.download_delete_success));
		} else
			ToastHelper.showToast(getActivity(),
					getString(R.string.download_delete_fail));
	}

	@Override
	public void onNewDownloadAdded(Integer id) {
		if (null != id) {
			Download addedDownload = new Download().retrieve(getActivity(), id);

			if (null != addedDownload && null != addedDownload.getUrl()) {
				refreshCursorLoader(false);

				ToastHelper.showToast(getActivity(), String.format(
						getString(R.string.download_add_success),
						addedDownload.getUrl()));

				if (NetworkUtils.isNetworkConnected(getActivity()))
					pushDownloadToService(addedDownload);
				else
					ToastHelper.showToast(getActivity(),
							getString(R.string.download_disconnected));
			} else {
				ToastHelper.showToast(getActivity(),
						getString(R.string.download_add_fail));
			}

		} else
			ToastHelper.showToast(getActivity(),
					getString(R.string.download_add_fail));
	}

	/**
	 * This method will show add new download dialog
	 * 
	 * downloadIds array of ids to delete
	 */
	protected void showRemoveDialog(long[] downloadIds) {
		Bundle bundle = new Bundle();
		bundle.putLongArray(DeleteDownloadDialog.KEY_DOWNLOADIDS, downloadIds);

		DeleteDownloadDialog newFragment = new DeleteDownloadDialog();
		newFragment.setArguments(bundle);
		newFragment.setTargetFragment(this, -1);
		newFragment.setCancelable(true);
		newFragment.show(getFragmentManager(),
				DeleteDownloadDialog.class.getSimpleName());
	}

	/**
	 * This method will show sort configure dialog
	 */
	private void showSortConfigureDialog() {
		SortConfigureDialog sortConfigureDialog = new SortConfigureDialog();
		sortConfigureDialog.setTargetFragment(this, -1);
		sortConfigureDialog.setCancelable(true);
		sortConfigureDialog.show(getFragmentManager(),
				NetworkConfigureDialog.class.getSimpleName());
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
	 * This method will remove given ids from the database
	 * 
	 * @param downloadIds
	 *            array of ids to delete
	 */
	protected void removeDownloads(long[] downloadIds) {
		showRemoveDialog(downloadIds);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.download_add)
			showAddNewDialog();
	}

	@Override
	public String getLogTag() {
		return DownloadedFragment.class.getSimpleName();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_download, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_sort:
			showSortConfigureDialog();

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * This method will push download to the service
	 * 
	 * @param download
	 */
	protected void pushDownloadToService(Download download) {
		if (bound) {
			downloadService.attachCallback(download.getId(),
					new DownloadListener() {

						@Override
						public void onDownloadStarted(Download download) {
							Log.d(getLogTag(),
									"Download started " + download.getName());

							postRefreshCursorLoader(false);
						}

						@Override
						public void onDownloadCancelled(Download download) {
							Log.d(getLogTag(),
									"Download cancelled " + download.getName());

							postRefreshCursorLoader(false);
						}

						@Override
						public void onDownloadCompleted(Download download) {
							Log.d(getLogTag(),
									"Download completed " + download.getName());

							postRefreshCursorLoader(false);
						}

						@Override
						public void onDownloadFailed(Download download,
								FailedReason reason) {
							Log.d(getLogTag(),
									"Download failed " + download.getName());

							postRefreshCursorLoader(false);
						}

						@Override
						public void onDownloadProgress(Download download,
								Integer progress) {
							Log.d(getLogTag(),
									"Download paused " + download.getName());
						}
					});

			DownloadService.downloadFile(download.getId());
		}
	}

	@Override
	public abstract Loader<Cursor> onCreateLoader(int id, Bundle args);

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		downloadListAdapter.swapCursor(newCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		downloadListAdapter.swapCursor(null);
	}

	/**
	 * This method will return MultiChoiceModeListener
	 * 
	 * @return MultiChoiceModeListener
	 */
	protected abstract MultiChoiceModeListener getMultiChoiceModeListener();

	/**
	 * Content observer for {@link DownloadContentProvider}
	 * 
	 * @author Milan
	 * 
	 */
	protected class DownloadContentObserver extends ContentObserver {

		public DownloadContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			postRefreshCursorLoader(false);
		}

		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

	}

}