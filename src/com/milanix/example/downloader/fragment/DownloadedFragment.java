package com.milanix.example.downloader.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;
import com.milanix.example.downloader.fragment.abs.AbstractDownloadFragment;
import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.service.DownloadService;

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
				case R.id.action_selectall:
					if (selectAllItems())
						mode.finish();

					return true;
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(android.view.ActionMode mode,
					Menu menu) {
				getActivity().onWindowStartingActionMode(this);
				getActivity().onActionModeStarted(mode);

				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.menu_context_downloaded, menu);

				return true;
			}

			@Override
			public void onDestroyActionMode(android.view.ActionMode mode) {
				getActivity().onActionModeFinished(mode);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = downloadListAdapter.getCursor();

		if (cursor.moveToPosition(position)) {
			final Bundle bundle = new Bundle();
			bundle.putString(DownloadService.KEY_FILE_PATH, cursor
					.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_PATH)));

			DownloadService.viewFile(bundle);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String selection = TextUtils.isEmpty(PreferenceHelper
				.getFilterType(getActivity())) ? QueryHelper
				.getWhereSelection(new String[] { DownloadsDatabase.COLUMN_STATE })
				: QueryHelper.getWhereSelection(new String[] {
						DownloadsDatabase.COLUMN_STATE,
						DownloadsDatabase.COLUMN_TYPE });
		final String[] selectionArgs = TextUtils.isEmpty(PreferenceHelper
				.getFilterType(getActivity())) ? new String[] { DownloadState.COMPLETED
				.toString() } : new String[] {
				DownloadState.COMPLETED.toString(),
				PreferenceHelper.getFilterType(getActivity()) };

		return new CursorLoader(getActivity(),
				DownloadContentProvider.CONTENT_URI_DOWNLOADS, null, selection,
				selectionArgs, QueryHelper.getOrdering(
						PreferenceHelper.getSortOrderingField(getActivity()),
						PreferenceHelper.getSortOrderingType(getActivity())));
	}

}