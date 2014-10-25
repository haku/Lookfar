package com.vaguehope.lookfar.auth;

import javax.servlet.http.HttpServletRequest;

public class SharedPasswd implements PasswdChecker {

	private static final String ENV_VAR = "SHARED_PASSWORD";
	private final String passwd;

	public SharedPasswd () {
		String env = System.getenv(ENV_VAR);
		if (env == null) throw new IllegalStateException(ENV_VAR + " not set.");
		this.passwd = env;
	}

	@Override
	public String toString () {
		return "sharedPasswd{}";
	}

	@Override
	public boolean verifyPasswd (final HttpServletRequest req, final String user, final String pass) {
		return this.passwd.equals(pass);
	}

}
