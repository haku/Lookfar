package com.vaguehope.lookfar.threshold;

public class EqualsStringThreshold implements Threshold {

	private static final String IDENTIFIER = "==";

	public static Threshold tryParse (String threshold) {
		if (threshold.startsWith(IDENTIFIER) && threshold.length() > IDENTIFIER.length()) {
			return new EqualsStringThreshold(threshold.substring(IDENTIFIER.length()));
		}
		return null;
	}

	private final String expectedValue;


	public EqualsStringThreshold (String expectedValue) {
		if (expectedValue == null || expectedValue.isEmpty()) throw new IllegalArgumentException();
		this.expectedValue = expectedValue;
	}

	@Override
	public ThresholdStatus isValid (String value) {
		if (value == null || value.isEmpty()) return ThresholdStatus.EXCEEDED;
		return this.expectedValue.equals(value) ? ThresholdStatus.OK : ThresholdStatus.EXCEEDED;
	}

}
