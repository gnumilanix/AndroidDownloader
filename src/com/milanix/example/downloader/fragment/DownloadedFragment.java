package com.milanix.example.downloader.fragment;

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;
import com.milanix.example.downloader.fragment.abs.AbstractDownloadFragment;
import com.milanix.example.downloader.util.PreferenceHelper;

/**
 * This fragment contains downloaded list and its related logic.
 */
public class DownloadedFragment extends AbstractDownloadFragment {

	@Override
	public MultiChoiceModeListener getMultiChoiceModeListener() {
		return new MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(android.view.ActionMode mode,
					MenuItem item) {
				switch (item.getItemId()) {
				case R.id.action_delete:
					removeDownloads(downloading_list.getCheckedItemIds());

					mode.finish();

					return true;
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(android.view.ActionMode mode,
					Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.menu_context_downloaded, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(android.view.ActionMode mode) {

			}

			@Override
			public boolean onPrepareActionMode(android.view.ActionMode mode,
					Menu menu) {
				return false;
			}

			@Override
			public void onItemCheckedStateChanged(android.view.ActionMode mode,
					int position, long id, boolean checked) {

			}
		};
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = downloadListAdapter.getCursor();

		if (cursor.moveToPosition(position)) {
			viewFile(cursor.getString(cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_PATH)));
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(),
				DownloadContentProvider.CONTENT_URI_DOWNLOADS, null,
				QueryHelper.getWhere(DownloadsDatabase.COLUMN_STATE,
						DownloadState.COMPLETED.toString(), true), null,
				QueryHelper.getOrdering(
						PreferenceHelper.getSortOrderingField(getActivity()),
						PreferenceHelper.getSortOrderingType(getActivity())));
	}

}