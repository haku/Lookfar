package com.vaguehope.lookfar.android;

import java.io.IOException;
import java.text.MessageFormat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.vaguehope.lookfar.android.model.Update;
import com.vaguehope.lookfar.android.util.DialogHelper;
import com.vaguehope.lookfar.android.util.DialogHelper.Listener;
import com.vaguehope.lookfar.android.util.LogWrapper;

public class UpdateDetailsDialog {

	private static final LogWrapper LOG = new LogWrapper("UDD");

	public static void show (final Context context, final Update update) {
		final UpdateDetailsDialog udd = new UpdateDetailsDialog(context, update);
		final AlertDialog.Builder bld = new AlertDialog.Builder(context);
		bld.setTitle(String.format("%s: %s", update.getNode(), update.getKey()));
		bld.setView(udd.getRootView());
		final AlertDialog dlg = bld.create();
		udd.setDialog(dlg);
		dlg.show();
	}

	private final Context context;
	private final Update update;
	private final View llParent;
	private Dialog dialog;

	public UpdateDetailsDialog (final Context context, final Update update) {
		this.context = context;
		this.update = update;

		final LayoutInflater inflater = LayoutInflater.from(context);
		this.llParent = inflater.inflate(R.layout.updatedetails, null);

		final Button btnThreshold = (Button) this.llParent.findViewById(R.id.btnThreshold);
		btnThreshold.setText(update.getThreshold());
		btnThreshold.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (final View v) {
				askThreshold();
			}
		});

		((Button) this.llParent.findViewById(R.id.btnThresholdDelete)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (final View v) {
				askThresholdDelete();
			}
		});

		final Button btnExpire = (Button) this.llParent.findViewById(R.id.btnExpire);
		btnExpire.setText(update.getExpire());
		btnExpire.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (final View v) {
				askExpire();
			}
		});

		((Button) this.llParent.findViewById(R.id.btnExpireDelete)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (final View v) {
				askExpireDelete();
			}
		});

		((Button) this.llParent.findViewById(R.id.btnReset)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (final View v) {
				askClearUpdateUpdated();
			}
		});
	}

	private void setDialog (final Dialog dialog) {
		this.dialog = dialog;
	}

	private View getRootView () {
		return this.llParent;
	}

	protected void askThreshold () {
		DialogHelper.askString(this.context, "Threshold:", this.update.getThreshold(), false, false, new Listener<String>() {
			@Override
			public void onAnswer (final String answer) {
				new SetThrshold(UpdateDetailsDialog.this.context, UpdateDetailsDialog.this.update, answer).execute();
			}
		});
	}

	protected void askThresholdDelete () {
		DialogHelper.askYesNo(this.context, "Delete threshold?", "delete", "cancel", new Runnable() {
			@Override
			public void run () {
				new DeleteThreshold(UpdateDetailsDialog.this.context, UpdateDetailsDialog.this.update).execute();
			}
		});
	}

	protected void askExpire () {
		DialogHelper.askString(this.context, "Expire:", this.update.getExpire(), false, false, new Listener<String>() {
			@Override
			public void onAnswer (final String answer) {
				new SetExpire(UpdateDetailsDialog.this.context, UpdateDetailsDialog.this.update, answer).execute();
			}
		});
	}

	protected void askExpireDelete () {
		DialogHelper.askYesNo(this.context, "Delete expire?", "delete", "cancel", new Runnable() {
			@Override
			public void run () {
				new DeleteExpire(UpdateDetailsDialog.this.context, UpdateDetailsDialog.this.update).execute();
			}
		});
	}

	protected void askClearUpdateUpdated () {
		DialogHelper.askYesNo(this.context, "Reset to PENDING status?", "reset", "cancel", new Runnable() {
			@Override
			public void run () {
				new ClearUpdateUpdated(UpdateDetailsDialog.this.context, UpdateDetailsDialog.this.update).execute();
			}
		});
	}

	protected static void askDeleteUpdate (final Context context, final Update update) {
		DialogHelper.askYesNo(context, MessageFormat.format("Delete update {0} / {1}?", update.getNode(), update.getKey()),
				"delete", "cancel", new Runnable() {
					@Override
					public void run () {
						new DeleteUpdate(context, update).execute();
					}
				});
	}

	private static class SetThrshold extends ModifyUpdate {

		private final String newThreshold;

		public SetThrshold (final Context context, final Update update, final String newThreshold) {
			super(context, update);
			this.newThreshold = newThreshold;
		}

		@Override
		protected void modifyUpdate (final Client client) throws IOException {
			client.setThrshold(this.update, this.newThreshold);
		}

	}

	private static class DeleteThreshold extends ModifyUpdate {

		public DeleteThreshold (final Context context, final Update update) {
			super(context, update);
		}

		@Override
		protected void modifyUpdate (final Client client) throws IOException {
			client.deleteThreshold(this.update);
		}

	}

	private static class SetExpire extends ModifyUpdate {

		private final String newExpire;

		public SetExpire (final Context context, final Update update, final String newExpire) {
			super(context, update);
			this.newExpire = newExpire;
		}

		@Override
		protected void modifyUpdate (final Client client) throws IOException {
			client.setExpire(this.update, this.newExpire);
		}

	}

	private static class DeleteExpire extends ModifyUpdate {

		public DeleteExpire (final Context context, final Update update) {
			super(context, update);
		}

		@Override
		protected void modifyUpdate (final Client client) throws IOException {
			client.deleteExpire(this.update);
		}

	}

	private static class ClearUpdateUpdated extends ModifyUpdate {

		public ClearUpdateUpdated (final Context context, final Update update) {
			super(context, update);
		}

		@Override
		protected void modifyUpdate (final Client client) throws IOException {
			client.clearUpdateUpdated(this.update);
		}

	}

	private static class DeleteUpdate extends ModifyUpdate {

		public DeleteUpdate (final Context context, final Update update) {
			super(context, update);
		}

		@Override
		protected void modifyUpdate (final Client client) throws IOException {
			client.deleteUpdate(this.update);
		}

	}

	private static abstract class ModifyUpdate extends AsyncTask<Void, Void, Exception> {

		protected final Context context;
		protected final Update update;

		private ProgressDialog dialog;

		public ModifyUpdate (final Context context, final Update update) {
			this.context = context;
			this.update = update;
		}

		@Override
		protected void onPreExecute () {
			this.dialog = ProgressDialog.show(this.context, "Update", "Writing...", true);
		}

		protected abstract void modifyUpdate (Client client) throws IOException;

		@Override
		protected Exception doInBackground (final Void... params) {
			try {
				final Client client = new Client();
				modifyUpdate(client);
				return null;
			}
			catch (final Exception e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute (final Exception result) {
			this.dialog.dismiss();
			if (result != null) {
				LOG.e("Failed to modify update.", result);
				DialogHelper.alert(this.context, result);
			}
		}

	}

}
