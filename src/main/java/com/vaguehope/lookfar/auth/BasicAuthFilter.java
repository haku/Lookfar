package com.vaguehope.lookfar.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.B64Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.util.Http;

public class BasicAuthFilter implements Filter {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static final Logger LOG = LoggerFactory.getLogger(BasicAuthFilter.class);

	private final PasswdChecker passwdChecker;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public BasicAuthFilter (PasswdChecker passwdChecker) {
		this.passwdChecker = passwdChecker;
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	@Override
	public void init (FilterConfig arg0) throws ServletException {
		// Unused.
	}

	@Override
	public void destroy () {
		// Unused.
	}

	@Override
	public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		// Request basic auth.
		String authHeader64 = req.getHeader(Http.HEADER_AUTHORISATION);
		if (authHeader64 == null
				|| authHeader64.length() < Http.HEADER_AUTHORISATION_PREFIX.length() + 3
				|| !authHeader64.startsWith(Http.HEADER_AUTHORISATION_PREFIX)) {
			LOG.info("Auth failed: for={} header={} uri={}", new Object[] { req.getHeader("X-Forwarded-For"), authHeader64, req.getRequestURI() });
			send401(resp);
			return;
		}

		// Verify password.
		authHeader64 = authHeader64.substring(Http.HEADER_AUTHORISATION_PREFIX.length());
		String authHeader = B64Code.decode(authHeader64, null);
		int x = authHeader.indexOf(':');
		String user = authHeader.substring(0, x);
		String pass = authHeader.substring(x + 1);
		if (user == null || pass == null || user.isEmpty() || pass.isEmpty() || !this.passwdChecker.verifyPasswd(req, user, pass)) {
			LOG.info("Auth failed: for={} uri={} user={} pass={}", new Object[] { req.getHeader("X-Forwarded-For"), req.getRequestURI(), user, pass });
			send401(resp);
			return;
		}

		chain.doFilter(request, response);
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static void send401 (HttpServletResponse resp) throws IOException {
		resp.setHeader(Http.WWW_AUTHENTICATE, Http.BASIC_REALM);
		resp.sendError(javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
