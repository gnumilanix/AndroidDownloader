package com.milanix.example.downloader.navigation;

import java.util.List;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.navigation.NavigationDrawerFragment.NavigationItem;

/**
 * Navigation drawee adapter
 */
public class NavigationDrawerAdapter extends
		RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

	private List<NavigationItem> mData;
	private NavigationDrawerCallbacks mNavigationDrawerCallbacks;
	private int mSelectedPosition = 0;

	public NavigationDrawerAdapter(List<NavigationItem> data) {
		mData = data;
	}

	public NavigationDrawerCallbacks getNavigationDrawerCallbacks() {
		return mNavigationDrawerCallbacks;
	}

	public void setNavigationDrawerCallbacks(
			NavigationDrawerCallbacks navigationDrawerCallbacks) {
		mNavigationDrawerCallbacks = navigationDrawerCallbacks;
	}

	@Override
	public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(
			ViewGroup viewGroup, int position) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.item_navdrawer, viewGroup, false);

		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder viewHolder,
			final int position) {
		viewHolder.textView.setText(mData.get(position).getText());
		viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(
				mData.get(position).getDrawable(), null, null, null);

		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mNavigationDrawerCallbacks != null)
					mNavigationDrawerCallbacks
							.onNavigationDrawerItemSelected(mData.get(position)
									.getRootFragment());

				selectPosition(position);
			}
		});

		if (mSelectedPosition == position)
			viewHolder.itemView.setBackgroundColor(Color.parseColor("#DCDCDC"));
		else
			viewHolder.itemView.setBackgroundResource(R.drawable.list_selector);
	}

	/**
	 * Selects given position
	 * 
	 * @param position
	 */
	public void selectPosition(int position) {
		int lastPosition = mSelectedPosition;
		mSelectedPosition = position;

		notifyItemChanged(lastPosition);
		notifyItemChanged(position);
	}

	@Override
	public int getItemCount() {
		return mData != null ? mData.size() : 0;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView textView;

		public ViewHolder(View itemView) {
			super(itemView);
			textView = (TextView) itemView.findViewById(R.id.item_name);
		}
	}
}