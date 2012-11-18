package com.vaguehope.lookfar.splunk;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

final class SplunkConsumer extends DefaultConsumer {

	private static final long BACKOFF_DELAY = 60000L;
	private static final Logger LOG = LoggerFactory.getLogger(SplunkConsumer.class);

	private final Splunk splunk;

	public SplunkConsumer (Channel channel, Splunk splunk) {
		super(channel);
		this.splunk = splunk;
	}

	@Override
	public void handleDelivery (String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bodyBytes) throws IOException {
		try {
			String body = new String(bodyBytes);
			this.splunk.writeUpdate(body);
			this.getChannel().basicAck(envelope.getDeliveryTag(), false);
		}
		catch (Exception e) {
			LOG.warn("Rollback and sleep: {}", e.getMessage());
			this.getChannel().basicNack(envelope.getDeliveryTag(), false, true);
			quietSleep(BACKOFF_DELAY);
		}
	}

	private static void quietSleep (long l) {
		try {
			Thread.sleep(l);
		}
		catch (InterruptedException e) {/* Ignore. */}
	}
}
