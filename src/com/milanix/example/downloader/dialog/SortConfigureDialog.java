package com.milanix.example.downloader.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.data.database.util.QueryHelper;
import com.milanix.example.downloader.pref.PreferenceHelper;

/**
 * This dialog allows user to configure download list sort type
 * 
 * @author Milan
 * 
 */
public class SortConfigureDialog extends DialogFragment implements
		View.OnClickListener {

	private RadioButton asc_added;
	private RadioButton dsc_added;
	private RadioButton asc_completed;
	private RadioButton dsc_completed;
	private RadioButton asc_size;
	private RadioButton dsc_size;

	private String orderingField;
	private String orderingType;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_sort, null);

		setUI(rootView);
		setListener();

		return new AlertDialog.Builder(getActivity()).setView(rootView)
				.setTitle(R.string.title_sortby)
				.setIcon(R.drawable.ic_action_sort).create();
	}

	/**
	 * This method will set UI components
	 */
	private void setUI(View rootView) {
		asc_added = (RadioButton) rootView.findViewById(R.id.asc_added);
		dsc_added = (RadioButton) rootView.findViewById(R.id.dsc_added);
		asc_completed = (RadioButton) rootView.findViewById(R.id.asc_completed);
		dsc_completed = (RadioButton) rootView.findViewById(R.id.dsc_completed);
		asc_size = (RadioButton) rootView.findViewById(R.id.asc_size);
		dsc_size = (RadioButton) rootView.findViewById(R.id.dsc_size);

		orderingField = PreferenceHelper.getSortOrderingField(getActivity());
		orderingType = PreferenceHelper.getSortOrderingType(getActivity());

		if (DownloadsDatabase.COLUMN_DATE_ADDED.equals(orderingField)
				&& QueryHelper.ORDERING_ASC.equals(orderingType))
			asc_added.setChecked(true);
		else if (DownloadsDatabase.COLUMN_DATE_ADDED.equals(orderingField)
				&& QueryHelper.ORDERING_DESC.equals(orderingType))
			dsc_added.setChecked(true);
		else if (DownloadsDatabase.COLUMN_DATE_COMPLETED.equals(orderingField)
				&& QueryHelper.ORDERING_ASC.equals(orderingType))
			asc_completed.setChecked(true);
		else if (DownloadsDatabase.COLUMN_DATE_COMPLETED.equals(orderingField)
				&& QueryHelper.ORDERING_DESC.equals(orderingType))
			dsc_completed.setChecked(true);
		else if (DownloadsDatabase.COLUMN_SIZE.equals(orderingField)
				&& QueryHelper.ORDERING_ASC.equals(orderingType))
			asc_size.setChecked(true);
		else if (DownloadsDatabase.COLUMN_SIZE.equals(orderingField)
				&& QueryHelper.ORDERING_DESC.equals(orderingType))
			dsc_size.setChecked(true);
	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
		asc_added.setOnClickListener(this);
		dsc_added.setOnClickListener(this);
		asc_completed.setOnClickListener(this);
		dsc_completed.setOnClickListener(this);
		asc_size.setOnClickListener(this);
		dsc_size.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.asc_added:
			PreferenceHelper.setSortOrdering(getActivity(),
					DownloadsDatabase.COLUMN_DATE_ADDED,
					QueryHelper.ORDERING_ASC);
			break;
		case R.id.dsc_added:
			PreferenceHelper.setSortOrdering(getActivity(),
					DownloadsDatabase.COLUMN_DATE_ADDED,
					QueryHelper.ORDERING_DESC);
			break;

		case R.id.asc_completed:
			PreferenceHelper.setSortOrdering(getActivity(),
					DownloadsDatabase.COLUMN_DATE_COMPLETED,
					QueryHelper.ORDERING_ASC);
			break;
		case R.id.dsc_completed:
			PreferenceHelper.setSortOrdering(getActivity(),
					DownloadsDatabase.COLUMN_DATE_COMPLETED,
					QueryHelper.ORDERING_DESC);
			break;

		case R.id.asc_size:
			PreferenceHelper.setSortOrdering(getActivity(),
					DownloadsDatabase.COLUMN_SIZE, QueryHelper.ORDERING_ASC);
			break;
		case R.id.dsc_size:
			PreferenceHelper.setSortOrdering(getActivity(),
					DownloadsDatabase.COLUMN_SIZE, QueryHelper.ORDERING_DESC);
			break;
		}

		dismiss();
	}

}