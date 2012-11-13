package com.vaguehope.lookfar.splunk;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ System.class, Splunk.class })
public class SplunkTest {

	private Splunk undertest;

	@Before
	public void before () throws Exception {
		PowerMockito.mockStatic(System.class);
		this.undertest = new Splunk();
	}

	@Ignore("Mocking is not working right.")
	@Test
	public void itDoesSomething () throws Exception {
		when(System.getenv("SPLUNK_URL")).thenReturn("tcp:example.com:12345");
		this.undertest.scheduleUpdate("example", ImmutableMap.of("foo", "bar"));
		Thread.sleep(5000L);
	}

}
