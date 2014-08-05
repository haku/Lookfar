package com.vaguehope.lookfar.twitter;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Update;
import com.vaguehope.lookfar.model.UpdateFactory;
import com.vaguehope.lookfar.model.UpdateFlag;
import com.vaguehope.lookfar.reporter.ReportProvider;

public class TwitterProducer {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterProducer.class);

	private final DataStore dataStore;
	private final UpdateFactory updateFactory;
	private final TwitterRoutes lookfarRoutes;

	private final AtomicInteger tweetsProduced = new AtomicInteger();

	public TwitterProducer (final DataStore dataStore, final UpdateFactory updateFactory, final TwitterRoutes lookfarRoutes) {
		this.dataStore = dataStore;
		this.updateFactory = updateFactory;
		this.lookfarRoutes = lookfarRoutes;
	}

	public ReportProvider getReporter () {
		return new ReportProvider() {
			@Override
			public void appendReport (final StringBuilder r) {
				r.append(getTweetsProduced()).append(" tweets produced.");
			}
		};
	}

	protected int getTweetsProduced () {
		return this.tweetsProduced.get();
	}

	public void scheduleUpdate (final String node, final Map<String, String> nextData) throws SQLException {
		final StringBuilder tweet = new StringBuilder();

		final List<Update> prevData = this.dataStore.getUpdates(node);
		for (final Entry<String, String> nextDatum : nextData.entrySet()) {
			final Update prevUpdate = findUpdate(prevData, nextDatum.getKey());
			if (prevUpdate == null) {
				LOG.warn("No previous update for node={} key={}", node, nextDatum.getKey());
				continue;
			}

			final Update nextUpdate = this.updateFactory.makeUpdate(node, new Date(), nextDatum.getKey(), nextDatum.getValue(), prevUpdate.getThreshold(), prevUpdate.getExpire());
			final UpdateFlag nextFlag = nextUpdate.calculateFlag();
			if (nextFlag == UpdateFlag.OK) continue;

			final UpdateFlag prevFlag = prevUpdate.calculateFlag();
			if (nextFlag == prevFlag) continue;

			if (tweet.length() < 1) tweet.append(node);
			tweet.append(String.format(" | %s=%s %s --> %s", nextUpdate.getKey(), nextUpdate.getValue(), prevFlag, nextFlag));
		}

		if (tweet.length() > 0) {
			this.lookfarRoutes.sendTweet(tweet.toString());
			this.tweetsProduced.incrementAndGet();
		}
	}

	private static Update findUpdate (final List<Update> updates, final String key) {
		for (final Update update : updates) {
			if (key.equals(update.getKey())) return update;
		}
		return null;
	}

}
