package com.vaguehope.lookfar.servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class ServletHelperTest {

	@Test
	public void itExtractsPathElement () throws Exception {
		testExtractPathElement("/foo", "foo");
		testExtractPathElement("/foo/", "foo");
		testExtractPathElement("/foo/asf", "foo");
		// TODO check error messages.
		testExtractPathElement("", null);
		testExtractPathElement("/", null);
		testExtractPathElement("//", null);
		testExtractPathElement("//sdf", null);
	}

	private static void testExtractPathElement (String path, String element) throws IOException {
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		when(req.getPathInfo()).thenReturn(path);
		when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
		assertEquals(element, ServletHelper.extractPathElement(req, resp));
	}

}
