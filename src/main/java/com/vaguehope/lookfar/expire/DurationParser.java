package com.vaguehope.lookfar.expire;

import java.util.concurrent.TimeUnit;

public final class DurationParser {

	private DurationParser () {
		throw new AssertionError();
	}

	public static long parseDurationToMillis (String str) {
		if (str == null) return 0L;
		long totalMillis = 0L;
		int numberStart = -1;
		for (int x = 0; x < str.length(); x++) {
			char c = str.charAt(x);
			if (Character.isDigit(c)) {
				if (numberStart < 0) numberStart = x;
			}
			else {
				if (numberStart >= 0) {
					int value = Integer.parseInt(str.substring(numberStart, x));
					totalMillis += parseUnitToMillis(value, c);
					numberStart = -1;
				}
				else if (!Character.isWhitespace(c)) {
					return 0L; // Its invalid.
				}
			}
		}
		return totalMillis;
	}

	private static long parseUnitToMillis (int value, char unit) {
		switch (Character.toLowerCase(unit)) {
			case 'd':
				return TimeUnit.DAYS.toMillis(value);
			case 'h':
				return TimeUnit.HOURS.toMillis(value);
			case 'm':
				return TimeUnit.MINUTES.toMillis(value);
			case 's':
				return TimeUnit.SECONDS.toMillis(value);
			default:
				return 0L;
		}
	}

}
