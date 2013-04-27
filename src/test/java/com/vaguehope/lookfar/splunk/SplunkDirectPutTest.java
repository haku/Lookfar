package com.vaguehope.lookfar.splunk;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SplunkDirectPutTest {

	private static final String DATA = "some data.";

	@Mock private Splunk splunk;

	private SplunkDirectPut undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new SplunkDirectPut(DATA, this.splunk);
	}

	@Test
	public void itWritesDataToSplunk () throws Exception {
		this.undertest.call();
		verify(this.splunk).writeUpdate(DATA);
	}

}
