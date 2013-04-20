package com.mosquitolabs.tonight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyStartupIntentReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent serviceIntent = new Intent(context, ServiceUpdate.class);
			context.startService(serviceIntent);
		}
	}
}