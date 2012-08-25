package com.vaguehope.lookfar.auth;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public interface PasswdChecker {

	boolean verifyPasswd (HttpServletRequest req, String user, String pass) throws IOException;

}
