package com.vaguehope.lookfar;

public final class Modes {

	private Modes () {}

	public static boolean isDebug () {
		return Boolean.parseBoolean(System.getenv("DEBUG"));
	}

	public static boolean isSecure () {
		String e = System.getenv("SECURE");
		return e == null || e.isEmpty() || !e.equalsIgnoreCase("false");
	}

	public static boolean isPostgresSsl () {
		return Boolean.parseBoolean(System.getenv("POSTGRES_SSL"));
	}

}
