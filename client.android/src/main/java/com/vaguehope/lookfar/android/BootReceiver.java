package com.vaguehope.lookfar.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive (final Context context, final Intent intent) {
		Log.i(C.TAG, "BootReceiver invoked.");
		//AlarmReceiver.configureAlarm(context);
	}

}
