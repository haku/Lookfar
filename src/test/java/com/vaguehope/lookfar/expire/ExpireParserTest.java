package com.vaguehope.lookfar.expire;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class ExpireParserTest {

	private ExpireParser undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new ExpireParser();
	}

	@Test
	public void itUsesDefaultForNullsExprire () throws Exception {
		Expire t = this.undertest.parseExpire(null);
		assertEquals(ExpireStatus.OK, t.isValid(new Date()));
	}

	@Test
	public void itTreatsNullsDatesAsPending () throws Exception {
		Expire t = this.undertest.parseExpire("1h");
		assertEquals(ExpireStatus.PENDING, t.isValid(null));
	}

	@Test
	public void itParsesStringEqualsSimple () throws Exception {
		Expire t = this.undertest.parseExpire("1h");
		assertEquals(ExpireStatus.OK, t.isValid(new Date()));
		assertEquals(ExpireStatus.EXPIRED, t.isValid(new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(61))));
	}

}
