package com.vaguehope.lookfar.auth;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PasswdGenTest {

	private PasswdGen undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new PasswdGen();
	}

	@Ignore("Seems to break Travis-CS")
	@Test
	public void itMakesPasswdOfAtLeast26CharLength () throws Exception {
		for (int i = 0; i < 100; i++) {
			String pw = this.undertest.makePasswd();
			assertTrue("length was " + pw.length(), pw.length()>= 26);
		}
	}

}
