package com.vaguehope.lookfar.auth;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswdGenTest {

	@Test
	public void itMakesPasswdOfAtLeast26CharLength () throws Exception {
		for (int i = 0; i < 100; i++) {
			String pw = PasswdGen.makePasswd();
			assertTrue("length was " + pw.length(), pw.length()>= 26);
		}
	}

}
