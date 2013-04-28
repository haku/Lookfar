package com.vaguehope.lookfar.expire;

import java.util.Date;

public class BasicExpire implements Expire {

	private final long maxAgeMillis;

	public BasicExpire (final long maxAgeMillis) {
		this.maxAgeMillis = maxAgeMillis;
	}

	@Override
	public ExpireStatus isValid (final Date updated) {
		if (updated == null) return ExpireStatus.PENDING;
		if (this.maxAgeMillis < 1) return ExpireStatus.EXPIRED;
		if (System.currentTimeMillis() - updated.getTime() < this.maxAgeMillis) {
			return ExpireStatus.OK;
		}
		return ExpireStatus.EXPIRED;
	}

}
