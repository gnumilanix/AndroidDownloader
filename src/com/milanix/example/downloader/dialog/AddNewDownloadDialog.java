package com.milanix.example.downloader.dialog;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.milanix.example.downloader.Downloader;
import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Credential;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.database.CredentialsDatabase;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.data.provider.CredentialContentProvider;
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
		View.OnClickListener, OnCheckedChangeListener {
	public static final String KEY_ADDNEW_URL = "addnew_url";

	private EditText et_url;
	private EditText et_username;
	private EditText et_password;

	private CheckBox cb_showpassword;

	private ViewGroup vg_credential;

	private Button btn_ok;

	private OnAddNewDownloadListener onNewDownloadListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View rootView = LayoutInflater.from(getActivity()).inflate(
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
		et_username = (EditText) rootView.findViewById(R.id.et_username);
		et_password = (EditText) rootView.findViewById(R.id.et_password);

		cb_showpassword = (CheckBox) rootView
				.findViewById(R.id.cb_showpassword);

		vg_credential = (ViewGroup) rootView.findViewById(R.id.vg_credential);
		vg_credential.setVisibility(View.GONE);

		btn_ok = (Button) rootView.findViewById(R.id.btn_ok);
	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
		btn_ok.setOnClickListener(this);
		cb_showpassword.setOnCheckedChangeListener(this);
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
	public void onClick(View view) {
		if (TextUtils.isEmpty(et_url.getText().toString()))
			et_url.setError(getString(R.string.addnew_error_empty));
		if (!Downloader.HTTP_VALIDATOR.isValid(et_url.getText().toString())
				&& !Downloader.FTP_VALIDATOR.isValid(et_url.getText()
						.toString()))
			et_url.setError(getString(R.string.addnew_error_invalid));
		else {
			if (Downloader.FTP_VALIDATOR.isValid(et_url.getText().toString())) {
				if (View.VISIBLE == vg_credential.getVisibility())
					saveCredential(et_url.getText().toString(), et_username
							.getText().toString(), et_password.getText()
							.toString());
				else
					showCredentialView(et_url.getText().toString());
			} else {
				addNewDownload(et_url.getText().toString());
			}
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		if (view.getId() == R.id.cb_showpassword) {
			if (isChecked)
				et_password
						.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			else
				et_password.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}

	}

	/**
	 * This method will save credential for this path
	 * 
	 * @param path
	 *            full url path
	 * @param username
	 *            username for the host
	 * @param password
	 *            password for the host
	 */
	private void saveCredential(final String path, final String username,
			final String password) {

		try {
			final URL downloadUrl = new URL(path);

			if (null != downloadUrl.getHost()) {
				final ContentValues values = new ContentValues();
				values.put(CredentialsDatabase.COLUMN_HOST,
						downloadUrl.getHost());
				values.put(CredentialsDatabase.COLUMN_PROTOCOL,
						downloadUrl.getProtocol());
				values.put(CredentialsDatabase.COLUMN_USERNAME, username);
				values.put(CredentialsDatabase.COLUMN_PASSWORD, password);

				final Credential credential = new Credential()
						.retrieve(getActivity()
								.getContentResolver()
								.query(CredentialContentProvider.CONTENT_URI_CREDENTIALS,
										null,
										QueryHelper
												.getWhere(
														CredentialsDatabase.COLUMN_HOST,
														downloadUrl.getHost(),
														true), null, null));

				if (null == credential
						|| TextUtils.isEmpty(credential.getHost()))
					getActivity().getContentResolver().insert(
							CredentialContentProvider.CONTENT_URI_CREDENTIALS,
							values);
				else
					getActivity().getContentResolver().update(
							CredentialContentProvider.CONTENT_URI_CREDENTIALS,
							values, CredentialsDatabase.COLUMN_ID + " = ?",
							new String[] { downloadUrl.getHost() });

			}
		} catch (MalformedURLException ignored) {

		}

		addNewDownload(et_url.getText().toString());
	}

	/**
	 * This method will get credential for this path
	 * 
	 * @param path
	 *            full url path
	 */
	private void getCredential(final String path) {
		try {
			final URL downloadUrl = new URL(path);

			if (null != downloadUrl.getHost()) {
				final Credential credential = new Credential()
						.retrieve(getActivity()
								.getContentResolver()
								.query(CredentialContentProvider.CONTENT_URI_CREDENTIALS,
										null,
										QueryHelper
												.getWhere(
														CredentialsDatabase.COLUMN_HOST,
														downloadUrl.getHost(),
														true), null, null));

				if (null != credential
						&& !TextUtils.isEmpty(credential.getHost())) {
					if (!TextUtils.isEmpty(credential.getUsername()))
						et_username.setText(credential.getUsername());

					if (!TextUtils.isEmpty(credential.getPassword()))
						et_password.setText(credential.getPassword());
				} else {
					et_username.setText(Credential.USERNAME_ANONOYMOUS);
					et_password.setText(Credential.PASSWORD_ANONOYMOUS);
				}
			}
		} catch (MalformedURLException ignored) {
		}
	}

	/**
	 * This method will add given url into the databse
	 * 
	 * @param url
	 */
	private void addNewDownload(final String url) {
		final ContentValues values = new ContentValues();
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

		final Uri insertedContentURI = getActivity().getContentResolver()
				.insert(DownloadContentProvider.CONTENT_URI_DOWNLOADS, values);

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
	 * Shows or hides credential view
	 */
	private void showCredentialView(final String path) {
		final float scaleFrom = View.VISIBLE == vg_credential.getVisibility() ? 1f
				: 0f;
		final float scaleTo = View.VISIBLE == vg_credential.getVisibility() ? 0f
				: 1f;

		final Animation scale = new ScaleAnimation(1f, 1f, scaleFrom, scaleTo,
				Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f);
		scale.setDuration(500);
		scale.setFillAfter(true);
		scale.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				vg_credential.setVisibility(View.VISIBLE);

				if (scaleTo == 1f)
					getCredential(path);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (scaleTo == 0f)
					vg_credential.setVisibility(View.GONE);
				else
					vg_credential.setVisibility(View.VISIBLE);

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

		});
		vg_credential.startAnimation(scale);
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
