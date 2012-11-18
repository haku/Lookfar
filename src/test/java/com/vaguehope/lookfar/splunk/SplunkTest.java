package com.vaguehope.lookfar.splunk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ System.class, Splunk.class })
public class SplunkTest {

	private Socket mockSocket;
	private Splunk undertest;
	private OutputStream mockOutputStream;

	@Before
	public void before () throws Exception {
		PowerMockito.mockStatic(System.class);
		when(System.getenv("SPLUNK_URL")).thenReturn("tcp://localhost:12345");

		this.mockSocket = mock(Socket.class);
		this.mockOutputStream = new ByteArrayOutputStream();
		PowerMockito.whenNew(Socket.class).withNoArguments().thenReturn(this.mockSocket);
		when(this.mockSocket.getOutputStream()).thenReturn(this.mockOutputStream);

		this.undertest = new Splunk();
	}

	@Test
	public void itIsEnabledWhenConfigure () throws Exception {
		assertTrue(this.undertest.isEnabled());
		assertNotNull(this.undertest.getExcutor());
	}

	@Test
	public void itReadsConfigAndConnectsToRightEndpoint () throws Exception {
		this.undertest.writeUpdate("example=foo");
		ArgumentCaptor<SocketAddress> cap = ArgumentCaptor.forClass(SocketAddress.class);
		verify(this.mockSocket).connect(cap.capture());
		assertEquals("localhost/127.0.0.1:12345", cap.getValue().toString());
	}

	@Test
	public void itWritesUpdate () throws Exception {
		this.undertest.writeUpdate("example=foo");
		assertEquals("example=foo", this.mockOutputStream.toString());
	}

	@Test
	public void itClosesSocket () throws Exception {
		this.undertest.writeUpdate("example=foo");
		verify(this.mockSocket).close();
	}

	@Test
	public void itCanBeDisabled () throws Exception {
		when(System.getenv("SPLUNK_URL")).thenReturn(null);
		this.undertest = new Splunk();

		assertFalse(this.undertest.isEnabled());
		assertNull(this.undertest.getExcutor());
	}

}
