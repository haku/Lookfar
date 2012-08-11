package com.vaguehope.lookfar.auth;

public class SharedPasswd implements PasswdChecker {

	private static final String ENV_VAR = "SHARED_PASSWORD";
	private final String passwd;

	public SharedPasswd () {
		String env = System.getenv(ENV_VAR);
		if (env == null) throw new IllegalStateException(ENV_VAR + " not set.");
		this.passwd = env;
	}

	@Override
	public boolean verifyPasswd (String pass) {
		return this.passwd.equals(pass);
	}

}
