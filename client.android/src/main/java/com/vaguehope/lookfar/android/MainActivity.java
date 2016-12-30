package com.vaguehope.lookfar.android;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.vaguehope.lookfar.android.model.Update;
import com.vaguehope.lookfar.android.model.UpdateAdapter;
import com.vaguehope.lookfar.android.util.DialogHelper;
import com.vaguehope.lookfar.android.util.LogWrapper;
import com.vaguehope.lookfar.android.util.Result;

public class MainActivity extends Activity {

	private static final LogWrapper LOG = new LogWrapper("MA");
	private ListView lvUpdates;
	private UpdateAdapter lvUpdatesAdapter;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Life cycle events.

	@Override
	public void onCreate (final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		wireGui();

		//AlarmReceiver.configureAlarm(this);
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
		this.lvUpdates = (ListView) findViewById(R.id.lvUpdates);
		this.lvUpdates.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick (final AdapterView<?> parent, final View view, final int position, final long id) {
				final Update update = (Update) MainActivity.this.lvUpdates.getAdapter().getItem(position);
				UpdateDetailsDialog.show(MainActivity.this, update);
			}
		});
		this.lvUpdates.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick (final AdapterView<?> parent, final View view, final int position, final long id) {
				final Update update = (Update) MainActivity.this.lvUpdates.getAdapter().getItem(position);
				UpdateDetailsDialog.askDeleteUpdate(MainActivity.this, update);
				return true;
			}
		});

		this.lvUpdatesAdapter = new UpdateAdapter(this);
		this.lvUpdates.setAdapter(this.lvUpdatesAdapter);
		new FetchUpdates(this, this.lvUpdatesAdapter).execute();
	}

	@Override
	public boolean onCreateOptionsMenu (final Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected (final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mnuRefresh:
				new FetchUpdates(this, this.lvUpdatesAdapter).execute();
				return true;
			case R.id.mnuShowAll:
				item.setChecked(!item.isChecked());
				item.setTitle(item.isChecked() ? "Fliter" : "All");
				this.lvUpdatesAdapter.setShowAll(item.isChecked());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static class FetchUpdates extends AsyncTask<Void, Void, Result<List<Update>>> {

		private final Context context;
		private final UpdateAdapter lvUpdatesAdapter;

		private ProgressDialog dialog;

		public FetchUpdates (final Context context, final UpdateAdapter lvUpdatesAdapter) {
			this.context = context;
			this.lvUpdatesAdapter = lvUpdatesAdapter;
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
				this.lvUpdatesAdapter.setData(result.getData());
			}
			else {
				LOG.e("Failed to fetch updates.", result.getE());
				DialogHelper.alert(this.context, result.getE());
			}
		}

	}

}
