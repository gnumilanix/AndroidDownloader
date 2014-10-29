package com.milanix.example.downloader.navigation;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.milanix.example.downloader.R;

/**
 * This method will creates navigation spinner adapter
 * 
 * @author Milan
 * 
 */
public class NavigationSpinnerAdapter extends BaseAdapter {
	private Context context;

	private ArrayList<NavigationSpinnerItems> spinnerItems = new ArrayList<NavigationSpinnerItems>();

	/**
	 * This method will set default constructor
	 * 
	 * @param context
	 */
	public NavigationSpinnerAdapter(Context context) {
		this.context = context;

		spinnerItems.clear();

		spinnerItems.add(new NavigationSpinnerItems(context
				.getString(R.string.tab_downloading),
				R.drawable.ic_icon_task_dark));
		spinnerItems.add(new NavigationSpinnerItems(context
				.getString(R.string.tab_downloaded),
				R.drawable.ic_icon_path_dark));
		spinnerItems.add(new NavigationSpinnerItems(context
				.getString(R.string.tab_settings),
				R.drawable.ic_action_settings_dark));
		// spinnerItems.add(new
		// NavigationSpinnerItems(context.getString(R.string.tab_browse),R.drawable.ic_action_browse));
	}

	@Override
	public int getCount() {
		return spinnerItems.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_spinner, null);

			holder = new ViewHolder();

			holder.spinner_item = (TextView) convertView
					.findViewById(R.id.spinner_item);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		setData(holder, position);
		setListener(holder, position);

		return convertView;
	}

	/**
	 * This method will set listener
	 * 
	 * @param holder
	 * @param position
	 */
	private void setListener(ViewHolder holder, int position) {
	}

	/**
	 * This method will set UI data
	 * 
	 * @param holder
	 *            is the ViewHolder for item at certain position
	 */
	private void setData(final ViewHolder holder, final int position) {
		holder.spinner_item.setText(spinnerItems.get(position).getName());
		holder.spinner_item.setCompoundDrawablesWithIntrinsicBounds(
				spinnerItems.get(position).getResId(), 0, 0, 0);
	}

	/**
	 * This is a class that holds data for each item in the adapter
	 * 
	 * @author Milan
	 * 
	 */
	private static class ViewHolder {
		public TextView spinner_item;
	}

	/**
	 * Spinner items objects
	 * 
	 * @author Milan
	 * 
	 */
	public static class NavigationSpinnerItems {
		public String name;
		public Integer resId;

		public NavigationSpinnerItems(String name, Integer resId) {
			this.name = name;
			this.resId = resId;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the resId
		 */
		public Integer getResId() {
			return resId;
		}

		/**
		 * @param resId
		 *            the resId to set
		 */
		public void setResId(Integer resId) {
			this.resId = resId;
		}

	}
}
