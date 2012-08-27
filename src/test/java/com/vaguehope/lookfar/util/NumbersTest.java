package com.vaguehope.lookfar.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumbersTest {

	@Test
	public void itReturnsNullForNull () throws Exception {
		assertEquals(null, Numbers.tryForceParse(null));
	}

	@Test
	public void itReturnsNullForEmptyString () throws Exception {
		assertEquals(null, Numbers.tryForceParse(""));
	}

	@Test
	public void itCanExtractJustNumbers () throws Exception {
		assertEquals(Integer.valueOf(123), Numbers.tryForceParse("123"));
	}

	@Test
	public void itCanExtractLeadingNumbers () throws Exception {
		assertEquals(Integer.valueOf(123), Numbers.tryForceParse("123px"));
	}

	@Test
	public void itDoesNotExtractLeadingNonNumbers () throws Exception {
		assertEquals(null, Numbers.tryForceParse("a123px"));
	}

}
