package com.vaguehope.lookfar.auth;

public class SharedPasswd implements PasswdChecker {

	private final String passwd;

	public SharedPasswd (String passwd) {
		this.passwd = passwd;
	}

	@Override
	public boolean verifyPasswd (String pass) {
		return this.passwd.equals(pass);
	}

}
