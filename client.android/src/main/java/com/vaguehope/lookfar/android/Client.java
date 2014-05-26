package com.vaguehope.lookfar.android;

import java.io.IOException;
import java.text.MessageFormat;
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

	private static final String HOST = "lookfar.herokuapp.com";
	private static final String VERB_POST = "POST";
	private static final String VERB_DELETE = "DELETE";
	private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded.";

	private final HttpCreds creds;

	public Client () throws IOException {
		this.creds = new FileCreds(C.CONFIG_FILE_PATH);
	}

	public List<Update> fetch () throws IOException, JSONException {
		final String json = HttpHelper.getUrlContent(MessageFormat.format("https://{0}/update", HOST), this.creds);
		final JSONArray items = (JSONArray) new JSONTokener(json).nextValue();
		final List<Update> updates = new ArrayList<Update>();
		for (int i = 0; i < items.length(); i++) {
			updates.add(Update.parseJson(items.getJSONObject(i)));
		}
		return updates;
	}

	public void setThrshold (final Update update, final String newThreshold) throws IOException {
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}/threshold", HOST, update.getNode(), update.getKey()),
				VERB_POST, newThreshold, APPLICATION_FORM_URLENCODED, this.creds);
	}

	public void deleteThreshold (final Update update) throws IOException {
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}/threshold", HOST, update.getNode(), update.getKey()),
				VERB_DELETE, null, null, this.creds);
	}

	public void setExpire (final Update update, final String newExpire) throws IOException {
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}/expire", HOST, update.getNode(), update.getKey()),
				VERB_POST, newExpire, APPLICATION_FORM_URLENCODED, this.creds);
	}

	public void deleteExpire (final Update update) throws IOException {
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}/expire", HOST, update.getNode(), update.getKey()),
				VERB_DELETE, null, null, this.creds);
	}

}
