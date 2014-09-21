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
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
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

	private TextView delete_message;
	private ListView delete_list;

	private long[] downloadIds;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		downloadIds = getArguments().getLongArray(KEY_DOWNLOADIDS);

		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_delete, null);

		setUI(rootView);
		setUIData();

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setView(rootView)
				.setTitle(getString(R.string.download_delete))
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
	 * This method will set UI components
	 */
	private void setUI(View rootView) {
		delete_message = (TextView) rootView.findViewById(R.id.delete_message);
		delete_list = (ListView) rootView.findViewById(R.id.delete_list);
	}

	/**
	 * This method set ui data
	 * 
	 */
	private void setUIData() {
		Cursor resultCursor = getActivity().getContentResolver()
				.query(DownloadContentProvider.CONTENT_URI_DOWNLOADS,
						new String[] { DownloadsDatabase.COLUMN_ID,
								DownloadsDatabase.COLUMN_NAME },
						QueryHelper.getLongIn(DownloadsDatabase.COLUMN_ID,
								downloadIds), null, null);

		delete_message.setText(getResources().getQuantityString(
				R.plurals.download_deleteconfirm, resultCursor.getCount(),
				resultCursor.getCount()));

		delete_list.setAdapter(new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_1, resultCursor,
				new String[] { DownloadsDatabase.COLUMN_NAME },
				new int[] { android.R.id.text1 }, 0));
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