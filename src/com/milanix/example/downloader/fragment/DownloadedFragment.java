package com.milanix.example.downloader.fragment;

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.milanix.example.downloader.HomeActivity;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.dialog.DeleteDownloadDialog;
import com.milanix.example.downloader.dialog.DeleteDownloadDialog.OnDeleteDownloadListener;
import com.milanix.example.downloader.fragment.abs.AbstractFragment;
import com.milanix.example.downloader.fragment.adapter.DownloadListAdapter;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This fragment contains downloaded list and its related logic.
 */
public class DownloadedFragment extends AbstractFragment implements
		OnItemClickListener, OnItemLongClickListener, OnDeleteDownloadListener {

	private View rootView;
	private ListView downloading_list;

	private DownloadListAdapter adapter;

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
		setAdapter(getDownloadedDownloads());

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
			adapter = new DownloadListAdapter(getActivity(), cursor, false);

			downloading_list.setAdapter(adapter);
		}
	}

	/**
	 * This method will set new cursor to the adapter
	 */
	private void refreshAdapter() {
		if (null != adapter) {
			adapter.changeCursor(getDownloadedDownloads());

			ToastHelper.showToast(getActivity(), "Refreshing list");
		}
	}

	/**
	 * This method will get downloads from the database and set to the adapter
	 * 
	 * @return cursor retrieved if successful otherwise null
	 */
	private Cursor getDownloadedDownloads() {
		if (getActivity() instanceof HomeActivity) {
			return ((HomeActivity) getActivity()).getDatabase().query(
					DownloadsDatabase.TABLE_DOWNLOADS,
					null,
					QueryHelper.getWhere(DownloadsDatabase.COLUMN_STATE,
							DownloadState.COMPLETED.toString(), true),
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

	/**
	 * This method will start activity with action_view intent
	 */
	private void viewFile(String filePath) {
		File file = new File(filePath);

		String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

		if (type == null)
			type = "*/*";

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), type);

		startActivity(intent);
	}

	@Override
	public String getLogTag() {
		return DownloadedFragment.class.getSimpleName();
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
		Cursor cursor = adapter.getCursor();

		if (cursor.moveToPosition(position)) {
			viewFile(cursor.getString(cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_PATH)));
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_downloaded, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_refresh:
			refreshAdapter();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
}