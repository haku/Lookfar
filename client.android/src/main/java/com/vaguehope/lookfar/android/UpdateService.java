package com.vaguehope.lookfar.android;

import java.util.List;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.vaguehope.lookfar.android.model.Update;

public class UpdateService extends IntentService {

	private static final int NOTIFICAITON_ID_ALERT = 101;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public UpdateService () {
		super("LookfarUpdateService");
	}

	@Override
	protected void onHandleIntent (final Intent i) {
		Log.i(C.TAG, "UpdateService invoked.");
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		final WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, C.TAG);
		wl.acquire();
		try {
			if (connectionPresent()) {
				checkUpdates();
			}
			else {
				Log.i(C.TAG, "No connection, aborted.");
			}
		}
		finally {
			wl.release();
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private void checkUpdates () {
		try {
			final StringBuilder title = new StringBuilder();
			final StringBuilder msg = new StringBuilder();
			int count = 0;

			final Client client = new Client();
			final List<Update> updates = client.fetch();
			for (final Update item : updates) {
				final String flag = item.getFlag();
				if (!"OK".equals(flag)) {
					count += 1;
					final String node = item.getNode();
					if (title.indexOf(node) < 0) {
						if (title.length() > 0) title.append(", ");
						title.append(node);
					}
					if (msg.length() > 0) msg.append(", ");
					msg.append(item.getKey());
				}
			}
			updateNotification(this, title.toString(), msg.toString(), count);
		}
		catch (final Exception e) {
			updateNotification(this, "Lookfar update failed", e.getMessage(), 0);
		}
	}

	private static void updateNotification (final Context context, final CharSequence title, final CharSequence msg, final int count) {
		final NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (msg != null && msg.length() > 0) {
			final Intent notificationIntent = new Intent(context, MainActivity.class);
			final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

			final Notification n = new Notification(R.drawable.service_notification, "Lookfar: " + title, System.currentTimeMillis());
			n.flags = Notification.FLAG_AUTO_CANCEL;
			n.defaults = Notification.DEFAULT_ALL;
			n.setLatestEventInfo(context, title != null ? title : "Lookfar", msg, contentIntent);
			n.number = count;

			notificationMgr.notify(NOTIFICAITON_ID_ALERT, n);
		}
		else {
			notificationMgr.cancel(NOTIFICAITON_ID_ALERT);
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private boolean connectionPresent () {
		final ConnectivityManager cMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
		if ((netInfo != null) && (netInfo.getState() != null)) {
			return netInfo.getState().equals(State.CONNECTED);
		}
		return false;
	}

}
