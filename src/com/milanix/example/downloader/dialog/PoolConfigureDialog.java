package com.milanix.example.downloader.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.util.PreferenceHelper;

/**
 * This dialog allows user to configure number of tasks
 * 
 * @author Milan
 * 
 */
public class PoolConfigureDialog extends DialogFragment implements
		OnSeekBarChangeListener {

	private SeekBar tasks_seekbar;
	private TextView tasks_number;

	private int poolSize = 0;

	// Max pool based on the device core.
	private int MAX_POOL_MULTIPLIER = 2;

	private OnPoolConfigureListener onTaskConfigureListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_pool, null);

		setUI(rootView);
		setListener();

		return new AlertDialog.Builder(getActivity())
				.setView(rootView)
				.setTitle(R.string.taskconfigure_title)
				.setIcon(R.drawable.ic_icon_task)
				.setPositiveButton(R.string.btn_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								PreferenceHelper.setDownloadPoolSize(
										getActivity(), poolSize);

								onTaskConfigureListener
										.onPoolConfigured(poolSize);
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
		tasks_seekbar = (SeekBar) rootView.findViewById(R.id.tasks_seekbar);
		tasks_number = (TextView) rootView.findViewById(R.id.tasks_number);

		poolSize = PreferenceHelper.getDownloadPoolSize(getActivity());

		tasks_seekbar.setProgress(poolSize - 1);
		tasks_seekbar
				.setMax((Runtime.getRuntime().availableProcessors() * MAX_POOL_MULTIPLIER) - 1);
		tasks_number.setText(getResources().getQuantityString(
				R.plurals.tasks_configured, poolSize, poolSize));
	}

	/**
	 * This method will set listeners to the components
	 */
	private void setListener() {
		tasks_seekbar.setOnSeekBarChangeListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onTaskConfigureListener = (OnPoolConfigureListener) getTargetFragment();
		} catch (ClassCastException e) {
			throw new ClassCastException(getTargetFragment().getTag()
					+ " must implement OnPoolConfigureListener");
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		poolSize = progress + 1;

		tasks_number.setText(getResources().getQuantityString(
				R.plurals.tasks_configured, poolSize, poolSize));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekbar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekbar) {

	}

	/**
	 * This is an call back interface for the caller
	 * 
	 * @author Milan
	 * 
	 */
	public interface OnPoolConfigureListener {

		/**
		 * THis method will be called when the user confirms pool configuration
		 * 
		 * 
		 * @param poolSize
		 *            is the pool size
		 * 
		 */
		public void onPoolConfigured(Integer poolSize);
	}

}
