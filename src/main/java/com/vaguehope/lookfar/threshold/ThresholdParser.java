package com.vaguehope.lookfar.threshold;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ThresholdParser {

	private static final String STRING_EQUALS = "==";

	private final LoadingCache<String, Threshold> cache;

	public ThresholdParser () {
		// TODO tweak cache parameters?
		this.cache = CacheBuilder.newBuilder()
				.maximumSize(1000)
				.expireAfterAccess(1, TimeUnit.HOURS)
				.build(new ThreasholdFactory());
	}

	public Threshold parseThreshold (String threshold) {
		if (threshold == null) return FixedThreshold.UNDEFINED;
		try {
			return this.cache.get(threshold);
		}
		catch (ExecutionException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private static class ThreasholdFactory extends CacheLoader<String, Threshold> {

		public ThreasholdFactory () {}

		@Override
		public Threshold load (String threshold) {
			if (threshold.startsWith(STRING_EQUALS) && threshold.length() > STRING_EQUALS.length()) {
				return new EqualsStringThreshold(threshold.substring(STRING_EQUALS.length()));
			}
			return FixedThreshold.INVALID;
		}

	}

}
