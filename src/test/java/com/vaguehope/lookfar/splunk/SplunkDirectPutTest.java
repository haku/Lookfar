package com.vaguehope.lookfar.splunk;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SplunkDirectPutTest {

	private static final String DATA = "some data.";

	@Mock private Splunk splunk;
	@Mock private SplunkProducer splunkProducer;

	private SplunkDirectPut undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new SplunkDirectPut(DATA, this.splunk, this.splunkProducer);
	}

	@Test
	public void itWritesDataToSplunk () throws Exception {
		this.undertest.call();
		verify(this.splunk).writeUpdate(DATA);
		verify(this.splunkProducer, never()).rescheduleUpdate(anyString());
	}

	@Test
	public void itReschedulesOnIoEx () throws Exception {
		doThrow(new IOException("Example ex.")).when(this.splunk).writeUpdate(anyString());
		this.undertest.call();
		verify(this.splunkProducer).rescheduleUpdate(DATA);
	}

}
