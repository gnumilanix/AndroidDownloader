package com.milanix.example.downloader.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.milanix.example.downloader.HomeActivity;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;

/**
 * This dialog allows user to delete downloads
 * 
 * @author Milan
 * 
 */
public class DeleteDownloadDialog extends DialogFragment {
	public static final String KEY_DOWNLOADID = "key_downloadid";
	public static final String KEY_DOWNLOADURL = "key_downloadurl";

	private OnDeleteDownloadListener onDeleteListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final long downloadId = getArguments().getLong(KEY_DOWNLOADID, -1);
		final String downloadUrl = getArguments()
				.getString(KEY_DOWNLOADURL, "");

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setMessage(
						String.format(
								getString(R.string.download_deleteconfirm),
								downloadUrl))
				.setPositiveButton(R.string.btn_delete,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						})
				.setNegativeButton(R.string.btn_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dismiss();
							}
						});

		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialogInf) {
				if (downloadId > 0) {
					dialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View view) {
									deleteDownload(downloadId, downloadUrl);
								}
							});
				}
			}
		});

		return dialog;
	}

	/**
	 * This method will delete given download id
	 */
	private void deleteDownload(long downloadId, String url) {
		if (getActivity() instanceof HomeActivity) {
			int rowsAffected = ((HomeActivity) getActivity()).getDatabase()
					.delete(DownloadsDatabase.TABLE_DOWNLOADS,
							QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID,
									downloadId, true), null);

			if (0 == rowsAffected) {
				onDeleteListener.onDownloadDeleted(false, url);
			} else {
				onDeleteListener.onDownloadDeleted(true, url);
			}
		}

		dismiss();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onDeleteListener = (OnDeleteDownloadListener) getTargetFragment();
		} catch (ClassCastException e) {
			throw new ClassCastException(getTargetFragment().getTag()
					+ " must implement OnDeleteDownloadListener");
		}
	}

	/**
	 * This is an call back interface for the caller
	 * 
	 * @author Milan
	 * 
	 */
	public interface OnDeleteDownloadListener {

		/**
		 * THis method will be called when the user confirms the delete
		 * 
		 * 
		 * @param true if success otherwise false
		 * 
		 */
		public void onDownloadDeleted(boolean isSuccess, String url);
	}
}