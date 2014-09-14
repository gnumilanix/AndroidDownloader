package com.milanix.example.downloader.fragment.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.data.dao.Download;
import com.milanix.example.downloader.data.dao.Download.DownloadListener;
import com.milanix.example.downloader.data.dao.Download.DownloadState;
import com.milanix.example.downloader.data.dao.Download.FailedReason;
import com.milanix.example.downloader.data.dao.Download.TaskState;
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.service.DownloadService;
import com.milanix.example.downloader.util.TextHelper;

/**
 * This Adapter creates download list items
 * 
 * @author Milan
 * 
 */
public class DownloadListAdapter extends CursorAdapter {
	private DownloadService downloadService = null;

	private static final int COLOR_HINT_SUCCESS = Color.parseColor("#99CC00");
	private static final int COLOR_HINT_FAILURE = Color.parseColor("#FF4444");
	private static final int COLOR_HINT_PROGRESS = Color.parseColor("#33B5E5");

	public DownloadListAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	public DownloadListAdapter(Context context, Cursor c, boolean autoRequery,
			DownloadService downloadService) {
		this(context, c, autoRequery);

		this.downloadService = downloadService;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		if (view.getTag() instanceof ViewHolder) {
			ViewHolder holder = (ViewHolder) view.getTag();

			setData(context, holder, cursor);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(
				R.layout.item_download, parent, false);

		ViewHolder holder = new ViewHolder();

		holder.download_hint = (View) view.findViewById(R.id.download_hint);

		holder.download_icon = (ImageView) view
				.findViewById(R.id.download_icon);

		holder.download_progress = (ProgressBar) view
				.findViewById(R.id.download_progress);

		holder.download_name = (TextView) view.findViewById(R.id.download_name);
		holder.download_date = (TextView) view.findViewById(R.id.download_date);
		holder.download_url = (TextView) view.findViewById(R.id.download_url);

		view.setTag(holder);

		setListener(holder, cursor);

		return view;
	}

	/**
	 * This method will set listener
	 * 
	 * @param holder
	 *            is a viewholder object for the view
	 * @param cursor
	 *            containing data
	 */
	private void setListener(final ViewHolder holder, final Cursor cursor) {
		final Integer id = cursor.getInt(cursor
				.getColumnIndex(DownloadsDatabase.COLUMN_ID));
		final DownloadState state = DownloadState.getEnum(cursor
				.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_STATE)));

		if (DownloadState.DOWNLOADING.equals(state)
				|| DownloadState.ADDED.equals(state)) {
			final ProgressBar progressBar = holder.download_progress;

			final DownloadListener callback = new DownloadListener() {

				@Override
				public void onDownloadStarted(Download download) {
				}

				@Override
				public void onDownloadCancelled(Download download) {
				}

				@Override
				public void onDownloadCompleted(Download download) {
				}

				@Override
				public void onDownloadFailed(Download download,
						FailedReason reason) {
				}

				@Override
				public void onDownloadProgress(TaskState taskState,
						Download download, Integer progress) {
					if (null != progressBar && null != progress)
						progressBar.setProgress(progress);
				}
			};

			holder.download_progress
					.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {

						@Override
						public void onViewAttachedToWindow(View view) {
							if (null != downloadService && null != id
									&& null != callback) {
								downloadService.attachCallback(id, callback);
							}
						}

						@Override
						public void onViewDetachedFromWindow(View view) {
							if (null != downloadService && null != id
									&& null != callback) {
								downloadService.detachCallback(id, callback);
							}
						}

					});
		}
	}

	/**
	 * This method will set data to the UI components
	 * 
	 * @param context
	 *            is the current context
	 * 
	 * @param holder
	 *            is a viewholder object for the view
	 * @param cursor
	 *            containing data
	 */
	private void setData(Context context, final ViewHolder holder,
			final Cursor cursor) {

		holder.download_name.setText(cursor.getString(cursor
				.getColumnIndex(DownloadsDatabase.COLUMN_NAME)));
		holder.download_date.setText(TextHelper.getRelativeDateString(cursor
				.getLong(cursor.getColumnIndex(DownloadsDatabase.COLUMN_NAME)),
				context.getString(R.string.download_date_notavailable)));
		holder.download_url.setText(cursor.getString(cursor
				.getColumnIndex(DownloadsDatabase.COLUMN_URL)));

		if (DownloadState.DOWNLOADING.equals(DownloadState.getEnum(cursor
				.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_STATE))))) {
			holder.download_progress.setVisibility(View.VISIBLE);

			holder.download_hint.setBackgroundColor(COLOR_HINT_PROGRESS);
		} else if (DownloadState.COMPLETED.equals(DownloadState.getEnum(cursor
				.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_STATE))))) {
			holder.download_progress.setVisibility(View.GONE);

			holder.download_hint.setBackgroundColor(COLOR_HINT_SUCCESS);
		} else {
			holder.download_progress.setVisibility(View.GONE);

			holder.download_hint.setBackgroundColor(COLOR_HINT_FAILURE);
		}

	}

	/**
	 * This is a class that holds view reference for each item in the adapter
	 * 
	 * @author Milan
	 * 
	 */
	public static class ViewHolder {
		public View download_hint;

		public ImageView download_icon;

		public ProgressBar download_progress;

		public TextView download_name;
		public TextView download_date;
		public TextView download_url;
	}
}