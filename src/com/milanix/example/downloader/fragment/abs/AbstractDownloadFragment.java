package com.milanix.example.downloader.fragment.abs;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.dialog.DeleteDownloadDialog;
import com.milanix.example.downloader.dialog.DeleteDownloadDialog.OnDeleteDownloadListener;
import com.milanix.example.downloader.fragment.DownloadedFragment;
import com.milanix.example.downloader.fragment.adapter.DownloadListAdapter;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This is an abstract download framgnet
 * 
 * @author Milan
 * 
 */
public abstract class AbstractDownloadFragment extends AbstractFragment
		implements OnDeleteDownloadListener {

	protected View rootView;
	protected ListView downloading_list;
	protected DownloadListAdapter adapter;

	public AbstractDownloadFragment() {
		super();
	}

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

		onInit();

		return rootView;
	}

	/**
	 * Called to init view components
	 */
	protected void onInit() {
		setUI();
		setListener();
		setAdapter(getDownloads());
	}

	@Override
	public void setUI() {
		downloading_list = (ListView) rootView
				.findViewById(R.id.downloading_list);
	}

	@Override
	public void setListener() {
		downloading_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		downloading_list
				.setMultiChoiceModeListener(getMultiChoiceModeListener());
	}

	/**
	 * This method will set adapter
	 * 
	 * @param cursor
	 */
	protected void setAdapter(Cursor cursor) {
		if (null != cursor) {
			adapter = new DownloadListAdapter(getActivity(), cursor, false);

			downloading_list.setAdapter(adapter);
		}
	}

	/**
	 * This method will set new cursor to the adapter
	 */
	protected void refreshAdapter() {
		if (null != adapter) {
			adapter.changeCursor(getDownloads());

			ToastHelper.showToast(getActivity(), "Refreshing list");
		}
	}

	/**
	 * This method will refresh adapter on ui thread
	 */
	protected void refreshAdapterOnUIThread() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				refreshAdapter();
			}

		});
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
	 * This method will show add new download dialog
	 */
	protected void showRemoveDialog(long id, String url) {
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

	/**
	 * This method will remove given ids from the database
	 * 
	 * @param position
	 */
	protected void removeDownload(int position) {
		Cursor cursor = adapter.getCursor();

		if (cursor.moveToPosition(position)) {
			showRemoveDialog(cursor.getLong(cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_ID)),
					cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_URL)));
		}
	}

	@Override
	public String getLogTag() {
		return DownloadedFragment.class.getSimpleName();
	}

	/**
	 * This method will return cursor to be set to the adapter
	 * 
	 * @return Cursor populated with data
	 */
	protected abstract Cursor getDownloads();

	/**
	 * This method will return MultiChoiceModeListener
	 * 
	 * @return MultiChoiceModeListener
	 */
	protected abstract MultiChoiceModeListener getMultiChoiceModeListener();

}