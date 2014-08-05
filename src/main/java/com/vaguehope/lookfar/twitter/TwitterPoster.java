package com.vaguehope.lookfar.twitter;

import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterPoster  {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterPoster.class);

	private final Twitter twitter;

	public TwitterPoster () throws TwitterException {
		this.twitter = makeTwitterFactory().getInstance();
		LOG.info("Twitter screen name: {}", this.twitter.getScreenName());
	}

	@Consume
	public void consume(@Body final String body) throws TwitterException {
		this.twitter.updateStatus(body);
		LOG.info("posted: {}.", body);
	}

	private static TwitterFactory makeTwitterFactory () {
		final String consumerKey = readEnv("TWITTER_CONSUMER_KEY");
		final String consumerSecret = readEnv("TWITTER_CONSUMER_SECRET");
		final String accessToken = readEnv("TWITTER_ACCESS_TOKEN");
		final String accessSecret = readEnv("TWITTER_ACCESS_SECRET");
		final ConfigurationBuilder cb = new ConfigurationBuilder()
				.setUseSSL(true)
				.setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessSecret);
		return new TwitterFactory(cb.build());
	}

	private static String readEnv (final String name) {
		String val = System.getenv(name);
		if(val == null || val.length() < 1) throw new IllegalArgumentException("Missing envvar: " + name);
		return val;
	}

}
