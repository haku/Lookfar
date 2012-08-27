package com.vaguehope.lookfar.threshold;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchesRegexThreshold implements Threshold {

	private static final String IDENTIFER = "=~";

	public static Threshold tryParse (String threshold) {
		if (threshold.startsWith(IDENTIFER) && threshold.length() > IDENTIFER.length()) {
			return new MatchesRegexThreshold(threshold.substring(IDENTIFER.length()));
		}
		return null;
	}

	private final Pattern pattern;

	public MatchesRegexThreshold (String regex) {
		if (regex == null || regex.isEmpty()) throw new IllegalArgumentException();
		this.pattern = Pattern.compile(regex);
	}

	@Override
	public ThresholdStatus isValid (String value) {
		if (value == null || value.isEmpty()) return ThresholdStatus.EXCEEDED;
		Matcher matcher = this.pattern.matcher(value);
		return matcher.matches() ? ThresholdStatus.OK : ThresholdStatus.EXCEEDED;
	}

}
