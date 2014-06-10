package com.vaguehope.lookfar.android;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.vaguehope.lookfar.android.model.Update;
import com.vaguehope.lookfar.android.util.EqualHelper;
import com.vaguehope.lookfar.android.util.FileHelper;

public class UpdateService extends IntentService {

	private static final int NOTIFICAITON_ID_ALERT = 101;
	private static final long IGNORE_UPDATES_ERRORS_MAX_DATA_AGE_SECONDS = TimeUnit.HOURS.toSeconds(1);

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
			try {
				if (connectionPresent()) {
					checkUpdates();
				}
				else {
					Log.i(C.TAG, "No connection, aborted.");
				}
			}
			catch (final UnknownHostException e) {
				final long ageSeconds = getPreviousUpdatesAgeSeconds(this);
				if (ageSeconds < 1 || ageSeconds > IGNORE_UPDATES_ERRORS_MAX_DATA_AGE_SECONDS) throw e;
				Log.i(C.TAG, String.format("Ignoring update error as data only %ss old: %s", ageSeconds, e));
			}
		}
		catch (final Exception e) {
			Log.i(C.TAG, "UpdateService failed: " + e);
			updateNotification(this, "Lookfar update failed", e.getMessage(), 0);
		}
		finally {
			wl.release();
			Log.i(C.TAG, "UpdateService end.");
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private void checkUpdates () throws IOException, JSONException {
		final String newUpdatesJson = new Client().fetchUnparsed();
		final Map<String, Update> newUpdates = updatesMap(Client.parseUpdatesJson(newUpdatesJson));
		final Map<String, Update> prevUpdates = updatesMap(getPreviousUpdates(this));
		compareUpdatesAndAlert(prevUpdates, newUpdates);
		putPreviousUpdates(this, newUpdatesJson);
	}

	private void compareUpdatesAndAlert (final Map<String, Update> prevUpdates, final Map<String, Update> newUpdates) {
		final StringBuilder title = new StringBuilder();
		final StringBuilder msg = new StringBuilder();
		int count = 0;

		for (final Entry<String, Update> nue : newUpdates.entrySet()) {
			final Update pu = prevUpdates.get(nue.getKey());
			if (pu == null) continue;
			final Update nu = nue.getValue();
			if ("OK".equals(nu.getFlag())) continue;
			if (EqualHelper.equal(nu.getFlag(), pu.getFlag())) continue;

			count += 1;

			if (title.indexOf(nu.getNode()) < 0) {
				if (title.length() > 0) title.append(", ");
				title.append(nu.getNode());
			}

			if (msg.length() > 0) msg.append(", ");
			msg.append(nu.getKey());
		}

		Log.i(C.TAG, String.format("title{%s} msg{%s}.", title, msg));
		updateNotification(this, title.toString(), msg.toString(), count);
	}

	private static Map<String, Update> updatesMap (final List<Update> updates) {
		final Map<String, Update> ret = new HashMap<String, Update>();
		if (updates != null) for (final Update update : updates) {
			ret.put(String.format("%s/%s", update.getNode(), update.getKey()), update);
		}
		return ret;
	}

	private static void updateNotification (final Context context, final CharSequence title, final CharSequence msg, final int count) {
		final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (msg != null && msg.length() > 0) {
			final Intent notificationIntent = new Intent(context, MainActivity.class);
			final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

			final Builder nb = new NotificationCompat.Builder(context)
					.setOnlyAlertOnce(true)
					.setSmallIcon(R.drawable.service_notification)
					.setTicker("Lookfar" + (title != null ? ": " + title : ""))
					.setContentTitle(title != null ? title : "Lookfar")
					.setContentText(msg)
					.setNumber(count)
					.setContentIntent(contentIntent)
					.setAutoCancel(true)
					.setWhen(System.currentTimeMillis())
					.setDefaults(Notification.DEFAULT_ALL);
			nm.notify(NOTIFICAITON_ID_ALERT, nb.build());
		}
		else {
			nm.cancel(NOTIFICAITON_ID_ALERT);
		}
	}

	private static void putPreviousUpdates (final Context context, final String updatesJson) throws IOException {
		FileHelper.stringToFile(getLastUpdatesFile(context), updatesJson);
	}

	private static long getPreviousUpdatesAgeSeconds (final Context context) {
		final File file = getLastUpdatesFile(context);
		if (!file.exists()) return 0L;
		return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - file.lastModified());
	}

	private static List<Update> getPreviousUpdates (final Context context) throws IOException, JSONException {
		final File file = getLastUpdatesFile(context);
		if (!file.exists()) return null;
		return Client.parseUpdatesJson(FileHelper.fileToString(file));
	}

	private static File getLastUpdatesFile (final Context context) {
		return new File(context.getCacheDir(), "last_updates.json");
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
