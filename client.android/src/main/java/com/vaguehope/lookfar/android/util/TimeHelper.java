package com.vaguehope.lookfar.android.util;

import java.util.concurrent.TimeUnit;

public class TimeHelper {

	private TimeHelper () {
		throw new AssertionError();
	}

	public static String humanTimeSpan (final long thenMillis, final long nowMillis) {
		if (thenMillis == 0L) return "";

		final long durSeconds = TimeUnit.MILLISECONDS.toSeconds(nowMillis - thenMillis);

		final long days = durSeconds / 86400;
		final long hours = (durSeconds % 86400) / 3600;
		final long minutes = (durSeconds % 3600) / 60;
		final long seconds = (durSeconds % 60);

		if (days > 0) return String.format("%sd %sh", days, hours);
		if (hours > 0) return String.format("%sh %sm", hours, minutes);
		if (minutes > 0) return String.format("%sm %ss", minutes, seconds);
		if (seconds > 0) return String.format("%ss", seconds);

		return String.valueOf(durSeconds);
	}

}
