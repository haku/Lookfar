package com.vaguehope.lookfar.twitter;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.vaguehope.lookfar.reporter.ReportProvider;

public class TwitterPoster  {

	private static final int TWITTER_ERROR_CODE_STATUS_IS_A_DUPLICATE = 187;
	private static final Logger LOG = LoggerFactory.getLogger(TwitterPoster.class);

	private final Twitter twitter;
	private final AtomicInteger tweetsPosted = new AtomicInteger();

	public TwitterPoster () throws TwitterException {
		this.twitter = makeTwitterFactory().getInstance();
		LOG.info("Twitter screen name: {}", this.twitter.getScreenName());
	}

	public ReportProvider getReporter () {
		return new ReportProvider() {
			@Override
			public void appendReport (final StringBuilder r) {
				r.append(getTweetsPosted()).append(" tweets posted.");
			}
		};
	}

	protected int getTweetsPosted () {
		return this.tweetsPosted.get();
	}

	@Consume
	public void consume(@Body final String body) throws TwitterException {
		final String safeBody = body.length() > 140 ? body.substring(0, 140) : body;
		try {
			this.twitter.updateStatus(safeBody);
			LOG.info("posted: {}.", safeBody);
			this.tweetsPosted.incrementAndGet();
		}
		catch (final TwitterException te) {
			if (te.getErrorCode() == TWITTER_ERROR_CODE_STATUS_IS_A_DUPLICATE) {
				LOG.warn("Failed to post duplicate: {}", body);
				return;
			}
			throw te;
		}
	}

	private static TwitterFactory makeTwitterFactory () {
		final String consumerKey = readEnv("TWITTER_CONSUMER_KEY");
		final String consumerSecret = readEnv("TWITTER_CONSUMER_SECRET");
		final String accessToken = readEnv("TWITTER_ACCESS_TOKEN");
		final String accessSecret = readEnv("TWITTER_ACCESS_SECRET");
		final ConfigurationBuilder cb = new ConfigurationBuilder()
				.setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessSecret);
		return new TwitterFactory(cb.build());
	}

	private static String readEnv (final String name) {
		final String val = System.getenv(name);
		if(val == null || val.length() < 1) throw new IllegalArgumentException("Missing envvar: " + name);
		return val;
	}

}
