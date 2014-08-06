package com.vaguehope.lookfar.twitter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Node;
import com.vaguehope.lookfar.model.Update;
import com.vaguehope.lookfar.model.UpdateFlag;

public class TwitterTimeoutProducer {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterTimeoutProducer.class);

	private final DataStore dataStore;
	private final TwitterProducer twitterProducer;
	private final ScheduledExecutorService schEx;

	public TwitterTimeoutProducer (final DataStore dataStore, final TwitterProducer twitterProducer) {
		this.dataStore = dataStore;
		this.twitterProducer = twitterProducer;
		this.schEx = Executors.newScheduledThreadPool(1);
	}

	public void spawn () {
		this.schEx.scheduleWithFixedDelay(new UpdateTimeoutChecker(this.dataStore, this.twitterProducer), 1, 1, TimeUnit.MINUTES);
	}

	private static class UpdateTimeoutChecker implements Runnable {

		private final TwitterProducer twitterProducer;
		private final DataStore dataStore;

		private final Map<Node, List<Update>> prevNodeUpdates = new HashMap<Node, List<Update>>();

		public UpdateTimeoutChecker (final DataStore dataStore, final TwitterProducer twitterProducer) {
			this.dataStore = dataStore;
			this.twitterProducer = twitterProducer;
		}

		@Override
		public void run () {
			try {
				searchForExpiredUpdates();
			}
			catch (final Exception e) {
				LOG.error("Unhandled excpetion while checking for expired updates.", e);
			}
		}

		private void searchForExpiredUpdates () throws SQLException {
			for (final Node node : this.dataStore.getAllNodes()) {
				searchForExpiredUpdates(node);
			}
		}

		private void searchForExpiredUpdates (final Node node) throws SQLException {
			final List<Update> nextAllUpdates = this.dataStore.getUpdates(node.getNode());

			final List<Update> prevUpdates = this.prevNodeUpdates.get(node);
			this.prevNodeUpdates.put(node, nextAllUpdates);

			if (prevUpdates != null) {
				final List<Update> nextExpiredUpdates = filterUpdates(nextAllUpdates, UpdateFlag.EXPIRED);
				if (nextExpiredUpdates.size() > 0) {
					this.twitterProducer.onNewData(node.getNode(), prevUpdates, nextExpiredUpdates);
				}
			}
		}

		private static List<Update> filterUpdates (final List<Update> updates, final UpdateFlag keepFlag) {
			final List<Update> ret = new ArrayList<Update>();
			for (final Update update : updates) {
				if (update.calculateFlag() == keepFlag) ret.add(update);
			}
			return ret;
		}

	}

}
