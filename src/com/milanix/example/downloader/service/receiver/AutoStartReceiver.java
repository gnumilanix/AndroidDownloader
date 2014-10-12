package com.milanix.example.downloader.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.milanix.example.downloader.pref.PreferenceHelper;
import com.milanix.example.downloader.service.DownloadService;

/**
 * This method will receive boot completed event and start a service if set in
 * preferences
 * 
 * @author Milan
 * 
 */
public class AutoStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (null != intent && null != intent.getExtras())
			if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
				if (PreferenceHelper.getIsAutoStart(context)) {
					Intent serviceStartIntent = new Intent(context,
							DownloadService.class);
					context.startService(serviceStartIntent);
				}
			}
	}
}
