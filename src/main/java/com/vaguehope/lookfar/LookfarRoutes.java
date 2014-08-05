package com.vaguehope.lookfar;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.twitter.TwitterPoster;

import twitter4j.TwitterException;

public class LookfarRoutes extends RouteBuilder {

	private static final String URL_ENVVAR = "CLOUDAMQP_URL";
	private static final String EXCHANGE_NAME = "lookfar";
	private static final String QUEUE_TWEETS = "tweets";

	private static final Logger LOG = LoggerFactory.getLogger(LookfarRoutes.class);

	private final URI cloudAmqpUri;

	private Endpoint tweetsQueueEndpoint;
	private ProducerTemplate producerTemplate;

	public LookfarRoutes () throws URISyntaxException {
		final String uriRaw = System.getenv(URL_ENVVAR);
		if (uriRaw != null) {
			this.cloudAmqpUri = new URI(uriRaw);
			if (!"amqp".equals(this.cloudAmqpUri.getScheme())) throw new IllegalArgumentException(URL_ENVVAR + " does not have scheme amqp.");
		}
		else {
			LOG.warn("RabbitMQ not configured as {} not set.", URL_ENVVAR);
			this.cloudAmqpUri = null;
		}
	}

	@Override
	public void configure () throws TwitterException {
		if (this.cloudAmqpUri == null) return;

		this.producerTemplate = getContext().createProducerTemplate();

		final String tweetsQueueUri = cloudAmqpUriToCamelUri(this.cloudAmqpUri, EXCHANGE_NAME, QUEUE_TWEETS);
		LOG.info("Connecting to {} ...", tweetsQueueUri);
		this.tweetsQueueEndpoint = getContext().getEndpoint(tweetsQueueUri);

		from(this.tweetsQueueEndpoint)
				.bean(new TwitterPoster());
	}

	public void sendTweet (final String body) {
		this.producerTemplate.sendBody(this.tweetsQueueEndpoint, body);
	}

	// amqp://abcdefgh:-abcdefghijklmnopqrstuvwxyzabcde@tiger.cloudamqp.com/vruyxkle
	// rabbitmq://localhost/A
	private static String cloudAmqpUriToCamelUri (final URI cloudAmqpUri, final String exchangeName, final String queueName) {
		String user = null, pass = null;
		if (cloudAmqpUri.getUserInfo() != null) {
			final String[] parts = cloudAmqpUri.getUserInfo().split(":");
			user = parts.length >= 1 ? parts[0] : null;
			pass = parts.length >= 2 ? parts[1] : null;
		}
		return String.format("rabbitmq://%s:5672/%s?username=%s&password=%s&vhost=%s&routingKey=%s",
				cloudAmqpUri.getHost(), exchangeName,
				user, pass, cloudAmqpUri.getPath().replaceFirst("^/", ""), queueName);
	}

}
