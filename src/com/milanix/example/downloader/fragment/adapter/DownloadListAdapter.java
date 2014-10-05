package com.milanix.example.downloader.fragment.adapter;

import java.util.HashMap;

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
import com.milanix.example.downloader.data.database.DownloadsDatabase;
import com.milanix.example.downloader.service.DownloadService;
import com.milanix.example.downloader.util.FileUtils;
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

	private HashMap<Long, Boolean> expandMap = new HashMap<Long, Boolean>();

	public DownloadListAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	/**
	 * This constructor can be used to pass download service which can listen to
	 * callbacks
	 * 
	 * @param context
	 *            application context
	 * @param c
	 *            data cursor
	 * @param autoRequery
	 *            if set true will auto requery
	 * @param downloadService
	 *            is the service to attach/detach callbacks
	 */
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
		holder.download_size = (TextView) view.findViewById(R.id.download_size);

		holder.row_base = (ViewGroup) view.findViewById(R.id.row_base);
		holder.expand_base = (ViewGroup) view.findViewById(R.id.expand_base);

		view.setTag(holder);
		setListener(holder, cursor);

		return view;
	}

	/**
	 * This method will set listener
	 * 
	 * @param holder
	 *            is a viewholder reference for the view
	 * @param cursor
	 *            containing data
	 */
	private void setListener(final ViewHolder holder, final Cursor cursor) {
		final Integer id = cursor.getInt(cursor
				.getColumnIndex(DownloadsDatabase.COLUMN_ID));
		// final DownloadState state = DownloadState.getEnum(cursor
		// .getString(cursor
		// .getColumnIndex(DownloadsDatabase.COLUMN_STATE)));

		// Attach callbacks to files that are authorized to download
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
			public void onDownloadFailed(Download download) {
			}

			@Override
			public void onDownloadProgress(Download download, Integer progress) {
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

	/**
	 * This method will set data to the UI components
	 * 
	 * @param context
	 *            is the current context
	 * 
	 * @param holder
	 *            is a viewholder reference for the view
	 * @param cursor
	 *            containing data
	 */
	private void setData(Context context, final ViewHolder holder,
			final Cursor cursor) {
		if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_NAME))
			holder.download_name.setText(cursor.getString(cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_NAME)));

		// If completed use completed date
		if (DownloadState.COMPLETED.equals(DownloadState.getEnum(cursor
				.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_STATE))))) {
			if (-1 != cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_DATE_COMPLETED))
				holder.download_date
						.setText(TextHelper.getRelativeDateString(
								cursor.getLong(cursor
										.getColumnIndex(DownloadsDatabase.COLUMN_DATE_COMPLETED)),
								context.getString(R.string.download_date_notavailable)));
		} else {
			if (-1 != cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_DATE_ADDED))
				holder.download_date
						.setText(TextHelper.getRelativeDateString(
								cursor.getLong(cursor
										.getColumnIndex(DownloadsDatabase.COLUMN_DATE_ADDED)),
								context.getString(R.string.download_date_notavailable)));
		}

		if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_SIZE))
			holder.download_size.setText(cursor.getString(cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_SIZE)));

		if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_URL)) {
			holder.download_url.setText(cursor.getString(cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_URL)));
			holder.download_url.setSelected(true);

			holder.download_icon.setImageResource(FileUtils
					.getFileTypeBasedRes(cursor.getString(cursor
							.getColumnIndex(DownloadsDatabase.COLUMN_URL))));
		}

		// Show progress only if it's authorized
		if (DownloadState.ADDED_AUTHORIZED.equals(DownloadState.getEnum(cursor
				.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_STATE))))) {
			holder.download_progress.setVisibility(View.VISIBLE);

			holder.download_hint.setBackgroundColor(COLOR_HINT_PROGRESS);
		} else if (DownloadState.DOWNLOADING.equals(DownloadState
				.getEnum(cursor.getString(cursor
						.getColumnIndex(DownloadsDatabase.COLUMN_STATE))))) {
			holder.download_progress.setVisibility(View.GONE);

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

		holder.row_base.setBackgroundResource(R.drawable.list_cab_selector);
		holder.expand_base.setVisibility(View.GONE);

		if (-1 != cursor.getColumnIndex(DownloadsDatabase.COLUMN_ID)) {
			long rowId = cursor.getInt(cursor
					.getColumnIndex(DownloadsDatabase.COLUMN_ID));

			if (expandMap.containsKey(rowId))
				if (expandMap.get(rowId)) {
					holder.row_base
							.setBackgroundResource(R.drawable.list_cab_selector_expanded);

					holder.expand_base.setVisibility(View.VISIBLE);
				}
		}

	}

	/**
	 * This method will set expanded value for a given position
	 * 
	 * @param position
	 */
	public void setExpanded(int position) {
		if (expandMap.containsKey(getItemId(position))) {
			expandMap.put(getItemId(position),
					!expandMap.get(getItemId(position)));
		} else {
			expandMap.put(getItemId(position), true);
		}

		notifyDataSetChanged();
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
		public TextView download_size;

		public ViewGroup row_base;
		public ViewGroup expand_base;
	}
}