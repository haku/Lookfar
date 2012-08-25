package com.vaguehope.lookfar.util;

public interface Http {

	String WWW_AUTHENTICATE = "WWW-Authenticate";
	String BASIC_REALM = "Basic realm=\"Secure Area\"";

	String HEADER_AUTHORISATION = "Authorization"; // Incoming request has this.
	String HEADER_AUTHORISATION_PREFIX = "Basic "; // Incoming request starts with this.

	String CONTENT_TYPE_PLAIN = "text/plain;charset=UTF-8";
	String CONTENT_TYPE_HTML = "text/html;charset=UTF-8";

}
