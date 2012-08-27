package com.vaguehope.lookfar.config;

public final class Modes {

	private static final String MODE_DEBUG = "DEBUG";
	private static final String MODE_SECURE = "SECURE";
	private static final String MODE_POSTGRES_SSL = "POSTGRES_SSL";

	private Modes () {}

	public static boolean isDebug () {
		return Boolean.parseBoolean(System.getenv(MODE_DEBUG));
	}

	public static boolean isSecure () {
		String e = System.getenv(MODE_SECURE);
		return e == null || e.isEmpty() || !e.equalsIgnoreCase("false");
	}

	public static boolean isPostgresSsl () {
		return Boolean.parseBoolean(System.getenv(MODE_POSTGRES_SSL));
	}

}
