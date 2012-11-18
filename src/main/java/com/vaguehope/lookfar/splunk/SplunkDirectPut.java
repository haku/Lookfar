package com.vaguehope.lookfar.splunk;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SplunkDirectPut implements Callable<Void> {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkDirectPut.class);

	private final String data;
	private final Splunk splunk;
	private final SplunkProducer splunkProducer;

	public SplunkDirectPut (String data, Splunk splunk, SplunkProducer splunkProducer) {
		this.data = data;
		this.splunk = splunk;
		this.splunkProducer = splunkProducer;
	}

	@Override
	public Void call () {
		try {
			this.splunk.writeUpdate(this.data);
		}
		catch (Exception e) {
			reschedule();
		}
		return null;
	}

	private void reschedule () {
		try {
			this.splunkProducer.rescheduleUpdate(this.data);
		}
		catch (IOException e) {
			LOG.warn("Failed to reschedule update: {}", e.getMessage());
		}
	}

}