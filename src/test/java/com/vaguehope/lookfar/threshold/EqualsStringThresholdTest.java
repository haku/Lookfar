package com.vaguehope.lookfar.threshold;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EqualsStringThresholdTest {

	@Test
	public void itMatchesString () throws Exception {
		assertEquals(ThresholdStatus.OK, new EqualsStringThreshold("a").isValid("a"));
	}

	@Test
	public void itDoesNotMatchNull () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new EqualsStringThreshold("a").isValid(null));
	}

	@Test
	public void itDoesNotMatchEmptyString () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new EqualsStringThreshold("a").isValid(""));
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void itRejectsNullExpectedValue () throws Exception {
		new EqualsStringThreshold(null);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void itRejectsEmptyExpectedValue () throws Exception {
		new EqualsStringThreshold("");
	}

}
