package com.milanix.example.downloader.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;

/**
 * This dialog allows user to delete downloads
 * 
 * @author Milan
 * 
 */
public class DeleteDownloadDialog extends DialogFragment {
	public static final String KEY_DOWNLOADIDS = "key_downloadid";

	private OnDeleteDownloadListener onDeleteListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final long[] downloadIds = getArguments().getLongArray(KEY_DOWNLOADIDS);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setMessage(
						String.format(
								getString(R.string.download_deleteconfirm), ""))
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
				if (null != downloadIds) {
					dialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View view) {
									deleteDownloads(downloadIds);
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
	private void deleteDownloads(long[] downloadIds) {
		ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<ContentProviderOperation>();

		for (long downloadId : downloadIds) {
			deleteOperations
					.add(ContentProviderOperation
							.newDelete(
									DownloadContentProvider.CONTENT_URI_DOWNLOADS)
							.withSelection(
									DownloadsDatabase.COLUMN_ID + " = ?",
									new String[] { Long.toString(downloadId) })
							.build());
		}

		try {
			ContentProviderResult[] operationsResult = getActivity()
					.getContentResolver()
					.applyBatch(DownloadContentProvider.AUTHORITY,
							deleteOperations);

			if (operationsResult.length > 0)
				onDeleteListener.onDownloadDeleted(true);
			else
				onDeleteListener.onDownloadDeleted(false);
		} catch (RemoteException e) {
			onDeleteListener.onDownloadDeleted(false);
		} catch (OperationApplicationException e) {
			onDeleteListener.onDownloadDeleted(false);
		} finally {
			dismiss();
		}
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
		 * @param true if success otherwise false. True if atleast a row is
		 *        affected
		 * 
		 */
		public void onDownloadDeleted(boolean isSuccess);
	}
}