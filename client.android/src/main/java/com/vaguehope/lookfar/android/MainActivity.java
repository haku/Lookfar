package com.vaguehope.lookfar.android;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.vaguehope.lookfar.android.model.Update;
import com.vaguehope.lookfar.android.model.UpdateAdapter;
import com.vaguehope.lookfar.android.util.DialogHelper;
import com.vaguehope.lookfar.android.util.LogWrapper;
import com.vaguehope.lookfar.android.util.Result;

public class MainActivity extends Activity {

	private static final LogWrapper LOG = new LogWrapper("MA");

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Life cycle events.

	@Override
	public void onCreate (final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		wireGui();
	}

	@Override
	protected void onResume () {
		super.onResume();
	}

	@Override
	protected void onPause () {
		super.onPause();
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	GUI setup and helpers.

	private void wireGui () {
		final ListView lvUpdates = (ListView) findViewById(R.id.lvUpdates);
		lvUpdates.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick (final AdapterView<?> parent, final View view, final int position, final long id) {
				final Update update = (Update) lvUpdates.getAdapter().getItem(position);
				UpdateDetailsDialog.show(MainActivity.this, update);
			}
		});
		lvUpdates.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick (final AdapterView<?> parent, final View view, final int position, final long id) {
				final Update update = (Update) lvUpdates.getAdapter().getItem(position);
				UpdateDetailsDialog.askDeleteUpdate(MainActivity.this, update);
				return true;
			}
		});

		final Button btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (final View v) {
				new FetchUpdates(MainActivity.this, lvUpdates).execute();
			}
		});

		if (lvUpdates.getAdapter() == null) new FetchUpdates(MainActivity.this, lvUpdates).execute();
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static class FetchUpdates extends AsyncTask<Void, Void, Result<List<Update>>> {

		private final Context context;
		private final ListView lvUpdates;

		private ProgressDialog dialog;

		public FetchUpdates (final Context context, final ListView lvUpdates) {
			this.context = context;
			this.lvUpdates = lvUpdates;
		}

		@Override
		protected void onPreExecute () {
			this.dialog = ProgressDialog.show(this.context, "Updates", "Fetching...", true);
		}

		@Override
		protected Result<List<Update>> doInBackground (final Void... params) {
			try {
				return new Result<List<Update>>(new Client().fetch());
			}
			catch (final Exception e) {
				return new Result<List<Update>>(e);
			}
		}

		@Override
		protected void onPostExecute (final Result<List<Update>> result) {
			this.dialog.dismiss();
			if (result.isSuccess()) {
				this.lvUpdates.setAdapter(new UpdateAdapter(this.context, result.getData()));
			}
			else {
				LOG.e("Failed to fetch updates.", result.getE());
				DialogHelper.alert(this.context, result.getE());
			}
		}

	}

}
