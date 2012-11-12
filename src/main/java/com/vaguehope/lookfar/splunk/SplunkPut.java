package com.vaguehope.lookfar.splunk;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SplunkPut implements Callable<Void> {

	private static final Logger LOG = LoggerFactory.getLogger(SplunkPut.class);

	private final String node;
	private final Map<String, String> data;
	private final URI endpoint;

	public SplunkPut (String node, Map<String, String> data, URI endpoint) {
		this.node = node;
		this.data = data;
		this.endpoint = endpoint;
	}

	@Override
	public Void call () {
		StringBuilder s = new StringBuilder();
		s.append("node=").append("\"").append(this.node).append("\"");
		for (Entry<String, String> e : this.data.entrySet()) {
			s.append(" ").append(e.getKey()).append("=").append("\"").append(e.getValue()).append("\"");
		}

		Socket sock = null;
		try {
			sock = new Socket();
			sock.connect(new InetSocketAddress(this.endpoint.getHost(), this.endpoint.getPort()));
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.write(s.toString());
			out.flush();
		}
		catch (IOException e) {
			LOG.warn("Failed to write to Splunk: {}", e.getMessage());
		}
		finally {
			if (sock != null) closeQuietly(sock);
		}

		return null;
	}

	private static void closeQuietly (Socket sock) {
		try {
			if (sock != null) {
				sock.close();
			}
		}
		catch (IOException ioe) {
			// ignore
		}
	}

}