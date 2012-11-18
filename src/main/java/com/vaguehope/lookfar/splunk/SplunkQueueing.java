package com.vaguehope.lookfar.splunk;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.vaguehope.lookfar.reporter.QueueReporter;
import com.vaguehope.lookfar.reporter.ReportProvider;

public class SplunkQueueing {

	private static final String CONSUMER_TAG = "lookfar";
	private static final String QUEUE_SPLUNK = "splunk";

	private final Splunk splunk;
	private final Channel chan;

	public SplunkQueueing (Splunk splunk) throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException {
		this.splunk = splunk;
		Connection conn = getConnection(Executors.newSingleThreadExecutor());
		this.chan = conn.createChannel();
		this.chan.basicQos(1); // prefetch = 1.
		this.chan.queueDeclare(QUEUE_SPLUNK, true, false, false, null);
		this.chan.basicConsume(QUEUE_SPLUNK, false, CONSUMER_TAG, new SplunkConsumer(this.chan, splunk));
	}

	public SplunkProducer getSplunkProducer () {
		return new SplunkProducer(this.splunk, this.chan, QUEUE_SPLUNK);
	}

	public ReportProvider getSplunkQueueRepoter () {
		return new QueueReporter(this.chan, QUEUE_SPLUNK);
	}

	private static Connection getConnection (ExecutorService ex) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException {
		String urlEnv = System.getenv("CLOUDAMQP_URL");
		if (urlEnv == null) throw new IllegalStateException("Env var CLOUDAMQP_URL not set.");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(urlEnv);
		return factory.newConnection(ex);
	}

}
