package com.vaguehope.lookfar.threshold;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ThresholdParserTest {

	private ThresholdParser undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new ThresholdParser();
	}

	@Test
	public void itDoesNotMindNulls () throws Exception {
		Threshold t = this.undertest.parseThreshold(null);
		assertSame(InvalidThreshold.INSTANCE, t);
	}

	@Test
	public void itParsesStringEqualsSimple () throws Exception {
		Threshold t = this.undertest.parseThreshold("==0");
		assertTrue(t.isValid("0"));
		assertFalse(t.isValid("1"));
	}

	@Test
	public void itParsesStringEqualsLong () throws Exception {
		Threshold t = this.undertest.parseThreshold("==success");
		assertTrue(t.isValid("success"));
		assertFalse(t.isValid("fail"));
	}

}
