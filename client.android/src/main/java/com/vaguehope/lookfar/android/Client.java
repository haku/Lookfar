package com.vaguehope.lookfar.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import com.vaguehope.lookfar.android.model.Update;
import com.vaguehope.lookfar.android.util.FileCreds;
import com.vaguehope.lookfar.android.util.HttpHelper;
import com.vaguehope.lookfar.android.util.HttpHelper.HttpCreds;

public class Client {

	private final HttpCreds creds;

	public Client () throws IOException {
		this.creds = new FileCreds(C.CONFIG_FILE_PATH);
	}

	public List<Update> fetch () throws IOException, JSONException {
		final String json = HttpHelper.getUrlContent("https://lookfar.herokuapp.com/update", this.creds);
		final JSONArray items = (JSONArray) new JSONTokener(json).nextValue();
		final List<Update> updates = new ArrayList<Update>();
		for (int i = 0; i < items.length(); i++) {
			updates.add(Update.parseJson(items.getJSONObject(i)));
		}
		return updates;
	}

}
