package com.vaguehope.lookfar.threshold;

public enum ThresholdTypes {

	EQUALS_STRING {
		@Override
		public Threshold tryParse (String threshold) {
			return EqualsStringThreshold.tryParse(threshold);
		}
	},
	REGEX {
		@Override
		public Threshold tryParse (String threshold) {
			return MatchesRegexThreshold.tryParse(threshold);
		}
	},
	;

	public abstract Threshold tryParse (String threshold);

}
