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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HerokoHttpsFilter implements Filter {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static final Logger LOG = LoggerFactory.getLogger(HerokoHttpsFilter.class);

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

		// Enforce HTTPS?
		String forwardProto = req.getHeader("X-Forwarded-Proto");
		if (!"https".equals(forwardProto)) {
			resp.sendError(javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
			LOG.info("Not HTTPS: for={}", req.getHeader("X-Forwarded-For"));
			return;
		}

		chain.doFilter(request, response);
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
