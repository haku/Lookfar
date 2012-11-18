package com.vaguehope.lookfar.splunk;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

public class SplunkProducer {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkProducer.class);

	private final Splunk splunk;
	private final Channel chan;
	private final String queue;

	public SplunkProducer (Splunk splunk, Channel chan, String queue) {
		this.splunk = splunk;
		this.chan = chan;
		this.queue = queue;
	}

	public void scheduleUpdate (String node, Map<String, String> data) {
		if (this.splunk.isEnabled()) {
			String body = Splunk.splunkDataString(node, data, System.currentTimeMillis());
			//this.splunk.getExcutor().submit(new SplunkDirectPut(body, this.splunk, this));
			try {
				rescheduleUpdate(body);
			}
			catch (IOException e) {
				LOG.warn("Failed to reschedule update: {}", e.getMessage());
			}
		}
	}

	void rescheduleUpdate (String body) throws IOException {
		this.chan.basicPublish("", this.queue, null, body.getBytes());
	}

}
