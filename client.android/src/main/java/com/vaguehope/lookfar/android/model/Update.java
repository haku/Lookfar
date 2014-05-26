package com.vaguehope.lookfar.android.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Update {

	private final String node;
	private final long updated;
	private final String key;
	private final String value;
	private final String threshold;
	private final String expire;
	private final String flag;

	public Update (final String node, final long updated, final String key, final String value, final String threshold, final String expire, final String flag) {
		this.node = node;
		this.updated = updated;
		this.key = key;
		this.value = value;
		this.threshold = threshold;
		this.expire = expire;
		this.flag = flag;
	}

	public String getNode () {
		return this.node;
	}

	public long getUpdated () {
		return this.updated;
	}

	public String getKey () {
		return this.key;
	}

	public String getValue () {
		return this.value;
	}

	public String getThreshold () {
		return this.threshold;
	}

	public String getExpire () {
		return this.expire;
	}

	public String getFlag () {
		return this.flag;
	}

	public static Update parseJson (final String json) throws JSONException {
		if (json == null) return null;
		return parseJson((JSONObject) new JSONTokener(json).nextValue());
	}

	public static Update parseJson (final JSONObject json) throws JSONException {
		final String node = json.getString("node");
		final long updated = json.optLong("updated");
		final String key = json.getString("key");
		final String value = json.getString("value");
		final String threshold = json.getString("threshold");
		final String expire = json.getString("expire");
		final String flag = json.getString("flag");
		return new Update(node, updated, key, value, threshold, expire, flag);
	}

}
