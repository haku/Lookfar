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

	public static Update parseJson (final JSONObject json) {
		final String node = stringOrNull(json, "node");
		final long updated = longOr0(json, "updated");
		final String key = stringOrNull(json, "key");
		final String value = stringOrNull(json, "value");
		final String threshold = stringOrNull(json, "threshold");
		final String expire = stringOrNull(json, "expire");
		final String flag = stringOrNull(json, "flag");
		return new Update(node, updated, key, value, threshold, expire, flag);
	}

	private static String stringOrNull(final JSONObject json, final String key) {
		if (json.isNull(key)) return null;
		return json.optString(key);
	}

	private static long longOr0(final JSONObject json, final String key) {
		if (json.isNull(key)) return 0L;
		return json.optLong(key);
	}

}
