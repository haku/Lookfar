package com.vaguehope.lookfar.util;

public final class Numbers {

	private Numbers () {
		throw new AssertionError();
	}

	public static boolean isNumeric (String str) {
		if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != '-') return false;
		for (char c : str.substring(1).toCharArray()) {
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}

}
