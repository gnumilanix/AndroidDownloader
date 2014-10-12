package com.milanix.example.downloader.dialog;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.util.FileUtils.ByteType;

/**
 * This dialog allows user to configure download limit
 * 
 * @author Milan
 * 
 */
public class LimitConfigureDialog extends DialogFragment implements
		OnValueChangeListener {

	private NumberPicker size_picker;
	private NumberPicker type_picker;

	private int limitSize;
	private ByteType limitType;

	private static final List<String> sizeList = Arrays.asList(new String[] {
			"5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55",
			"60", "65", "70", "75", "80", "85", "90", "95", "100" });
	private static final List<String> typeList = Arrays.asList(new String[] {
			ByteType.KB.toString(), ByteType.MB.toString(),
			ByteType.GB.toString() });

	private static final String[] sizeArray = sizeList
			.toArray(new String[sizeList.size()]);
	private static final String[] typeArray = typeList
			.toArray(new String[typeList.size()]);

	private OnLimitConfigureListener onLimitConfigureListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_limit, null);

		setUI(rootView);
		setListener();
		setPickerData();

		return new AlertDialog.Builder(getActivity())
				.setView(rootView)
				.setTitle(R.string.limitconfigure_title)
				.setIcon(R.drawable.ic_icon_limit)
				.setPositiveButton(R.string.btn_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								PreferenceHelper.setDownloadLimit(
										getActivity(), limitSize, limitType);

								onLimitConfigureListener.onLimitConfigured(
										limitSize, limitType);
							}
						})
				.setNegativeButton(R.string.btn_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dismiss();
							}
						}).create();
	}

	/**
	 * This method will set UI components
	 */
	private void setUI(View rootView) {
		size_picker = (NumberPicker) rootView.findViewById(R.id.size_picker);
		type_picker = (NumberPicker) rootView.findViewById(R.id.type_picker);

		size_picker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		type_picker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		limitSize = PreferenceHelper.getDownloadLimitSize(getActivity());
		limitType = PreferenceHelper.getDownloadLimitType(getActivity());

	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
		size_picker.setOnValueChangedListener(this);
		type_picker.setOnValueChangedListener(this);
	}

	/**
	 * This method will set picker data
	 */
	private void setPickerData() {
		size_picker.setMinValue(0);
		size_picker.setMaxValue(sizeArray.length - 1);
		size_picker.setDisplayedValues(sizeArray);
		size_picker.setValue(sizeList.indexOf(Integer.toString(PreferenceHelper
				.getDownloadLimitSize(getActivity()))));

		type_picker.setMinValue(0);
		type_picker.setMaxValue(typeArray.length - 1);
		type_picker.setDisplayedValues(typeArray);
		type_picker.setValue(typeList.indexOf(PreferenceHelper
				.getDownloadLimitType(getActivity()).toString()));
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onLimitConfigureListener = (OnLimitConfigureListener) getTargetFragment();
		} catch (ClassCastException e) {
			throw new ClassCastException(getTargetFragment().getTag()
					+ " must implement OnLimitConfigureListener");
		}
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		if (picker.getId() == R.id.size_picker) {
			// Don't need number format exception
			limitSize = Integer.parseInt(sizeList.get(newVal));
		} else if (picker.getId() == R.id.type_picker) {
			limitType = ByteType.valueOf(typeList.get(newVal));
		}

	}

	/**
	 * This is an call back interface for the caller
	 * 
	 * @author Milan
	 * 
	 */
	public interface OnLimitConfigureListener {

		/**
		 * THis method will be called when the user confirms limit configuration
		 * 
		 * 
		 * @param size
		 *            is the limit size
		 * @param type
		 *            is the limit sizetype
		 * 
		 */
		public void onLimitConfigured(Integer size, ByteType type);
	}

}
