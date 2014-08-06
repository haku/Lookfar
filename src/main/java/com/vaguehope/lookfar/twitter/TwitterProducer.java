package com.vaguehope.lookfar.twitter;

import java.sql.SQLException;
import java.util.ArrayList;
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

	public void onNewRawData (final String node, final Map<String, String> nextRawData) throws SQLException {
		final List<Update> prevData = this.dataStore.getUpdates(node);

		final List<Update> nextData = new ArrayList<Update>();
		for (final Entry<String, String> nextDatum : nextRawData.entrySet()) {
			final Update prevUpdate = findUpdate(prevData, nextDatum.getKey());
			if (prevUpdate == null) continue;
			nextData.add(this.updateFactory.makeUpdate(node, new Date(), nextDatum.getKey(), nextDatum.getValue(), prevUpdate.getThreshold(), prevUpdate.getExpire()));
		}

		onNewData(node, prevData, nextData);
	}

	public void onNewData (final String node, final List<Update> prevData, final List<Update> nextData) {
		StringBuilder tweet = null;

		for (final Update nextUpdate : nextData) {
			final Update prevUpdate = findUpdate(prevData, nextUpdate.getKey());
			if (prevUpdate == null) {
				LOG.warn("No previous update for node={} key={}", node, nextUpdate.getKey());
				continue;
			}

			final UpdateFlag nextFlag = nextUpdate.calculateFlag();
			if (nextFlag == UpdateFlag.OK) continue;

			final UpdateFlag prevFlag = prevUpdate.calculateFlag();
			if (nextFlag == prevFlag) continue;

			if (tweet == null) tweet = new StringBuilder(node);
			tweet.append(String.format(" | %s=%s %s --> %s", nextUpdate.getKey(), nextUpdate.getValue(), prevFlag, nextFlag));
		}

		if (tweet != null) {
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
