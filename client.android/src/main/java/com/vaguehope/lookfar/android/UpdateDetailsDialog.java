package com.vaguehope.lookfar.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.vaguehope.lookfar.android.model.Update;
import com.vaguehope.lookfar.android.util.LogWrapper;

public class UpdateDetailsDialog {

	private static final LogWrapper LOG = new LogWrapper("UDD");

	public static void show (final Context context, final Update update) {
		final UpdateDetailsDialog udd = new UpdateDetailsDialog(context, update);
		final AlertDialog.Builder bld = new AlertDialog.Builder(context);
		bld.setTitle(String.format("%s: %s",update.getNode(), update.getKey()));
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

		final Button btnExpire = (Button) this.llParent.findViewById(R.id.btnExpire);
		btnExpire.setText(update.getExpire());
	}

	private void setDialog (final Dialog dialog) {
		this.dialog = dialog;
	}

	private View getRootView () {
		return this.llParent;
	}

}
