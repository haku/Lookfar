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

	public static Integer tryForceParse (String str) {
		if (str == null || str.isEmpty()) return null;
		if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != '-') return null;
		for (int x = 0; x < str.length(); x++) {
			char c = str.charAt(x);
			if (!Character.isDigit(c)) {
				return Integer.valueOf(str.substring(0, x));
			}
		}
		return Integer.valueOf(str);
	}

}
