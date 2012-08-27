package com.vaguehope.lookfar.threshold;

public enum InvalidThreshold implements Threshold {
	INSTANCE;

	@Override
	public boolean isValid (String value) {
		return false;
	}

}
