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
import com.milanix.example.downloader.service.DownloadService.TaskStateResult;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This fragment contains downloading list and its related logic.
 */
public class DownloadingFragment extends AbstractDownloadFragment {

	/**
	 * This method will resume downloads
	 * 
	 * @param selectedIds
	 */
	private void resumeDownloads(long[] selectedIds) {
		if (isBound()) {
			TaskStateResult result = DownloadService
					.resumeDownload(selectedIds);

			if (null != result && null != result.getFreshTasks()
					&& !result.getFreshTasks().isEmpty()) {
				ToastHelper.showToast(
						getActivity(),
						getResources().getQuantityString(
								R.plurals.download_fresh_added,
								result.getFreshTasks().size(),
								result.getFreshTasks().size()));
			}
		}
	}

	/**
	 * This method will pause downloads
	 */
	private void pauseDownloads(long[] selectedIds) {
		if (isBound()) {
			DownloadService.pauseDownload(selectedIds);
		}
	}

	@Override
	public MultiChoiceModeListener getMultiChoiceModeListener() {
		return new MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(android.view.ActionMode mode,
					MenuItem item) {
				switch (item.getItemId()) {
				case R.id.action_resume:
					resumeDownloads(downloading_list.getCheckedItemIds());

					mode.finish();

					return true;
				case R.id.action_pause:
					pauseDownloads(downloading_list.getCheckedItemIds());

					mode.finish();

					return true;
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
				inflater.inflate(R.menu.menu_context_downloading, menu);

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
		downloadListAdapter.setExpanded(position);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String selection = TextUtils.isEmpty(PreferenceHelper
				.getFilterType(getActivity())) ? DownloadsDatabase.COLUMN_STATE
				+ " !=?" : DownloadsDatabase.COLUMN_STATE + " !=? AND "
				+ DownloadsDatabase.COLUMN_TYPE + " =?";
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