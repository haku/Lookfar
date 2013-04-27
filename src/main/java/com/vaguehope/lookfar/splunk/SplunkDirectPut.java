package com.vaguehope.lookfar.splunk;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SplunkDirectPut implements Callable<Void> {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkDirectPut.class);

	private final String data;
	private final Splunk splunk;

	public SplunkDirectPut (final String data, final Splunk splunk) {
		this.data = data;
		this.splunk = splunk;
	}

	@Override
	public Void call () {
		try {
			this.splunk.writeUpdate(this.data);
		}
		catch (Exception e) { // NOSONAR
			LOG.warn("Failed to send to Splunk: {}", e.getMessage());
		}
		return null;
	}

}