package com.vaguehope.lookfar.model;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Objects;
import com.vaguehope.lookfar.expire.ExpireStatus;
import com.vaguehope.lookfar.threshold.ThresholdStatus;

public class Update {

	private final String node;
	private final Date updated;
	private final String key;
	private final String value;
	private final String threshold;
	private final ThresholdStatus thresholdStatus;
	private final String expire;
	private final ExpireStatus expireStatus;

	public Update (String node, Date updated, String key, String value, String threshold, ThresholdStatus thresholdStatus, String expire, ExpireStatus expireStatus) {
		this.node = node;
		this.updated = updated;
		this.key = key;
		this.value = value;
		this.threshold = threshold;
		this.thresholdStatus = thresholdStatus;
		this.expire = expire;
		this.expireStatus = expireStatus;
	}

	public String getNode () {
		return this.node;
	}

	public Date getUpdated () {
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

	@JsonProperty("flag")
	public UpdateFlag calculateFlag () {
		return UpdateFlag.fromThresholdAndExpire(this.thresholdStatus, this.expireStatus);
	}

	@Override
	public String toString () {
		return Objects.toStringHelper(Update.class)
				.add("node", this.node)
				.add("updated", this.updated)
				.add("key", this.key)
				.add("value", this.value)
				.toString();
	}

	@Override
	public boolean equals (Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (!(o instanceof Update)) return false;
		Update that = (Update) o;
		return Objects.equal(this.node, that.node) &&
				Objects.equal(this.updated, that.updated) &&
				Objects.equal(this.key, that.key) &&
				Objects.equal(this.value, that.value);
	}

	@Override
	public int hashCode () {
		return Objects.hashCode(this.node, this.updated, this.key, this.value);
	}

}
