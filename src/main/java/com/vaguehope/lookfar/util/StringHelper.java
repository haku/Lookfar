package com.vaguehope.lookfar.util;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public final class StringHelper {

	private StringHelper () {
		throw new AssertionError();
	}

	public static int nthOccurrence (String str, char c, int n) {
		int x = str.indexOf(c);
		if (x < 0) return x;
		for (int i = 0; i < n; i++) {
			x = str.indexOf(c, x + 1);
			if (x < 0) return x;
		}
		return x;
	}

	public static String readerFirstLine (HttpServletRequest req, int maxLen) throws IOException {
		String l = readerFirstLine(req);
		return l.length() > maxLen ? l.substring(0, maxLen) : l;
	}

	public static String readerFirstLine (HttpServletRequest req) throws IOException {
		BufferedReader r = req.getReader();
		try {
			return r.readLine();
		}
		finally {
			r.close();
		}
	}

}
