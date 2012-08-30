package com.vaguehope.lookfar.expire;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ExpireParser {

	private final LoadingCache<String, Expire> cache;

	public ExpireParser () {
		// TODO tweak cache parameters?
		this.cache = CacheBuilder.newBuilder()
				.maximumSize(500)
				.expireAfterAccess(1, TimeUnit.HOURS)
				.build(new ExpireFactory());
	}

	public Expire parseExpire (String expire) {
		if (expire == null) return FixedExpire.DEFAULT;
		try {
			return this.cache.get(expire);
		}
		catch (ExecutionException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private static class ExpireFactory extends CacheLoader<String, Expire> {

		public ExpireFactory () {}

		@Override
		public Expire load (String expire) {
			long d = DurationParser.parseDurationToMillis(expire);
			if (d > 0L) return new BasicExpire(d);
			return FixedExpire.EXPIRED;
		}

	}

}
