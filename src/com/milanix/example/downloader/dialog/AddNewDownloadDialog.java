package com.milanix.example.downloader.dialog;

import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.provider.DownloadContentProvider;
import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.util.FileUtils;
import com.milanix.example.downloader.util.TextHelper;

/**
 * This dialog allows user to add download to the queue
 * 
 * @author Milan
 * 
 */
public class AddNewDownloadDialog extends DialogFragment implements
		View.OnClickListener {
	public static final String KEY_ADDNEW_URL = "addnew_url";

	private TextView et_url;
	private Button btn_ok;

	private OnAddNewDownloadListener onNewDownloadListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_addnew, null);

		setUI(rootView);
		setListener();
		setData(getArguments());

		return new AlertDialog.Builder(getActivity()).setView(rootView)
				.setTitle(R.string.addnew_title)
				.setIcon(R.drawable.ic_action_addtoqueue).create();
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

	/**
	 * Sets UI data with given bundle
	 * 
	 * @param arguments
	 */
	private void setData(Bundle arguments) {
		if (null != arguments && arguments.containsKey(KEY_ADDNEW_URL))
			et_url.setText(arguments.getString(KEY_ADDNEW_URL, ""));
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onNewDownloadListener = (OnAddNewDownloadListener) activity;
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
		ContentValues values = new ContentValues();
		values.put(DownloadsDatabase.COLUMN_URL, url);
		values.put(DownloadsDatabase.COLUMN_NAME,
				FilenameUtils.getBaseName(url));
		values.put(DownloadsDatabase.COLUMN_TYPE,
				FilenameUtils.getExtension(url));
		values.put(DownloadsDatabase.COLUMN_DATE_ADDED, new Date().getTime());
		values.put(DownloadsDatabase.COLUMN_STATE,
				DownloadState.ADDED_NOTAUTHORIZED.toString());
		values.put(
				DownloadsDatabase.COLUMN_PATH,
				FileUtils.getLocalDownloadPath(
						PreferenceHelper.getDownloadPath(getActivity()), url));

		Uri insertedContentURI = getActivity().getContentResolver().insert(
				DownloadContentProvider.CONTENT_URI_DOWNLOADS, values);

		if (null != insertedContentURI) {
			int rowId = TextHelper.getValueAsInt(insertedContentURI
					.getLastPathSegment());

			if (-1 == rowId) {
				onNewDownloadListener.onNewDownloadAdded(null);
			} else {
				onNewDownloadListener.onNewDownloadAdded(rowId);

				dismiss();
			}
		} else {
			onNewDownloadListener.onNewDownloadAdded(null);

			dismiss();
		}

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
		 *            is the row id of a newly added download, null if failed
		 * 
		 */
		public void onNewDownloadAdded(Integer id);
	}

}
