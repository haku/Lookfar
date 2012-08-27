package com.vaguehope.lookfar.threshold;

import com.vaguehope.lookfar.util.Numbers;

public class NumberComparatorThreshold implements Threshold {

	public enum MathSymbol { // I can not remember the actual term for <, >, etc.

		GT(">") {
			@Override
			public boolean call (Integer right, Integer left) {
				return left.intValue() > right.intValue();
			}
		},
		GTE(">=") {
			@Override
			public boolean call (Integer right, Integer left) {
				return left.intValue() >= right.intValue();
			}
		},
		LT("<") {
			@Override
			public boolean call (Integer right, Integer left) {
				return left.intValue() < right.intValue();
			}
		},
		LTE("<=") {
			@Override
			public boolean call (Integer right, Integer left) {
				return left.intValue() <= right.intValue();
			}
		}, ;

		private final String identifier;

		private MathSymbol (String identifier) {
			this.identifier = identifier;
		}

		public String getIdentifier () {
			return this.identifier;
		}

		public abstract boolean call (Integer right, Integer left);

	}

	public static Threshold tryParse (String threshold) {
		for (MathSymbol c : MathSymbol.values()) {
			if (threshold.startsWith(c.getIdentifier()) && threshold.length() > c.getIdentifier().length()) {
				return new NumberComparatorThreshold(c, threshold.substring(c.getIdentifier().length()));
			}
		}
		return null;
	}

	private final MathSymbol symbol;
	private final Integer argument;

	public NumberComparatorThreshold (MathSymbol comparator, String argument) {
		this.symbol = comparator;
		if (argument == null || argument.isEmpty()) throw new IllegalArgumentException();
		if (Numbers.isNumeric(argument)) {
			this.argument = Integer.valueOf(argument);
		}
		else {
			this.argument = null;
		}
	}

	@Override
	public ThresholdStatus isValid (String value) {
		if (value == null || value.isEmpty()) return ThresholdStatus.EXCEEDED;
		if (this.argument == null) return ThresholdStatus.INVALID;
		Integer actual = Numbers.tryForceParse(value);
		if (actual != null) {
			return this.symbol.call(this.argument, actual) ? ThresholdStatus.OK : ThresholdStatus.EXCEEDED;
		}
		return ThresholdStatus.INVALID;
	}

}
