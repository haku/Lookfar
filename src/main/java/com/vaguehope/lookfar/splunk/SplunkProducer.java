package com.vaguehope.lookfar.splunk;

import java.util.Map;

public class SplunkProducer {

	private final Splunk splunk;

	public SplunkProducer (final Splunk splunk) {
		this.splunk = splunk;
	}

	public void scheduleUpdate (final String node, final Map<String, String> data) {
		if (this.splunk.isEnabled()) {
			String body = Splunk.splunkDataString(node, data, System.currentTimeMillis());
			this.splunk.getExcutor().submit(new SplunkDirectPut(body, this.splunk));
		}
	}

}
