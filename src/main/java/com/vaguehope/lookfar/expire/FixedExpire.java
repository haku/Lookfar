package com.vaguehope.lookfar.expire;

import java.util.Date;

import com.vaguehope.lookfar.config.Config;

public enum FixedExpire implements Expire {

	DEFAULT(new BasicExpire(Config.UPDATE_DEFAULT_EXPIRY_AGE_MILLIS)),
	EXPIRED(new BasicExpire(0L)),
	;

	private final Expire expire;

	private FixedExpire (Expire expire) {
		this.expire = expire;
	}

	@Override
	public ExpireStatus isValid (Date updated) {
		return this.expire.isValid(updated);
	}

}
