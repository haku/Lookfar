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
	public boolean verifyPasswd (HttpServletRequest req, String user, String pass) {
		return this.passwd.equals(pass);
	}

}
