package com.vaguehope.lookfar.android;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

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

import com.vaguehope.lookfar.android.util.FileCreds;
import com.vaguehope.lookfar.android.util.HttpHelper;
import com.vaguehope.lookfar.android.util.HttpHelper.HttpCreds;

public class UpdateService extends IntentService {

	private static final int NOTIFICAITON_ID_ALERT = 101;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public UpdateService () {
		super("LookfarUpdateService");
	}

	@Override
	protected void onHandleIntent (Intent i) {
		Log.i(C.TAG, "UpdateService invoked.");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, C.TAG);
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
			StringBuilder title = new StringBuilder();
			StringBuilder msg = new StringBuilder();
			int count = 0;
			HttpCreds creds = new FileCreds(C.CONFIG_FILE_PATH);
			String json = HttpHelper.getUrlContent("https://lookfar.herokuapp.com/update", null, creds);
			JSONArray items = (JSONArray) new JSONTokener(json).nextValue();
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = (JSONObject) items.get(i);
				String flag = item.getString("flag");
				if (!"OK".equals(flag)) {
					count += 1;
					String node = item.getString("node");
					if (title.indexOf(node) < 0) {
						if (title.length() > 0) title.append(", ");
						title.append(node);
					}
					if (msg.length() > 0) msg.append(", ");
					msg.append(item.getString("key"));
				}
			}
			updateNotification(this, title.toString(), msg.toString(), count);
		}
		catch (Exception e) {
			updateNotification(this, "Lookfar update failed", e.getMessage(), 0);
		}
	}

	private static void updateNotification (Context context, CharSequence title, CharSequence msg, int count) {
		NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (msg != null && msg.length() > 0) {
			Intent notificationIntent = new Intent(context, MainActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

			Notification n = new Notification(R.drawable.service_notification, "Lookfar: " + title, System.currentTimeMillis());
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
		ConnectivityManager cMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
		if ((netInfo != null) && (netInfo.getState() != null)) {
			return netInfo.getState().equals(State.CONNECTED);
		}
		return false;
	}

}
