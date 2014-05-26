package com.vaguehope.lookfar.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Life cycle events.

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		wireGui();

		AlarmReceiver.configureAlarm(this);
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
		Button btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				startService(new Intent(MainActivity.this, UpdateService.class));
			}
		});
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
