package com.milanix.example.downloader.navigation;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.navigation.NavigationDrawerFragment.NavigationItem;

/**
 * Created by poliveira on 24/10/2014.
 */
public class NavigationDrawerAdapter extends
		RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

	private List<NavigationItem> mData;
	private NavigationDrawerCallbacks mNavigationDrawerCallbacks;
	private int mSelectedPosition;

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
			ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.item_navdrawer, viewGroup, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder viewHolder,
			final int i) {
		viewHolder.textView.setText(mData.get(i).getText());
		viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(mData
				.get(i).getDrawable(), null, null, null);

		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mNavigationDrawerCallbacks != null)
					mNavigationDrawerCallbacks
							.onNavigationDrawerItemSelected(mData.get(i)
									.getRootFragment());
			}
		});

	}

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