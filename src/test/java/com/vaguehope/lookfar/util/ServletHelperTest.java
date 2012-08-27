package com.vaguehope.lookfar.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.vaguehope.lookfar.util.ServletHelper;

public class ServletHelperTest {

	@Test
	public void itExtractsElement () throws Exception {
		assertEquals("", ServletHelper.extractPathElement("/", 0));
		assertEquals("a", ServletHelper.extractPathElement("a/", 0));
		assertEquals("", ServletHelper.extractPathElement("//", 1));
		assertEquals("foo", ServletHelper.extractPathElement("/foo", 1));
		assertEquals("foo", ServletHelper.extractPathElement("/foo/", 1));
	}

	@Test
	public void itExtractsPathElement () throws Exception {
		testExtractPathElement("/foo", 1, "foo");
		testExtractPathElement("/foo/", 1, "foo");
		testExtractPathElement("/foo/asf", 1, "foo");
		// TODO check error messages.
		testExtractPathElement("", 1, null);
		testExtractPathElement("/", 1, null);
		testExtractPathElement("//", 1, null);
		testExtractPathElement("//sdf", 1, null);
	}

	@Test
	public void itExtractsSecondPathElement () throws Exception {
		testExtractPathElement("/foo/asf", 2, "asf");
		testExtractPathElement("/foo/asf/", 2, "asf");
		testExtractPathElement("/foo/asf/xcv", 2, "asf");
		testExtractPathElement("//sdf", 2, "sdf");
		// TODO check error messages.
		testExtractPathElement("/foo/", 2, null);
		testExtractPathElement("/foo//", 2, null);
		testExtractPathElement("/foo///", 2, null);
		testExtractPathElement("/foo", 2, null);
		testExtractPathElement("", 2, null);
		testExtractPathElement("/", 2, null);
		testExtractPathElement("//", 2, null);
	}

	private static void testExtractPathElement (String path, int n, String expectedElement) throws IOException {
		assertEquals(expectedElement, ServletHelper.extractPathElement(mockRequest(path), n, mockResponse()));
	}

	private static HttpServletRequest mockRequest (String path) {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getPathInfo()).thenReturn(path);
		return req;
	}

	private static HttpServletResponse mockResponse () throws IOException {
		HttpServletResponse resp = mock(HttpServletResponse.class);
		when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
		return resp;
	}

}
