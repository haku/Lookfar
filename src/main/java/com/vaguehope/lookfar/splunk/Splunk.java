package com.vaguehope.lookfar.splunk;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Splunk {

	private static final Logger LOG = LoggerFactory.getLogger(Splunk.class);

	private final URI endpoint;
	private final ExecutorService ex;

	public Splunk () throws URISyntaxException {
		String urlRaw = System.getenv("SPLUNK_URL");
		if (urlRaw != null && !urlRaw.isEmpty()) {
			URI uri = new URI(urlRaw);
			// FIXME verify host and port correct.
			LOG.info("Splunk: {} {}", uri.getHost(), String.valueOf(uri.getPort()));
			this.endpoint = uri;
		}
		else {
			LOG.warn("Splunk not configured.");
			this.endpoint = null;
		}
		this.ex = (this.endpoint == null ? null : Executors.newSingleThreadExecutor());
	}

	public void scheduleUpdate (String node, Map<String, String> data) {
		if (this.endpoint != null) this.ex.submit(new SplunkPut(node, data, this.endpoint));
	}

}
