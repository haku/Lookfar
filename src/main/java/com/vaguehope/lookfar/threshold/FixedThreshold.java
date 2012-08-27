package com.vaguehope.lookfar.threshold;

public enum FixedThreshold implements Threshold {

	UNDEFINED(ThresholdStatus.UNDEFINED),
	INVALID(ThresholdStatus.INVALID),
	;

	private final ThresholdStatus thresholdStatus;

	private FixedThreshold (ThresholdStatus thresholdStatus) {
		this.thresholdStatus = thresholdStatus;

	}

	@Override
	public ThresholdStatus isValid (String value) {
		return this.thresholdStatus;
	}

}
