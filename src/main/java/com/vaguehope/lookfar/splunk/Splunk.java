package com.vaguehope.lookfar.splunk;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.reporter.ReportProvider;
import com.vaguehope.lookfar.util.IoHelper;

public class Splunk {

	private static final int CONNECT_TIMEOUT_MILLIS = 20000;
	private static final Logger LOG = LoggerFactory.getLogger(Splunk.class);

	private final SocketAddress endpoint;
	private final ExecutorService ex;
	private final AtomicInteger updatesWritten = new AtomicInteger();

	public Splunk () throws URISyntaxException {
		this.endpoint = getEndpoint();
		this.ex = (this.endpoint == null ? null : Executors.newSingleThreadExecutor());
	}

	public boolean isEnabled () {
		return this.endpoint != null;
	}

	public ReportProvider getSplunkRepoter () {
		return new ReportProvider() {
			@Override
			public void appendReport (StringBuilder r) {
				r.append(getUpdatesWritten()).append(" Splunk updates written.");
			}
		};
	}

	ExecutorService getExcutor () {
		return this.ex;
	}

	int getUpdatesWritten () {
		return this.updatesWritten.intValue();
	}

	void writeUpdate (String data) throws IOException {
		Socket sock = null;
		try {
			sock = new Socket();
			sock.connect(this.endpoint, CONNECT_TIMEOUT_MILLIS);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.write(data);
			out.flush();
			this.updatesWritten.incrementAndGet();
		}
		finally {
			if (sock != null) IoHelper.closeQuietly(sock);
		}
	}

	static String splunkDataString (String node, Map<String, String> data, long timeMillis) {
		StringBuilder s = new StringBuilder();
		s.append("node=").append("\"").append(node).append("\"");
		for (Entry<String, String> e : data.entrySet()) {
			s.append(" ").append(e.getKey()).append("=").append("\"").append(e.getValue()).append("\"");
		}
		s.append(" mtime=\"").append(timeMillis / 1000L).append("\"");
		s.append("\n");
		return s.toString();
	}

	private static InetSocketAddress getEndpoint () throws URISyntaxException {
		String urlRaw = System.getenv("SPLUNK_URL");
		if (urlRaw != null && !urlRaw.isEmpty()) {
			URI uri = new URI(urlRaw);
			// FIXME verify host and port correct.
			LOG.info("Splunk: {} {}", uri.getHost(), String.valueOf(uri.getPort()));
			return new InetSocketAddress(uri.getHost(), uri.getPort());
		}
		LOG.warn("Splunk not configured.");
		return null;
	}

}
