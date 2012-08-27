package com.vaguehope.lookfar.threshold;

import static org.junit.Assert.assertEquals;

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
		assertEquals(ThresholdStatus.UNDEFINED, t.isValid(null));
	}

	@Test
	public void itParsesStringEqualsSimple () throws Exception {
		Threshold t = this.undertest.parseThreshold("==0");
		assertEquals(ThresholdStatus.OK, t.isValid("0"));
		assertEquals(ThresholdStatus.EXCEEDED, t.isValid("1"));
	}

	@Test
	public void itParsesStringEqualsLong () throws Exception {
		Threshold t = this.undertest.parseThreshold("==success");
		assertEquals(ThresholdStatus.OK, t.isValid("success"));
		assertEquals(ThresholdStatus.EXCEEDED, t.isValid("fail"));
	}

	@Test
	public void itParsesRegexMatches () throws Exception {
		Threshold t = this.undertest.parseThreshold("=~[0 ]+");
		assertEquals(ThresholdStatus.OK, t.isValid("0 0"));
		assertEquals(ThresholdStatus.EXCEEDED, t.isValid("0 1"));
	}

	@Test
	public void itParsesNumberComparator () throws Exception {
		Threshold t = this.undertest.parseThreshold("<50");
		assertEquals(ThresholdStatus.OK, t.isValid("49"));
		assertEquals(ThresholdStatus.EXCEEDED, t.isValid("55"));
	}

}
