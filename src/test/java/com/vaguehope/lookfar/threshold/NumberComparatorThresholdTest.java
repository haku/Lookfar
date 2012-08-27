package com.vaguehope.lookfar.threshold;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaguehope.lookfar.threshold.NumberComparatorThreshold.MathSymbol;

public class NumberComparatorThresholdTest {

	@Test
	public void itMatchesGreaterThanGood () throws Exception {
		assertEquals(ThresholdStatus.OK, new NumberComparatorThreshold(MathSymbol.GT, "5").isValid("6"));
	}

	@Test
	public void itMatchesGreaterThanBad () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new NumberComparatorThreshold(MathSymbol.GT, "5").isValid("5"));
	}

	@Test
	public void itMatchesGreaterThanOrEqualToGood () throws Exception {
		assertEquals(ThresholdStatus.OK, new NumberComparatorThreshold(MathSymbol.GTE, "5").isValid("6"));
	}

	@Test
	public void itMatchesGreaterThanOrEqualToGood2 () throws Exception {
		assertEquals(ThresholdStatus.OK, new NumberComparatorThreshold(MathSymbol.GTE, "5").isValid("5"));
	}

	@Test
	public void itMatchesGreaterThanOrEqualBad () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new NumberComparatorThreshold(MathSymbol.GTE, "5").isValid("4"));
	}

	@Test
	public void itMatchesLessThanGood () throws Exception {
		assertEquals(ThresholdStatus.OK, new NumberComparatorThreshold(MathSymbol.LT, "5").isValid("4"));
	}

	@Test
	public void itMatchesLessThanBad () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new NumberComparatorThreshold(MathSymbol.LT, "5").isValid("5"));
	}

	@Test
	public void itMatchesLessThanOrEqualToGood () throws Exception {
		assertEquals(ThresholdStatus.OK, new NumberComparatorThreshold(MathSymbol.LTE, "5").isValid("4"));
	}

	@Test
	public void itMatchesLessThanOrEqualToGood2 () throws Exception {
		assertEquals(ThresholdStatus.OK, new NumberComparatorThreshold(MathSymbol.LTE, "5").isValid("5"));
	}

	@Test
	public void itMatchesLessThanOrEqualBad () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new NumberComparatorThreshold(MathSymbol.LTE, "5").isValid("6"));
	}

	@Test
	public void itDoesNotMatchNull () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new NumberComparatorThreshold(MathSymbol.GT, "5").isValid(null));
	}

	@Test
	public void itDoesNotMatchEmptyString () throws Exception {
		assertEquals(ThresholdStatus.EXCEEDED, new NumberComparatorThreshold(MathSymbol.GT, "5").isValid(""));
	}

	@Test
	public void itIsOkWithTrailingNonNumbers () throws Exception {
		assertEquals(ThresholdStatus.OK, new NumberComparatorThreshold(MathSymbol.GT, "5").isValid("6px"));
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void itRejectsNullSymbol () throws Exception {
		new NumberComparatorThreshold(null, "");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void itRejectsNullExpectedValue () throws Exception {
		new NumberComparatorThreshold(MathSymbol.GT, null);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void itRejectsEmptyExpectedValue () throws Exception {
		new NumberComparatorThreshold(MathSymbol.GT, "");
	}

}
