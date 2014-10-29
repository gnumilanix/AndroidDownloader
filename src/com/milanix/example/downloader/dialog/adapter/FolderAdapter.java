package com.milanix.example.downloader.dialog.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.milanix.example.downloader.R;

/**
 * This method will create folders adapter
 * 
 * @author Milan
 * 
 */
public class FolderAdapter extends BaseAdapter {
	private Context context;
	private File rootFile;

	private List<File> folderList = new ArrayList<File>();

	/**
	 * This method will set default constructor
	 * 
	 * @param context
	 */
	public FolderAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return folderList.size();
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
			convertView = inflater.inflate(R.layout.item_folder, null);

			holder = new ViewHolder();

			holder.folder = (TextView) convertView.findViewById(R.id.folder);

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
		holder.folder.setText(folderList.get(position).getName());
	}

	/**
	 * This method will return item list in this adapter
	 * 
	 * @return
	 */
	public List<File> getFolderList() {
		return folderList;
	}

	/**
	 * This method will get folder at given position, otherwise null
	 * 
	 * @param position
	 * @return
	 */
	public File getFolder(int position) {
		if (position < folderList.size())
			return folderList.get(position);

		return null;
	}

	/**
	 * @return the rootFile
	 */
	public File getRootFile() {
		return rootFile;
	}

	/**
	 * @param rootFile
	 *            the rootFile to set
	 */
	public void setRootFile(File rootFile) {
		this.rootFile = rootFile;

		populateListFromPath();
	}

	/**
	 * This method will populate this adapter from given path
	 */
	private void populateListFromPath() {
		if (null != folderList)
			folderList.clear();

		if (null != rootFile && rootFile.exists() && rootFile.isDirectory()) {
			File[] files = rootFile.listFiles();

			for (File file : files) {
				if (file.isDirectory() && !file.isHidden()) {
					folderList.add(file);
				}
			}
		}

		notifyDataSetChanged();
	}

	/**
	 * This is a class that holds data for each item in the adapter
	 * 
	 * @author Milan
	 * 
	 */
	public static class ViewHolder {
		public TextView folder;
	}
}
