package com.vaguehope.lookfar.threshold;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MatchesRegexThresholdTest {

	@Test
	public void itMatchesString () throws Exception {
		assertEquals(ThresholdStatus.OK, new MatchesRegexThreshold("a+").isValid("aaa"));
	}

	@Test
	public void itDoesNotMatchNull () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new MatchesRegexThreshold("a*").isValid(null));
	}

	@Test
	public void itDoesNotMatchEmptyString () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new MatchesRegexThreshold("a*").isValid(""));
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void itRejectsNullRegex () throws Exception {
		new MatchesRegexThreshold(null);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void itRejectsEmptyRegex () throws Exception {
		new MatchesRegexThreshold("");
	}

}
