package com.milanix.example.downloader.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.util.FileUtils.FileType;

/**
 * This dialog allows user to configure download list filter
 * 
 * @author Milan
 * 
 */
public class FilterConfigureDialog extends DialogFragment implements
		View.OnClickListener {

	private RadioButton filter_document;
	private RadioButton filter_code;
	private RadioButton filter_audio;
	private RadioButton filter_image;
	private RadioButton filter_video;
	private RadioButton filter_archive;
	private RadioButton filter_pdf;
	private RadioButton filter_all;

	private String filterType;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_filter, null);

		setUI(rootView);
		setListener();

		return new AlertDialog.Builder(getActivity()).setView(rootView)
				.setTitle(R.string.title_filterby)
				.setIcon(R.drawable.ic_action_filter).create();
	}

	/**
	 * This method will set UI components
	 */
	private void setUI(View rootView) {
		filter_document = (RadioButton) rootView
				.findViewById(R.id.filter_document);
		filter_code = (RadioButton) rootView.findViewById(R.id.filter_code);
		filter_audio = (RadioButton) rootView.findViewById(R.id.filter_audio);
		filter_image = (RadioButton) rootView.findViewById(R.id.filter_image);
		filter_video = (RadioButton) rootView.findViewById(R.id.filter_video);
		filter_archive = (RadioButton) rootView
				.findViewById(R.id.filter_archive);
		filter_pdf = (RadioButton) rootView.findViewById(R.id.filter_pdf);
		filter_all = (RadioButton) rootView.findViewById(R.id.filter_all);

		filterType = PreferenceHelper.getFilterType(getActivity());

		if (TextUtils.isEmpty(filterType))
			filter_all.setChecked(true);
		else if (FileType.DOCUMENT.toString().equals(filterType))
			filter_document.setChecked(true);
		else if (FileType.CODE.toString().equals(filterType))
			filter_code.setChecked(true);
		else if (FileType.AUDIO.toString().equals(filterType))
			filter_audio.setChecked(true);
		else if (FileType.IMAGE.toString().equals(filterType))
			filter_image.setChecked(true);
		else if (FileType.VIDEO.toString().equals(filterType))
			filter_video.setChecked(true);
		else if (FileType.ARCHIVE.toString().equals(filterType))
			filter_archive.setChecked(true);
		else if (FileType.PDF.toString().equals(filterType))
			filter_pdf.setChecked(true);
	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
		filter_document.setOnClickListener(this);
		filter_code.setOnClickListener(this);
		filter_audio.setOnClickListener(this);
		filter_image.setOnClickListener(this);
		filter_video.setOnClickListener(this);
		filter_archive.setOnClickListener(this);
		filter_pdf.setOnClickListener(this);
		filter_all.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.filter_document:
			PreferenceHelper.setFilterType(getActivity(),
					FileType.DOCUMENT.toString());
			break;
		case R.id.filter_code:
			PreferenceHelper.setFilterType(getActivity(),
					FileType.CODE.toString());
			break;
		case R.id.filter_audio:
			PreferenceHelper.setFilterType(getActivity(),
					FileType.AUDIO.toString());
			break;
		case R.id.filter_image:
			PreferenceHelper.setFilterType(getActivity(),
					FileType.IMAGE.toString());
			break;
		case R.id.filter_video:
			PreferenceHelper.setFilterType(getActivity(),
					FileType.VIDEO.toString());
			break;
		case R.id.filter_archive:
			PreferenceHelper.setFilterType(getActivity(),
					FileType.ARCHIVE.toString());
			break;
		case R.id.filter_pdf:
			PreferenceHelper.setFilterType(getActivity(),
					FileType.PDF.toString());
			break;
		case R.id.filter_all:
			PreferenceHelper.setFilterType(getActivity(), null);
			break;
		}

		dismiss();
	}

}