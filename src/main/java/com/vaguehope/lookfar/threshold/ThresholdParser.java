package com.vaguehope.lookfar.threshold;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ThresholdParser {

	private final LoadingCache<String, Threshold> cache;

	public ThresholdParser () {
		// TODO tweak cache parameters?
		this.cache = CacheBuilder.newBuilder()
				.maximumSize(500)
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
			for (ThresholdTypes tt : ThresholdTypes.values()) {
				Threshold t = tt.tryParse(threshold);
				if (t != null) return t;
			}
			return FixedThreshold.INVALID;
		}

	}

}
