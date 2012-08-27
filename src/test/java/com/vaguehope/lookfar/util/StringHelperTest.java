package com.vaguehope.lookfar.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringHelperTest {


	@Test
	public void itCanFindNthElement () throws Exception {
		assertEquals(-1, StringHelper.nthOccurrence("", '/', 0));
		assertEquals(-1, StringHelper.nthOccurrence("a", '/', 0));
		assertEquals(0, StringHelper.nthOccurrence("/", '/', 0));
		assertEquals(1, StringHelper.nthOccurrence("//", '/', 1));
		assertEquals(1, StringHelper.nthOccurrence("a/", '/', 0));
		assertEquals(-1, StringHelper.nthOccurrence("a/", '/', 1));
		assertEquals(0, StringHelper.nthOccurrence("/a/", '/', 0));
		assertEquals(2, StringHelper.nthOccurrence("/a/", '/', 1));
	}

}
