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
import com.vaguehope.lookfar.android.util.LogWrapper;
import com.vaguehope.lookfar.android.util.StringHelper;

public class Client {

	private static final LogWrapper LOG = new LogWrapper("CL");

	private static final String HOST = "lookfar.herokuapp.com";
	private static final String VERB_POST = "POST";
	private static final String VERB_DELETE = "DELETE";
	private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded.";

	private final HttpCreds creds;

	public Client () throws IOException {
		this.creds = new FileCreds(C.CONFIG_FILE_PATH);
	}

	public List<Update> fetch () throws IOException, JSONException {
		return parseUpdatesJson(fetchUnparsed());
	}

	public String fetchUnparsed () throws IOException {
		return HttpHelper.getUrlContent(MessageFormat.format("https://{0}/update", HOST), this.creds);
	}

	public void setThrshold (final Update update, final String newThreshold) throws IOException {
		checkNodeAndKey(update);
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}/threshold", HOST, update.getNode(), update.getKey()),
				VERB_POST, newThreshold, APPLICATION_FORM_URLENCODED, this.creds);
		LOG.i("SET %s %s t=%s.", update.getNode(), update.getKey(), newThreshold);
	}

	public void deleteThreshold (final Update update) throws IOException {
		checkNodeAndKey(update);
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}/threshold", HOST, update.getNode(), update.getKey()),
				VERB_DELETE, null, null, this.creds);
		LOG.i("DEL %s %s t.", update.getNode(), update.getKey());
	}

	public void setExpire (final Update update, final String newExpire) throws IOException {
		checkNodeAndKey(update);
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}/expire", HOST, update.getNode(), update.getKey()),
				VERB_POST, newExpire, APPLICATION_FORM_URLENCODED, this.creds);
		LOG.i("SET %s %s e=%s.", update.getNode(), update.getKey(), newExpire);
	}

	public void deleteExpire (final Update update) throws IOException {
		checkNodeAndKey(update);
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}/expire", HOST, update.getNode(), update.getKey()),
				VERB_DELETE, null, null, this.creds);
		LOG.i("DEL %s %s e.", update.getNode(), update.getKey());
	}

	public void deleteUpdate (final Update update) throws IOException {
		checkNodeAndKey(update);
		HttpHelper.getUrlContent(MessageFormat.format("https://{0}/node/{1}/{2}", HOST, update.getNode(), update.getKey()),
				VERB_DELETE, null, null, this.creds);
		LOG.i("DEL %s %s.", update.getNode(), update.getKey());
	}

	public static List<Update> parseUpdatesJson (final String json) throws JSONException {
		final JSONArray items = (JSONArray) new JSONTokener(json).nextValue();
		final List<Update> updates = new ArrayList<Update>();
		for (int i = 0; i < items.length(); i++) {
			updates.add(Update.parseJson(items.getJSONObject(i)));
		}
		return updates;
	}

	private static void checkNodeAndKey (final Update update) {
		if (StringHelper.isEmpty(update.getNode())) throw new IllegalArgumentException("Update has empty node.");
		if (StringHelper.isEmpty(update.getKey())) throw new IllegalArgumentException("Update has empty key.");
	}

}
