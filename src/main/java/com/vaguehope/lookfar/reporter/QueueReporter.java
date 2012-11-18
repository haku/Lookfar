package com.vaguehope.lookfar.reporter;

import java.io.IOException;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;

public class QueueReporter implements ReportProvider {

	private final Channel chan;
	private final String queue;

	public QueueReporter (Channel chan, String queue) {
		this.chan = chan;
		this.queue = queue;
	}

	@Override
	public void appendReport (StringBuilder r) {
		r.append("queue:").append(this.queue);
		try {
			DeclareOk d = this.chan.queueDeclarePassive(this.queue);
			r.append(".count=").append(d.getMessageCount()).append(":");
		}
		catch (IOException e) {
			r.append(" unreachable.");
		}
	}

}
