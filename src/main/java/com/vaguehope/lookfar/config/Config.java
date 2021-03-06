package com.vaguehope.lookfar.config;

import java.util.concurrent.TimeUnit;

public interface Config {

	// Layout.
	String HOME_PAGE = "/";

	// Server.
	int SERVER_IDLE_TIME_MS = 25000; // 25 seconds in milliseconds.
	int SERVER_SESSION_INACTIVE_TIMEOUT_SECONDS = 60 * 60; // 60 minutes in seconds.

	// Updates.
	long UPDATE_DEFAULT_EXPIRY_AGE_MILLIS = TimeUnit.HOURS.toMillis(25);

}
