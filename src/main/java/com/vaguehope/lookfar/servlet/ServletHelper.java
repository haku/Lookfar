package com.vaguehope.lookfar.servlet;

import static com.vaguehope.lookfar.util.Numbers.isNumeric;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public final class ServletHelper {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public static final String ROOT_PATH = "/";
	public static final String CONTENT_TYPE_PLAIN = "text/plain;charset=UTF-8";
	public static final String CONTENT_TYPE_HTML = "text/html;charset=UTF-8";

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
		resp.setContentType("text/plain");
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

	public static String extractPathElement (HttpServletRequest req) throws IOException {
		return extractPathElement(req, null);
	}

	public static String extractPathElement (HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.length() < 2) {
			if (resp != null) ServletHelper.error(resp, HttpServletResponse.SC_BAD_REQUEST, "No element specified.");
			return null;
		}
		int x = pathInfo.indexOf('/', 1);
		String element = x > 0 ? pathInfo.substring(1, x) : pathInfo.substring(1);
		if (element.length() < 1) {
			if (resp != null) ServletHelper.error(resp, HttpServletResponse.SC_BAD_REQUEST, "No element found.");
			return null;
		}
		return element;
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
