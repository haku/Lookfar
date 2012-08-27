package com.vaguehope.lookfar.servlet;

import static com.vaguehope.lookfar.util.Numbers.isNumeric;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vaguehope.lookfar.util.Http;

public final class ServletHelper {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public static final String ROOT_PATH = "/";

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private ServletHelper () {
		throw new AssertionError();
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public static String validateStringParam (HttpServletRequest req, HttpServletResponse resp, String param) throws IOException {
		String p = req.getParameter(param);
		if (p != null && !p.isEmpty()) {
			return p;
		}
		error(resp, HttpServletResponse.SC_BAD_REQUEST, "Param '" + param + "' not valid.");
		return null;
	}

	public static long validatePositiveLongParam (HttpServletRequest req, HttpServletResponse resp, String param) throws IOException {
		String p = req.getParameter(param);
		if (p != null && !p.isEmpty() && isNumeric(p)) {
			long n = Long.parseLong(p);
			if (n > 0) {
				return n;
			}
			error(resp, HttpServletResponse.SC_BAD_REQUEST, "Param '" + param + "' not positive.");
			return 0;
		}
		error(resp, HttpServletResponse.SC_BAD_REQUEST, "Param '" + param + "' not valid.");
		return 0;
	}

	public static void error (HttpServletResponse resp, int status, String msg) throws IOException {
		resp.reset();
		resp.setStatus(status);
		resp.setContentType(Http.CONTENT_TYPE_PLAIN);
		resp.getWriter().println("HTTP Error " + status + ": " + msg);
	}

	public static void resetSession (HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session != null) session.invalidate();
		req.getSession(true);
	}

	public static String requestSubPath (HttpServletRequest req, String baseContext) {
		String requestURI = req.getRequestURI();
		String reqPath = requestURI.startsWith(baseContext) ? requestURI.substring(baseContext.length()) : requestURI;
		return reqPath.startsWith(ROOT_PATH) ? reqPath.substring(ROOT_PATH.length()) : reqPath;
	}

	public static String extractPathElement (HttpServletRequest req, int n) throws IOException {
		return extractPathElement(req, n, null);
	}

	public static String extractPathElement (HttpServletRequest req, int n, HttpServletResponse resp) throws IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.length() < 2) {
			if (resp != null) ServletHelper.error(resp, HttpServletResponse.SC_BAD_REQUEST, "No element specified.");
			return null;
		}
		String element = extractPathElement(pathInfo, n);
		if (element == null || element.length() < 1) {
			if (resp != null) ServletHelper.error(resp, HttpServletResponse.SC_BAD_REQUEST, "No element found.");
			return null;
		}
		return element;
	}

	protected static String extractPathElement (String path, int n) {
		if (n < 0) throw new IllegalArgumentException();
		int x = (n == 0 ? 0 : nthOccurrence(path, '/', n - 1));
		String element;
		if (x >= 0) {
			int y = path.indexOf('/', x + 1);
			if (y > x) {
				element = path.substring(n == 0 ? 0 : x + 1, y);
			}
			else if (y == x) {
				element = "";
			}
			else {
				element = path.substring(x + 1);
			}
		}
		else {
			element = n == 0 ? path : null;
		}
		return element;
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	protected static int nthOccurrence (String str, char c, int n) {
		int x = str.indexOf(c);
		if (x < 0) return x;
		for (int i = 0; i < n; i++) {
			x = str.indexOf(c, x + 1);
			if (x < 0) return x;
		}
		return x;
	}

}
