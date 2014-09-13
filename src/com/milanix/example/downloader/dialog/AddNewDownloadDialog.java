package com.milanix.example.downloader.dialog;

import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.milanix.example.downloader.HomeActivity;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.util.TextHelper;

/**
 * This dialog allows user to add download to the queue
 * 
 * @author Milan
 * 
 */
public class AddNewDownloadDialog extends DialogFragment implements
		View.OnClickListener {

	private TextView et_url;
	private Button btn_ok;

	private OnAddNewDownloadListener onNewDownloadListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_addnew, null);

		setUI(rootView);
		setListener();

		return new AlertDialog.Builder(getActivity()).setView(rootView)
				.create();
	}

	/**
	 * This method will set UI components
	 */
	private void setUI(View rootView) {
		et_url = (EditText) rootView.findViewById(R.id.et_url);
		btn_ok = (Button) rootView.findViewById(R.id.btn_ok);
	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
		btn_ok.setOnClickListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onNewDownloadListener = (OnAddNewDownloadListener) getTargetFragment();
		} catch (ClassCastException e) {
			throw new ClassCastException(getTargetFragment().getTag()
					+ " must implement OnAddNewDownloadListener");
		}
	}

	@Override
	public void onClick(View v) {
		if (TextHelper.isStringEmpty(et_url.getText().toString()))
			et_url.setError(getString(R.string.addnew_error_empty));
		if (!UrlValidator.getInstance().isValid(et_url.getText().toString()))
			et_url.setError(getString(R.string.addnew_error_invalid));
		else {
			addNewDownload(et_url.getText().toString());
		}
	}

	/**
	 * This method will add given url into the databse
	 * 
	 * @param url
	 */
	private void addNewDownload(String url) {
		if (getActivity() instanceof HomeActivity) {
			ContentValues values = new ContentValues();
			values.put(DownloadsDatabase.COLUMN_URL, url);
			values.put(DownloadsDatabase.COLUMN_NAME,
					FilenameUtils.getBaseName(url));
			values.put(DownloadsDatabase.COLUMN_TYPE,
					FilenameUtils.getExtension(url));
			values.put(DownloadsDatabase.COLUMN_DATE, new Date().getTime());
			values.put(DownloadsDatabase.COLUMN_STATE,
					DownloadState.DOWNLOADING.toString());

			Long rowId = ((HomeActivity) getActivity()).getDatabase().insert(
					DownloadsDatabase.TABLE_DOWNLOADS, null, values);

			if (-1 == rowId) {
				onNewDownloadListener.onNewDownloadAdded(null);
			} else {
				onNewDownloadListener.onNewDownloadAdded(getDownload(rowId));

				dismiss();
			}
		}
	}

	/**
	 * This method will retrieve download with given id
	 * 
	 * @param id
	 *            of the download
	 * @return download object
	 */
	private Download getDownload(long id) {
		if (getActivity() instanceof HomeActivity) {

			Cursor retrievedCursor = ((HomeActivity) getActivity())
					.getDatabase().query(
							DownloadsDatabase.TABLE_DOWNLOADS,
							null,
							QueryHelper.getWhere(DownloadsDatabase.COLUMN_ID,
									id, true), null, null, null, null, null);

			if (retrievedCursor.getCount() > 0) {
				if (retrievedCursor.moveToFirst()) {
					return new Download(
							retrievedCursor
									.getInt(retrievedCursor
											.getColumnIndex(DownloadsDatabase.COLUMN_ID)),
							retrievedCursor.getString(retrievedCursor
									.getColumnIndex(DownloadsDatabase.COLUMN_URL)),
							retrievedCursor.getString(retrievedCursor
									.getColumnIndex(DownloadsDatabase.COLUMN_NAME)),
							retrievedCursor.getInt(retrievedCursor
									.getColumnIndex(DownloadsDatabase.COLUMN_TYPE)),
							retrievedCursor.getLong(retrievedCursor
									.getColumnIndex(DownloadsDatabase.COLUMN_DATE)),
							DownloadState.getEnum(retrievedCursor.getString(retrievedCursor
									.getColumnIndex(DownloadsDatabase.COLUMN_STATE))));
				}
			}
		}

		return null;
	}

	/**
	 * This is an call back interface for the caller
	 * 
	 * @author Milan
	 * 
	 */
	public interface OnAddNewDownloadListener {

		/**
		 * THis method will be called when the user confirms the url
		 * 
		 * 
		 * @param download
		 *            is the download object, null if failed
		 * 
		 */
		public void onNewDownloadAdded(Download download);
	}

}
