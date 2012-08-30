package com.vaguehope.lookfar.expire;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DurationParserTest {

	@Test
	public void itCanParseTwoDays () throws Exception {
		assertEquals(2L * 24L * 60L * 60L * 1000L, DurationParser.parseDurationToMillis("2d"));
	}

	@Test
	public void itCanParseTwoHour () throws Exception {
		assertEquals(2L * 60L * 60L * 1000L, DurationParser.parseDurationToMillis("2h"));
	}

	@Test
	public void itCanParseTwoMinutes () throws Exception {
		assertEquals(2L * 60L * 1000L, DurationParser.parseDurationToMillis("2m"));
	}

	@Test
	public void itCanParseTwoSeconds () throws Exception {
		assertEquals(2L * 1000L, DurationParser.parseDurationToMillis("2s"));
	}

	@Test
	public void itCanParseMixedUnits () throws Exception {
		assertEquals((5L * 24L * 60L * 60L + 2L * 60L * 60L + 50L * 60L + 32L) * 1000L, DurationParser.parseDurationToMillis("5d 2h 50m 32s"));
	}

	@Test
	public void itReturnsZeroOnMalformed () throws Exception {
		assertEquals(0L, DurationParser.parseDurationToMillis("2ss"));
	}

	@Test
	public void itReturnsZeroOnEmpty () throws Exception {
		assertEquals(0L, DurationParser.parseDurationToMillis(""));
	}

	@Test
	public void itReturnsZeroOnNull () throws Exception {
		assertEquals(0L, DurationParser.parseDurationToMillis(null));
	}

}
