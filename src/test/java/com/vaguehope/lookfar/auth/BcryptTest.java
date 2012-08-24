package com.vaguehope.lookfar.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

public class BcryptTest {

	@Test
	public void hashesAre60CharInLength () throws Exception {
		String password = "sdfsdf";
		String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
		assertEquals(60, hashed.length());
	}
}
